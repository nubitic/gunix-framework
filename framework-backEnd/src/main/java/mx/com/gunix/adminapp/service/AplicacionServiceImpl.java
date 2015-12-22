package mx.com.gunix.adminapp.service;

import java.io.Serializable;
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
import mx.com.gunix.framework.persistence.DescriptorCambios;
import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.Funcion;
import mx.com.gunix.framework.security.domain.Parametro;
import mx.com.gunix.framework.security.domain.Funcion.Acceso;
import mx.com.gunix.framework.security.domain.Modulo;
import mx.com.gunix.framework.security.domain.Rol;
import mx.com.gunix.framework.service.ACLTypeServiceSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("aplicacionService")
@Transactional(rollbackFor = Exception.class)
@Secured(AuthenticatedVoter.IS_AUTHENTICATED_REMEMBERED)
public class AplicacionServiceImpl extends ACLTypeServiceSupport<Aplicacion> {

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
	protected void doInsert(Aplicacion aplicacion) {
		am.inserta(aplicacion);
		aplicacion.getModulos().forEach(modulo -> {
			doInsert(modulo);
		});
		aplicacion.getRoles().forEach(
				rol -> {
					doInsert(aplicacion, rol);
				});
	}

	private void doInsert(Aplicacion aplicacion, Rol rol) {
		rm.inserta(rol);

		rol.getModulos().forEach(
				modulo -> {
					Modulo modRolOrg = acomodaFuncionesModulos(aplicacion, modulo.getFunciones()).get(0);
					List<Funcion> funcionesPadre = new ArrayList<Funcion>();
					insertaFuncionesRol(rol.getIdRol(), modRolOrg.getFunciones(), toListFuncionesPadre(funcionesPadre, aplicacion.getModulos().get(aplicacion.getModulos().indexOf(modRolOrg)).getFunciones()), true);
				});
	}

	private void doInsert(Modulo modulo) {
		mm.inserta(modulo);
		int orden = 0;
		for (Funcion funcion : modulo.getFunciones()) {
			doInsertFuncion(funcion, orden++);
		}
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

	private void insertaFuncionesRol(String idRol, List<Funcion> funciones, List<Funcion> funcionesPadre, boolean setAcceso) {
		funciones.forEach(funcionSeleccionada -> {
			boolean accesoCompleto = setAcceso ? setAccesoFuncion(funcionSeleccionada, funcionesPadre) : funcionSeleccionada.getAcceso().equals(Acceso.COMPLETO);

			rm.insertaFuncion(idRol, funcionSeleccionada);

			if (!accesoCompleto && funcionSeleccionada.getHijas() != null && !funcionSeleccionada.getHijas().isEmpty()) {
				insertaFuncionesRol(idRol, funcionSeleccionada.getHijas(), funcionesPadre, setAcceso);
			}
		});
	}

	private boolean setAccesoFuncion(Funcion funcionSeleccionada, List<Funcion> funcionesPadre) {
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
				throw new IllegalStateException("Relación función-rol inválida: Se intentó asociar una función con hijas que de acuerdo al módulo correspondiente no tiene hijas");
			}
		} else {
			if (funcionesPadre.contains(funcionSeleccionada) && (funcionSeleccionada.getHijas() == null || funcionSeleccionada.getHijas().isEmpty())) {
				accesoCompleto = true;
				funcionSeleccionada.setAcceso(Acceso.COMPLETO);
			} else {
				funcionSeleccionada.setAcceso(Acceso.PUNTUAL);
			}
		}
		if (accesoCompleto && funcionSeleccionada.getHijas() != null) {
			funcionSeleccionada.getHijas().clear();
		}
		return accesoCompleto;
	}

	private void doInsertFuncion(Funcion funcion, int orden) {
		funcion.setOrden(orden);
		fm.inserta(funcion);
		if (funcion.getParametros() != null) {
			funcion.getParametros().forEach(parametro -> {
				fm.insertaParametro(funcion, parametro);
			});
		}
		if (funcion.getHijas() != null) {
			int ordenHija = 0;
			for (Funcion funcionHija : funcion.getHijas()) {
				doInsertFuncion(funcionHija, ordenHija++);
			}
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

	public List<Aplicacion> getByExample(Boolean esMaestro, Aplicacion aplicacion) {
		List<Aplicacion> apps = null;
		if (!esMaestro) {
			apps = new ArrayList<Aplicacion>();
			apps.add(am.getByidAplicacion(aplicacion.getIdAplicacion()));
			if (!apps.isEmpty()) {
				Aplicacion app = apps.get(0);
				if(app==null) {
					throw new IllegalArgumentException("No se encontró el detalle de la aplicación con id: "+aplicacion.getIdAplicacion());
				}
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
		}else {
			apps = am.getByExample(aplicacion);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void doUpdate(Aplicacion appOr, Aplicacion appNew) {
		updateAccesoFunciones(appOr);
		updateAccesoFunciones(appNew);

		DescriptorCambios dc = extraeCambios(appOr, appNew);
		
		if (dc != null) {
			if (dc.getCambios() != null) {
				// Actualizando las propiedades simples de la Aplicación
				if (dc.getCambios().containsKey("descripcion") || dc.getCambios().containsKey("icono")) {
					am.update(dc);
				}
				// Actualizando los modulos
				if (dc.getCambios().containsKey("modulos")) {
					List<DescriptorCambios> dcMods = (List<DescriptorCambios>) dc.getCambios().get("modulos");
					dcMods.forEach(dcMod -> {
						if(dcMod.getCambios() != null) {
							// Actualizando las propiedades simples del Módulo
							if (dcMod.getCambios().containsKey("descripcion") || dcMod.getCambios().containsKey("icono")) {
								mm.update(dcMod);
							}
							// Actualizando las funciones
							if (dcMod.getCambios().containsKey("funciones")) {
								List<DescriptorCambios> dcFuncs = (List<DescriptorCambios>) dcMod.getCambios().get("funciones");
								updateFunciones(dcFuncs);
							}
							
							if (dcMod.getInserciones() != null) {
								// Agregando las nuevas hijas
								if (dcMod.getInserciones().containsKey("funciones")) {
									List hijas = dcMod.getInserciones().get("funciones");
									doInsertaNuevasFunciones(hijas);
								}
							}
						}
					});
				}
				
				// Actualizando los roles
				if (dc.getCambios().containsKey("roles")) {
					List<DescriptorCambios> dcRoles = (List<DescriptorCambios>) dc.getCambios().get("roles");
					dcRoles.forEach(dcRol -> {
						if (dcRol.getCambios() != null) {
							// Actualizando las propiedades simples del Rol
							if (dcRol.getCambios().containsKey("descripcion") || dcRol.getCambios().containsKey("habilitado")) {
								rm.update(dcRol);
							}
							// Actualizando las funciones
							if (dcRol.getCambios().containsKey("modulos")) {
								List<DescriptorCambios> dcMods = (List<DescriptorCambios>) dcRol.getCambios().get("modulos");
								dcMods.forEach(dcMod -> {
									//Actualizando las funciones existentes
									if (dcMod.getCambios().containsKey("funciones")) {
										List<DescriptorCambios> dcFuncs = (List<DescriptorCambios>) dcMod.getCambios().get("funciones");
										updateFuncionesRol(dcRol, dcFuncs);
									}
									
									if (dcMod.getInserciones() != null) {
										// Agregando las nuevas hijas
										if (dcMod.getInserciones().containsKey("funciones")) {
											List hijas = dcMod.getInserciones().get("funciones");
											insertaFuncionesRol((String) dcRol.getIdMap().get("idRol"), (List<Funcion>) hijas, null, false);
										}
									}
									
									if (dcMod.getEliminaciones() != null) {
										// Eliminando las hijas
										if (dcMod.getEliminaciones().containsKey("funciones")) {
											List hijas = dcMod.getEliminaciones().get("funciones");
											eliminaFuncionesRol((String) dcRol.getIdMap().get("idRol"), (List<Funcion>) hijas);
										}
									}
								});
							}
						}
					});
				}
			}
			
			if (dc.getInserciones() != null) {
				if (dc.getInserciones().containsKey("modulos")) {
					List<Serializable> newModulos = dc.getInserciones().get("modulos");
					newModulos.forEach(modulo -> {
						doInsert((Modulo) modulo);
					});
				}

				if (dc.getInserciones().containsKey("roles")) {
					List<Serializable> newRoles = dc.getInserciones().get("roles");
					newRoles.forEach(rol -> {
						doInsert(appNew, (Rol) rol);
					});
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void updateFuncionesRol(DescriptorCambios dcRol, List<DescriptorCambios> dcFuncs) {
		dcFuncs.forEach(dcFunc -> {
			if (dcFunc.getCambios() != null) {
				// Actualizando las propiedades simples de la función-rol
				if (dcFunc.getCambios().containsKey("acceso")) {
					rm.updateFuncion(dcRol.getIdMap(), dcFunc);
				}

				if (dcFunc.getInserciones() != null) {
					// Agregando las nuevas hijas
					if (dcFunc.getInserciones().containsKey("hijas")) {
						List hijas = dcFunc.getInserciones().get("hijas");
						insertaFuncionesRol((String) dcRol.getIdMap().get("idRol"), (List<Funcion>) hijas, null, false);
					}
				}
				
				if (dcFunc.getEliminaciones() != null) {
					// Eliminando las hijas
					if (dcFunc.getEliminaciones().containsKey("hijas")) {
						List hijas = dcFunc.getEliminaciones().get("hijas");
						eliminaFuncionesRol((String) dcRol.getIdMap().get("idRol"), (List<Funcion>) hijas);
					}
				}

				// Actualizando las hijas de la función
				if (dcFunc.getCambios().containsKey("hijas")) {
					updateFuncionesRol(dcRol, (List<DescriptorCambios>) dcFunc.getCambios().get("hijas"));
				}
			}
		});
	}
	
	private void eliminaFuncionesRol(String idRol, List<Funcion> hijas) {
		hijas.forEach(funcion -> {
			if (funcion.getHijas() != null) {
				eliminaFuncionesRol(idRol, funcion.getHijas());
			}
			rm.deleteFuncion(idRol, funcion);
		});
	}

	@SuppressWarnings("unchecked")
	private void updateFunciones(List<DescriptorCambios> dcFuncs) {
		dcFuncs.forEach(dcFunc -> {
			if(dcFunc.getCambios() != null) {
				// Actualizando las propiedades simples de la función
				if (dcFunc.getCambios().containsKey("titulo") || 
					dcFunc.getCambios().containsKey("descripcion") ||
					dcFunc.getCambios().containsKey("processKey") ||
					dcFunc.getCambios().containsKey("orden") ||
					dcFunc.getCambios().containsKey("horario")) {
					fm.update(dcFunc);
				}
				// Actualizando los parámetros de la función
				if (dcFunc.getCambios().containsKey("parametros")) {
					List<DescriptorCambios> dcParams = (List<DescriptorCambios>) dcFunc.getCambios().get("parametros");
					dcParams.forEach(dcParam -> {
						if (dcParam.getCambios() != null) {
							// Actualizando el valor del parámetro
							if (dcParam.getCambios().containsKey("valor")) {
								fm.updateParametro(dcFunc.getIdMap(), dcParam);
							}
						}
					});
				}

				if (dcFunc.getInserciones() != null) {
					// Agregando los nuevos parámetros
					if (dcFunc.getInserciones().containsKey("parametros")) {
						Funcion f = new Funcion();
						f.setModulo((Modulo) dcFunc.getIdMap().get("modulo"));
						f.setIdFuncion((String) dcFunc.getIdMap().get("idFuncion"));
						List<Serializable> newParametros = dcFunc.getInserciones().get("parametros");
						newParametros.forEach(param -> {
							fm.insertaParametro(f, (Parametro) param);
						});
					}
					// Agregando las nuevas hijas
					if (dcFunc.getInserciones().containsKey("hijas")) {
						doInsertaNuevasFunciones(dcFunc.getInserciones().get("hijas"));
					}
				}

				// Actualizando las hijas de la función
				if (dcFunc.getCambios().containsKey("hijas")) {
					updateFunciones((List<DescriptorCambios>) dcFunc.getCambios().get("hijas"));
				}
			}
		});
	}

	private void doInsertaNuevasFunciones(List<Serializable> newHijas) {
		int orden = 0;
		for (Serializable fHija : newHijas) {
			if (orden == 0) {
				Funcion padre = ((Funcion) fHija).getPadre();
				if (padre != null) {
					if ((orden = padre.getHijas().indexOf(fHija)) > 0) {
						orden = (int) (padre.getHijas().get(orden - 1).getOrden() + 1);
					}
				} else {
					if ((orden = newHijas.indexOf(fHija)) > 0) {
						orden = (int) (((Funcion) newHijas.get(orden - 1)).getOrden() + 1);
					}
				}
			}
			doInsertFuncion((Funcion) fHija, orden++);
		}
	}

	private void updateAccesoFunciones(Aplicacion appNew) {
		appNew.getRoles().forEach(rol -> {
			rol.getModulos().forEach(modulo -> {
				List<Funcion> funcionesPadre = new ArrayList<Funcion>();
				toListFuncionesPadre(funcionesPadre, appNew.getModulos().get(appNew.getModulos().indexOf(modulo)).getFunciones());
				updateAccesoFunciones(modulo.getFunciones(), funcionesPadre);
			});
		});
	}

	private void updateAccesoFunciones(List<Funcion> hijas, List<Funcion> funcionesPadre) {
		hijas.forEach(funcionSeleccionada -> {
			if (!setAccesoFuncion(funcionSeleccionada, funcionesPadre) && funcionSeleccionada.getHijas() != null && !funcionSeleccionada.getHijas().isEmpty()) {
				updateAccesoFunciones(funcionSeleccionada.getHijas(), funcionesPadre);
			}
		});
	}

	@Override
	protected void doDelete(Aplicacion objeto) {
		// TODO Auto-generated method stub
		
	}
}