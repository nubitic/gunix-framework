package mx.com.gunix.adminapp.service;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.validation.ConstraintViolation;
import javax.validation.groups.Default;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.com.gunix.adminapp.domain.persistence.AmbitoMapper;
import mx.com.gunix.adminapp.domain.persistence.RolMapper;
import mx.com.gunix.adminapp.domain.persistence.UsuarioAdminMapper;
import mx.com.gunix.framework.domain.validation.GunixValidationGroups.BeanValidations;
import mx.com.gunix.framework.security.domain.ACLType;
import mx.com.gunix.framework.security.domain.ACLTypeMap;
import mx.com.gunix.framework.security.domain.Ambito;
import mx.com.gunix.framework.security.domain.Ambito.Permiso;
import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.service.ACLTypeService;
import mx.com.gunix.framework.service.GetterService;
import mx.com.gunix.framework.service.GunixActivitServiceSupport;
import mx.com.gunix.framework.service.hessian.ByteBuddyUtils;

@Service("usuarioAdminService")
@Transactional(rollbackFor = Exception.class)
@Secured(AuthenticatedVoter.IS_AUTHENTICATED_REMEMBERED)
public class UsuarioAdminServiceImpl extends GunixActivitServiceSupport<Usuario> {
	
	private static final Logger log = Logger.getLogger(UsuarioAdminServiceImpl.class); 
	
	@Autowired
	@Qualifier("aplicacionService")
	ACLTypeService<Aplicacion> am;
	
	@Autowired
	UsuarioAdminMapper um;	
	
	@Autowired
	RolMapper rm;
	
	@Autowired
	AmbitoMapper ambm;
	
	@Autowired
	MutableAclService aclService;
	
	@Autowired
	PasswordEncoder pe;
	
	private static Class<GetterService> securedACLGetterInterface;
	private static Method getMethod;
	
	public List<Aplicacion> getAppRoles() {
		List<Aplicacion> appRoles = am.getAllForAdmin();
		if (appRoles == null) {
			throw new IllegalArgumentException("No se encontraron Aplicaciones");
		} else {
			appRoles.forEach(app -> {
				app.setRoles(rm.getByIdAplicacion(app.getIdAplicacion()));
				app.setAmbito(ambm.getByIdAplicacion(app.getIdAplicacion()));
				if (app.getAmbito() != null) {
					app.getAmbito().forEach(ambito -> {
						initAmbito(ambito, null);
					});
				}
			});
		}
		return appRoles;
	}

	public boolean isValid(Usuario usuario){
		boolean esValido = false;
		Set<ConstraintViolation<Usuario>> result = valida(usuario, Default.class, BeanValidations.class);
		
		Usuario u = null;
		
		if (result.isEmpty()) {
			if ("Alta".equals($("operación"))){
				//validamos que no exista el usuario
				u = um.getByidUsuario(usuario.getIdUsuario());
				if(u == null){
					esValido = true;
				}else{
					List<String> errores = new ArrayList<String>();
					errores.add("El Usuario no se puede dar de Alta porque ya existe un Usuario con id " + u.getIdUsuario());
					agregaVariable("errores", errores);
				}
				
			}else{
				esValido = true;
			}
			
		}else{
			agregaVariable("errores", toStringList(result));
		}
		
		return esValido;		
	}
	
	public void doInsert(Usuario usuario){
		String encodedPassword = pe.encode(usuario.getPassword());
		usuario.setEncodePassword(encodedPassword);
		um.insertaUsuario(usuario);
		um.insertaDatos(usuario.getIdUsuario(), usuario.getDatosUsuario());
		usuario.getAplicaciones().forEach(aplicacion -> {
			doInsertAppRoles(usuario.getIdUsuario(), aplicacion);
			doAmbito(usuario.getIdUsuario(), aplicacion, false);
		});
	}
	
	public void doInsertAppRoles(String idUsuario,Aplicacion aplicacion){
		um.insertaUsuarioApp(idUsuario,aplicacion.getIdAplicacion());
		aplicacion.getRoles().forEach(rol->{um.insertaUsuarioRol(idUsuario, aplicacion.getIdAplicacion(), rol.getIdRol());});
	}
	
	public void doUpdate(Usuario usuario) {
		String idUsuario = usuario.getIdUsuario();
		um.updateUsuario(usuario);
		um.updateDatosUsuario(idUsuario, usuario.getDatosUsuario());
		um.deleteRolesUsuario(idUsuario);
		um.deleteAppUsuario(idUsuario);
		usuario.getAplicaciones().forEach(aplicacion -> {
			doInsertAppRoles(idUsuario, aplicacion);
			doAmbito(idUsuario, aplicacion, true);
		});
	}
	
	private void doAmbito(String idUsuario, Aplicacion aplicacion, Boolean isUpdate) {
		if (aplicacion.getAmbito() != null) {
			List<Sid> userSid = new ArrayList<Sid>();
			PrincipalSid psid = new PrincipalSid(idUsuario);
			userSid.add(psid);
			aplicacion.getAmbito().forEach(ambito -> {
				if (ambito.getPermisos() != null) {
					List<ObjectIdentity> objects = new ArrayList<ObjectIdentity>();
					Map<ObjectIdentity, Permiso> oI2Permiso = new HashMap<ObjectIdentity, Permiso>();
					ambito.getPermisos().forEach(permiso -> {
						ObjectIdentity oi = new ObjectIdentityImpl(ambito.getClase(), permiso.getAclType().getId());
						objects.add(oi);
						oI2Permiso.put(oi, permiso);
					});
					Map<ObjectIdentity, Acl> permisosUsuario = aclService.readAclsById(objects);
					
					if (isUpdate) {
						permisosUsuario.forEach((oi, acl) -> {
							AtomicReference<Permiso> curPer = new AtomicReference<Permiso>();
							if (acl.isSidLoaded(userSid)) {
								AtomicInteger entryCounter = new AtomicInteger(0);
								acl.getEntries().forEach(entry -> {
									// Se actualizan los ACEs que el usuario ya tenía
										if (psid.equals(entry.getSid())) {
											MutableAcl macl = (MutableAcl) acl;
											CumulativePermission permission = buildPermission(curPer.get() == null ? initPerm(curPer, oI2Permiso.remove(oi)) : curPer.get());
											macl.deleteAce(entryCounter.get());
											macl.insertAce(entryCounter.get(), permission, psid, true);
										}
										entryCounter.addAndGet(1);
									});
								aclService.updateAcl((MutableAcl) acl);
							}
						});
					}

					// Se ingresan nuevos ACEs que el usuario no tenía
					oI2Permiso.forEach((oii, perm) -> {
						MutableAcl acl = (MutableAcl) permisosUsuario.get(oii);
						acl.insertAce(acl.getEntries().size(), buildPermission(perm), psid, true);
						aclService.updateAcl(acl);
					});
					ambm.deleteFullReadAccessFor(idUsuario, ambito);
					if (ambito.isPuedeLeerTodos()) {
						ambm.insertFullReadAccessFor(idUsuario, ambito);
					}
				}
			});
		}
	}

	private Permiso initPerm(AtomicReference<Permiso> curPer, Permiso remove) {
		curPer.set(remove);
		return remove;
	}

	private CumulativePermission buildPermission(Permiso p) {
		CumulativePermission cumPer = new CumulativePermission();

		if (p.isLectura()) {
			cumPer.set(BasePermission.READ);
		}
		if (p.isEliminacion()) {
			cumPer.set(BasePermission.DELETE);
		}
		if (p.isModificacion()) {
			cumPer.set(BasePermission.WRITE);
		}
		if (p.isAdministracion()) {
			cumPer.set(BasePermission.ADMINISTRATION);
		}
		return cumPer;
	}

	@SuppressWarnings({ "null"})
	public List<Usuario> getByExample(Boolean esMaestro, Usuario usuario) {
		List<Usuario> usuarios = null;
		if (esMaestro) {
			usuarios = um.getByExample(usuario);			
		}else{
			List<Aplicacion> appRoles = getAppRoles();
			this.agregaVariable("aplicaciones", appRoles);
			usuarios = new ArrayList<Usuario>();
			usuarios.add(um.getDetalleUById(usuario.getIdUsuario()));		

			if (!usuarios.isEmpty()) {
				Usuario u = usuarios.get(0);
				if(u==null) {
					throw new IllegalArgumentException("No se encontró el detalle del usuario con id: "+u.getIdUsuario());
				}
				
				List<Aplicacion> appRolesUS =  um.getAppUsuario(u.getIdUsuario());
						
				if(appRolesUS.isEmpty()){
					throw new IllegalArgumentException("No se encontrarion Aplicaciónes asociadas al usuario con id: "+u.getIdUsuario()); 
				}

				appRolesUS.forEach(app -> {
					app.setRoles(um.getRolAppUsuario(u.getIdUsuario(), app.getIdAplicacion()));
					app.setAmbito(ambm.getByIdAplicacion(app.getIdAplicacion()));
					if (app.getAmbito() != null) {
						app.getAmbito().forEach(ambito -> {
							initAmbito(ambito, u);
						});
					}
				});
				u.setAplicaciones(appRolesUS);
			}
		}
		return usuarios;	
	}

	private void initAmbito(Ambito ambito, Usuario u) {
		try {
			String[] serviceUrl = ambito.getGetAllUri().split("\\?");
			GetterService gs = createGetter(serviceUrl[0]);
			List<ACLType> aclTypes = sanitize((List<?>) getMethod.invoke(gs, new Object[] { serviceUrl[1].split("=")[1] + "/getAllForAdmin", new Object[0], SecurityContextHolder.getContext().getAuthentication().getPrincipal() }));
			
			if (aclTypes != null && !aclTypes.isEmpty()) {
				List<ObjectIdentity> objects = new ArrayList<ObjectIdentity>();
				Map<ACLType, ObjectIdentity> aclType2OI = new HashMap<ACLType, ObjectIdentity>();
				aclTypes.forEach(aclType -> {
					ObjectIdentity oi = new ObjectIdentityImpl(ambito.getClase(), aclType.getId()); 
					objects.add(oi);
					aclType2OI.put(aclType, oi);
				});
				
				Map<ObjectIdentity, Acl> permisosUsuario = new HashMap<ObjectIdentity, Acl>();
				List<Sid> userSid = new ArrayList<Sid>();

				if (u != null) {
					userSid.add(new PrincipalSid(u.getIdUsuario()));
					permisosUsuario.putAll(aclService.readAclsById(objects, userSid));
					ambito.setPuedeLeerTodos(ambm.puedeLeerTodo(u.getIdUsuario(), ambito));
				}

				ambito.setPermisos(new ArrayList<Permiso>());
				aclTypes.forEach(aclType -> {
					ObjectIdentity foundOI = aclType2OI.get(aclType);
					Acl aclUsr = foundOI != null ? permisosUsuario.get(foundOI) : null;
					
					Permiso p = new Permiso();
					p.setAclType(aclType);

					if (aclUsr != null) {
						List<Permission> permiso = new ArrayList<Permission>();

						if (ambito.isPuedeLeerTodos()) {
							p.setLectura(true);
						} else {
							permiso.add(BasePermission.READ);
							try {
								if (aclUsr.isGranted(permiso, userSid, true)) {
									p.setLectura(true);
								}
							} catch (NotFoundException ignorar) {
							} finally {
								permiso.clear();
							}
						}
						
						permiso.add(BasePermission.WRITE);
						try {
							if (aclUsr.isGranted(permiso, userSid, true)) {
								p.setModificacion(true);
							}
						} catch (NotFoundException ignorar) {
						} finally {
							permiso.clear();
						}
						
						permiso.add(BasePermission.DELETE);
						try {
							if (aclUsr.isGranted(permiso, userSid, true)) {
								p.setEliminacion(true);
							}
						} catch (NotFoundException ignorar) {
						} finally {
							permiso.clear();
						}
						
						permiso.add(BasePermission.ADMINISTRATION);
						try {
							if (aclUsr.isGranted(permiso, userSid, true)) {
								p.setAdministracion(true);
							}
						} catch (NotFoundException ignorar) {
						} finally {
							permiso.clear();
						}
					}
					ambito.getPermisos().add(p);
				});
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			if (ExceptionUtils.getRootCause(e) instanceof FileNotFoundException) {
				log.error("No fue posible inicializar el ámbito: " + ambito.toString(), e);
			} else {
				throw new RuntimeException(e);
			}
		} catch (Exception ignorar) {
			log.error("No fue posible inicializar el ámbito: " + ambito.toString(), ignorar);
		}
	}

	@SuppressWarnings("unchecked")
	private List<ACLType> sanitize(List<?> list) {
		if (list != null && !list.isEmpty()) {
			Object sampleMap = null;
			if ((sampleMap = list.get(0)) instanceof Map) {
				List<ACLType> wrappedMapACLType = new ArrayList<ACLType>();
				
				String idPropertyName = resolve((Map<String, Object>) sampleMap, "id");
				String descripcionPropertyName = resolve((Map<String, Object>) sampleMap, "descripcion");
				String claveNegocioPropertyName = resolve((Map<String, Object>) sampleMap, "claveNegocio");

				list.forEach(aclTypeMap -> {
					wrappedMapACLType.add(new ACLTypeMap((Map<String, Object>) aclTypeMap, idPropertyName, descripcionPropertyName, claveNegocioPropertyName));
				});
				return wrappedMapACLType;
			}
		}
		return (List<ACLType>) list;
	}

	private String resolve(Map<String, Object> aclTypeMap, String property) {
		AtomicReference<String> actualPropertyName = new AtomicReference<String>();
		if ("id".equals(property)) {
			aclTypeMap.forEach((propertyName, value) -> {
				if (actualPropertyName.get() == null && propertyName.toLowerCase().indexOf("id") != -1 && value != null && (value instanceof Long || long.class.isAssignableFrom(value.getClass()))) {
					actualPropertyName.set(propertyName);
				}
			});
		} else {
			if ("descripcion".equals(property)) {
				aclTypeMap.forEach((propertyName, value) -> {
					if (actualPropertyName.get() == null && propertyName.toLowerCase().indexOf("desc") != -1 && value != null && value instanceof String) {
						actualPropertyName.set(propertyName);
					}
				});
			} else {
				if ("claveNegocio".equals(property)) {
					aclTypeMap.forEach((propertyName, value) -> {
						if (actualPropertyName.get() == null && value != null && (propertyName.toLowerCase().indexOf("desc") == -1 && propertyName.toLowerCase().indexOf("id") == -1)) {
							actualPropertyName.set(propertyName);
						}
					});
				}
			}
		}
		return actualPropertyName.get();
	}

	@SuppressWarnings("unchecked")
	private GetterService createGetter(String hostURL) throws NoSuchMethodException, SecurityException {
		HessianProxyFactoryBean factoryBean = new HessianProxyFactoryBean();
		if (securedACLGetterInterface == null) {
			securedACLGetterInterface = (Class<GetterService>) ByteBuddyUtils.appendUsuario(getClass().getClassLoader(), GetterService.class.getName());
			getMethod = securedACLGetterInterface.getMethod("get", new Class[] { String.class, Object[].class, UserDetails.class });
		}
		factoryBean.setServiceInterface(securedACLGetterInterface);
		factoryBean.setServiceUrl(hostURL + "/getterService");
		factoryBean.afterPropertiesSet();
		return (GetterService) factoryBean.getObject();
	}
}
