package mx.com.gunix.adminapp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.groups.Default;

import mx.com.gunix.adminapp.domain.persistence.AplicacionMapper;
import mx.com.gunix.adminapp.domain.persistence.FuncionMapper;
import mx.com.gunix.adminapp.domain.persistence.ModuloMapper;
import mx.com.gunix.adminapp.domain.persistence.RolMapper;
import mx.com.gunix.framework.domain.validation.GunixValidationGroups.BeanValidations;
import mx.com.gunix.framework.domain.validation.GunixValidationGroups.DatabaseValidation;
import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.Funcion;
import mx.com.gunix.framework.security.domain.Funcion.Acceso;
import mx.com.gunix.framework.security.domain.Modulo;
import mx.com.gunix.framework.security.domain.Parametro;
import mx.com.gunix.framework.service.ACLTypeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("aplicacionService")
@Transactional(rollbackFor = Exception.class)
@Secured(AuthenticatedVoter.IS_AUTHENTICATED_REMEMBERED)
public class AplicacionServiceImpl extends ACLTypeService<Aplicacion> {

	@Autowired
	AplicacionMapper am;

	@Autowired
	ModuloMapper mm;

	@Autowired
	FuncionMapper fm;

	@Autowired
	RolMapper rm;

	@Override
	public Aplicacion getById(Long id) {
		return am.getById(id);
	}

	@Override
	public List<Aplicacion> getAll() {
		return am.getAll();
	}

	@Override
	public void update(Aplicacion aclType) {

	}

	@Override
	public void delete(Aplicacion aclType) {

	}

	@Override
	protected void doInsert(Aplicacion aplicacion) {
		am.inserta(aplicacion);
		aplicacion.getModulos().forEach(modulo -> {
			mm.inserta(modulo);
			int orden= 0;
			modulo.getFunciones().forEach(funcion -> {
				doInsertFuncion(funcion, orden+1);
			});
		});
		aplicacion.getRoles().forEach(
				rol -> {
					rm.inserta(rol);

					rol.getModulos().forEach(
							modulo -> {
								Modulo modRolOrg = acomodaFuncionesModulos(aplicacion, modulo.getFunciones()).get(0);
								List<Funcion> funcionesPadre = new ArrayList<Funcion>();
								insertaFuncionesRol(rol.getIdRol(), modRolOrg.getFunciones(), toListFuncionesPadre(funcionesPadre, aplicacion.getModulos().get(aplicacion.getModulos().indexOf(modRolOrg)).getFunciones()));
							});
				});
	}

	private List<Funcion> toListFuncionesPadre(List<Funcion> funcionesPadre, List<Funcion> funciones) {
		funciones.forEach(funcion -> {
			if (funcion.getHijas() != null && !funcion.getHijas().isEmpty()) {
				funcionesPadre.add(funcion);
				toListFuncionesPadre(funcionesPadre, funcion.getHijas());
			}
		});
		return funcionesPadre;
	}

	private void insertaFuncionesRol(String idRol, List<Funcion> funciones, List<Funcion> funcionesPadre) {
		funciones.forEach(funcionSeleccionada -> {
			boolean accesoCompleto = false;
			if (funcionSeleccionada.getHijas() != null && !funcionSeleccionada.getHijas().isEmpty()) {
				int funcionesHijasDisponiblesIdx = -1;
				if ((funcionesHijasDisponiblesIdx = funcionesPadre.indexOf(funcionSeleccionada)) >= 0) {
					if (funcionSeleccionada.getHijas().size() == funcionesPadre.get(funcionesHijasDisponiblesIdx).getHijas().size()) {
						accesoCompleto = true;
						funcionSeleccionada.setAcceso(Acceso.COMPLETO);
					} else {
						funcionSeleccionada.setAcceso(Acceso.PUNTUAL);
					}
				} else {
					throw new IllegalStateException("Relación función-rol inválida: Se intentó asociar una función con hijas que de acuerdo al módulo correspondiente no debe tener hijas");
				}
			} else {
				if (funcionesPadre.contains(funcionSeleccionada) && (funcionSeleccionada.getHijas() == null || funcionSeleccionada.getHijas().isEmpty())) {
					throw new IllegalStateException("Relación función-rol inválida: Se intentó asociar una función sin hijas que de acuerdo al módulo correspondiente debe tener al menos una");
				} else {
					funcionSeleccionada.setAcceso(Acceso.PUNTUAL);
				}
			}

			rm.insertaFuncion(idRol, funcionSeleccionada);

			if (!accesoCompleto && funcionSeleccionada.getHijas() != null && !funcionSeleccionada.getHijas().isEmpty()) {
				insertaFuncionesRol(idRol, funcionSeleccionada.getHijas(), funcionesPadre);
			}
		});
	}

	private void doInsertFuncion(Funcion funcion, int orden) {
		funcion.setOrden(orden);
		fm.inserta(funcion);
		if (funcion.getParametros() != null) {
			for (Parametro param : funcion.getParametros()) {
				fm.insertaParametro(funcion, param);
			}
		}
		if (funcion.getHijas() != null) {
			funcion.getHijas().forEach(funcionHija -> {
				doInsertFuncion(funcionHija, orden+1);
			});
		}
	}

	public boolean isValid(Aplicacion aplicacion) {
		boolean isValid = false;
		Set<ConstraintViolation<Aplicacion>> result = valida(aplicacion, Default.class, BeanValidations.class);
		if (result.isEmpty()) {
			if (aplicacion.getId() == null && "Alta".equals($("operación"))) {
				if (am.getByidAplicacion(aplicacion.getIdAplicacion()) == null) {
					isValid = true;
				} else {
					List<String> errores = new ArrayList<String>();
					errores.add("La aplicación no se puede dar de Alta porque ya existe una aplicación con id " + aplicacion.getIdAplicacion());
					agregaVariable("errores", errores);
				}
			} else {
				result = valida(aplicacion, DatabaseValidation.class);
				if (result.isEmpty()) {
					isValid = true;
				} else {
					agregaVariable("errores", toStringList(result));
				}
			}
		} else {
			agregaVariable("errores", toStringList(result));
		}
		return isValid;
	}

	public List<Aplicacion> consulta(Boolean esMaestro, Aplicacion aplicacion) {
		List<Aplicacion> apps = am.getByExample(aplicacion);
		if (!esMaestro) {
			if (apps.size() > 1) {
				throw new IllegalStateException("Para la consulta a detalle se debe obtener sólo una aplicación");
			}
			if (!apps.isEmpty()) {
				Aplicacion app = apps.get(0);
				app.setModulos(mm.getByIdAplicacion(app.getIdAplicacion()));
				if (app.getModulos() != null) {
					app.getModulos().forEach(modulo -> {
						modulo.setAplicacion(app);
						List<Funcion> funciones = fm.getByIdModulo(app.getIdAplicacion(), modulo.getIdModulo());
						funciones.forEach(funcion -> {
							funcion.setParametros(fm.getParametrosByIdFuncion(app.getIdAplicacion(), modulo.getIdModulo(), funcion.getIdFuncion()));
						});
						modulo.setFunciones(Funcion.jerarquizaFunciones(modulo, funciones));
					});
				}
				app.setRoles(rm.getByIdAplicacion(app.getIdAplicacion()));
				if (app.getRoles() != null) {
					app.getRoles().forEach(rol -> {
						rol.setAplicacion(app);
						rol.setModulos(acomodaFuncionesModulos(app, rm.getFuncionesByIdRol(app.getIdAplicacion(), rol.getIdRol())));
					});
				}
			}
		}
		return apps;
	}

	private List<Modulo> acomodaFuncionesModulos(Aplicacion app, List<Funcion> funcionesByIdRol) {
		Objects.requireNonNull(funcionesByIdRol);
		List<Modulo> modulosRol = new ArrayList<Modulo>();
		funcionesByIdRol.forEach(funcion -> {
			if (funcion.getPadre() != null && funcion.getPadre().getIdFuncion() == null) {
				funcion.setPadre(null);
			}
			Modulo m = null;
			Optional<Modulo> posibleModulo = modulosRol.stream().filter(modulo -> (funcion.getModulo().getIdModulo().equals(modulo.getIdModulo()))).findFirst();
			if (!posibleModulo.isPresent()) {
				m = new Modulo();
				m.setAplicacion(app);
				m.setIdModulo(funcion.getModulo().getIdModulo());
				m.setFunciones(new ArrayList<Funcion>());
				modulosRol.add(m);
			} else {
				m = posibleModulo.get();
			}
			funcion.setModulo(m);
			m.getFunciones().add(funcion);
		});
		modulosRol.forEach(modulo -> {
			modulo.setFunciones(Funcion.jerarquizaFunciones(modulo, modulo.getFunciones()));
		});
		return modulosRol;
	}
}