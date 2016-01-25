package mx.com.gunix.adminapp.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.groups.Default;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.com.gunix.adminapp.domain.persistence.AplicacionMapper;
import mx.com.gunix.adminapp.domain.persistence.RolMapper;
import mx.com.gunix.adminapp.domain.persistence.UsuarioAdminMapper;
import mx.com.gunix.framework.domain.validation.GunixValidationGroups.BeanValidations;
import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.service.GunixActivitServiceSupport;

@Service("usuarioAdminService")
@Transactional(rollbackFor = Exception.class)
@Secured(AuthenticatedVoter.IS_AUTHENTICATED_REMEMBERED)
public class UsuarioAdminServiceImpl extends GunixActivitServiceSupport<Usuario> {
	@Autowired
	AplicacionMapper am;
	
	@Autowired
	UsuarioAdminMapper um;	
	
	@Autowired
	RolMapper rm;
	
	private PasswordEncoder pe;
	
	public List<Aplicacion> getAppRoles() {
		List<Aplicacion> appRoles =  am.getAll();
		if(appRoles==null){
			throw new IllegalArgumentException("No se encontraron Aplicaciónes");
		}else{
			appRoles.forEach(app -> {app.setRoles(rm.getByIdAplicacion(app.getIdAplicacion()));} );						
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
				
			}
			
		}else{
			agregaVariable("errores", toStringList(result));
		}
		
		return esValido;		
	}
	
	public void doInsert(Usuario usuario){
		pe = new BCryptPasswordEncoder();
		String encodedPassword = pe.encode(usuario.getPassword());
		usuario.setEncodePassword(encodedPassword);
		um.inserta(usuario);
		um.insertaDatos(usuario.getIdUsuario(), usuario.getDatosUsuario());
		usuario.getAplicaciones().forEach(aplicacion->{doInsertAppRoles(usuario.getIdUsuario(),aplicacion);});
	}
	
	public void doInsertAppRoles(String idUsuario,Aplicacion aplicacion){
		um.insertaUsuarioApp(idUsuario,aplicacion.getIdAplicacion());
		aplicacion.getRoles().forEach(rol->{um.insertaUsuarioRol(idUsuario, aplicacion.getIdAplicacion(), rol.getIdRol());});
	}
}
