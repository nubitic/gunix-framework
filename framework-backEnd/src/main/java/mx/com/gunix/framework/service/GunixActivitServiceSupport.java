package mx.com.gunix.framework.service;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.log4j.Level;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import mx.com.gunix.framework.domain.Identificador;
import mx.com.gunix.framework.persistence.DescriptorCambios;
import mx.com.gunix.framework.processes.domain.ProgressUpdate;
import mx.com.gunix.framework.util.GunixLogger;
import mx.com.gunix.framework.util.SystemConfigurationParameter;
import mx.com.gunix.framework.util.Utils;

public abstract class GunixActivitServiceSupport<T extends Serializable> {
	private interface FoundAction {
		boolean doFoundAction(Serializable itProp1Obj, Serializable itProp2Obj);
	}

	private static final ThreadLocal<List<Integer>> objetosProcesados = ThreadLocal.<List<Integer>>withInitial(() -> {return new ArrayList<Integer>();});
	private static final Map<Class<?>, Set<Field>> identificadoresCache = new Hashtable<Class<?>, Set<Field>>();
	private static final Map<Class<?>, Set<Field>> validCache = new Hashtable<Class<?>, Set<Field>>();
	private static final Set<Field> EMPTY_FIELD_SET = Collections.unmodifiableSet(new HashSet<Field>());
	private static GunixLogger log;
	
	private static Boolean isGroovyPresent;
	private static Class<?> groovyMetaClass;
	
	@Autowired
	private Validator validator;
	
	@Autowired
	private ActivitiService activitiService;
	
	@Autowired
	MessageSource ms;
	
	
	@Autowired
	SystemConfigParameterService scps;

	protected final void actualizaVariable(Object var) {
		ExecutionEntity ee = Context.getExecutionContext().getExecution();
		ee.setVariable(getVarNameToUpdate(ee, var), var);
	}

	private String getVarNameToUpdate(ExecutionEntity ee, Object var) {
		String varName = null;
		Map<String, VariableInstance> varMap = ee.getVariableInstances();
		if (varMap != null && !varMap.isEmpty()) {
			Optional<VariableInstance> ovie = varMap.values().stream().filter(ivie -> ivie.getCachedValue() == var).findFirst();
			if (ovie.isPresent()) {
				varName = ovie.get().getName();
			}
		}

		if (varName == null) {
			if (ee.getParent() != null) {
				varName = getVarNameToUpdate(ee.getParent(), var);
			} else {
				throw new IllegalArgumentException(
						"No se encontró la variable a actualizar en el contexto de la ejecución actual, ¿será que es nueva y mas bien necesitas 'agregarla' en vez de actualizarla?");
			}
		}
		return varName;
	}

	protected final void agregaVariable(String nombre, Object valor) {
		Context.getExecutionContext().getExecution().setVariable(nombre, valor);
	}

	protected final Set<ConstraintViolation<T>> valida(T serializable, Class<?>... groups) {
		return valida(serializable, null, groups);
	}

	protected final Set<ConstraintViolation<T>> valida(T serializable, String[] fieldsIgnorar, Class<?>... groups) {
		Set<ConstraintViolation<T>> violaciones = validator.validate(serializable, groups);
		findValidAnnotatedFields(serializable.getClass()).forEach(field -> {
			filtraFieldsNoIdentificadores(violaciones, field, "");
		});
		if (fieldsIgnorar != null && fieldsIgnorar.length > 0) {
			filtraFieldsIgnorados(violaciones, fieldsIgnorar);
		}
		return violaciones;
	}

	private void filtraFieldsIgnorados(Set<ConstraintViolation<T>> violaciones, String[] fieldsIgnorar) {
		List<ConstraintViolation<T>> subFieldsViolations = violaciones
				.stream()
				.filter(consViol -> isIgnorado(consViol,fieldsIgnorar))
				.collect(Collectors.toCollection(() -> {return new ArrayList<ConstraintViolation<T>>();}));
		
		subFieldsViolations.forEach(consViol->{
			violaciones.remove(consViol);
		});
	}

	private boolean isIgnorado(ConstraintViolation<T> consViol, String[] fieldsIgnorar) {
		boolean ans = false;

		for (String field : fieldsIgnorar) {
			consViol.getPropertyPath().toString().startsWith(field);
			ans = true;
			break;
		}

		return ans;
	}

	private void filtraFieldsNoIdentificadores(Set<ConstraintViolation<T>> violaciones, Field field, String nestedPath) {
		String actualNestedPath = new StringBuilder(nestedPath).append("".equals(nestedPath)?"":".").append(field.getName()).toString();
		List<ConstraintViolation<T>> subFieldsViolations = violaciones
																	.stream()
																	.filter(consViol -> consViol.getPropertyPath().toString().startsWith(actualNestedPath))
																	.collect(Collectors.toCollection(() -> {return new ArrayList<ConstraintViolation<T>>();}));
		findIdentificadorFields(field.getType())
												.forEach(idField -> {
													String currentFieldPath = new StringBuilder(actualNestedPath).append(".").append(idField.getName()).toString();
													ConstraintViolation<T> foundConsViol=null;
													for (ConstraintViolation<T> consViol : subFieldsViolations) {
														if (consViol.getPropertyPath().toString().equals(currentFieldPath)) {
															foundConsViol=consViol;
															break;
														}
													}
													if (foundConsViol!=null) {
														subFieldsViolations.remove(foundConsViol);
													}
												});
		subFieldsViolations.forEach(consViol->{
			violaciones.remove(consViol);
		});
	}

	protected final List<String> toStringList(Set<ConstraintViolation<T>> errores) {
		List<String> erroresList = new ArrayList<String>();
		errores.forEach(consViol -> {
			erroresList.add(new StringBuilder(consViol.getPropertyPath().toString()).append(": ").append(consViol.getMessage()).toString());
		});
		return erroresList;
	}
	
	protected final Serializable $(String nombreVariable) {
		return (Serializable) Context.getExecutionContext().getExecution().getVariable(nombreVariable);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean doIterables(DescriptorCambios cambios, PropertyUtilsBean propUtils, String propertyName, Iterable itProp1, Iterable itProp2, String lista, FoundAction fa) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		boolean hayCambios = false;
		Iterator<Serializable> itProp1It = itProp1.iterator();
		Iterator<Serializable> itProp2It = itProp2.iterator();

		while (itProp1It.hasNext()) {
			Serializable itProp1Obj = itProp1It.next();
			List idObj1 = getObjectId(itProp1Obj, propUtils);
			boolean objt1Found = false;

			while (itProp2It.hasNext()) {
				Serializable itProp2Obj = itProp2It.next();
				List idObj2 = getObjectId(itProp2Obj, propUtils);

				if ((!idObj1.isEmpty() && idObj1.equals(idObj2)) || (itProp1Obj.equals(itProp2Obj))) {
					if (fa != null) {
						hayCambios = (fa.doFoundAction(itProp1Obj, itProp2Obj) || hayCambios);
					}
					objt1Found = true;
					break;
				}
			}
			if (!objt1Found) {
				aseguraInicializacionListas(cambios, propertyName);
				if ("eliminaciones".equals(lista)) {
					cambios.getEliminaciones().get(propertyName).add(itProp1Obj);
				} else {
					if ("inserciones".equals(lista)) {
						cambios.getInserciones().get(propertyName).add(itProp1Obj);
					} else {
						throw new IllegalArgumentException("No se reconoce la lista " + lista);
					}
				}
				hayCambios = true;
			}
			itProp2It = itProp2.iterator();
		}
		return hayCambios;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected final <P extends Serializable> DescriptorCambios extraeCambios(P original, P nuevo) {
		DescriptorCambios cambios = new DescriptorCambios();
		boolean hayCambios = false;
		List<Integer> procesados;
		boolean isFirstIt = false;
		if (!(procesados = objetosProcesados.get()).contains(original.hashCode())) {
			isFirstIt = procesados.isEmpty();
			procesados.add(original.hashCode());
			Class<P> clazz = (Class<P>) original.getClass();

			if (isGroovyPresent == null && clazz != null) {
				try {
					groovyMetaClass = Class.forName("groovy.lang.MetaClass");
					isGroovyPresent = Boolean.TRUE;
				} catch (ClassNotFoundException ignorar) {
					isGroovyPresent = Boolean.FALSE;
				}
			}

			if (groovyMetaClass != null) {
				if (groovyMetaClass.isAssignableFrom(clazz)) {
					return null;
				}
			}
			
			try {
				cambios.setCambios(new HashMap<String, Serializable>());
				BeanMap map = new BeanMap(original);

				PropertyUtilsBean propUtils = new PropertyUtilsBean();

				for (Object propNameObject : map.keySet()) {
					String propertyName = (String) propNameObject;
					if ("timeStampHash".equals(propertyName) || "class".equals(propertyName)) {
						continue;
					}
					Serializable property1;
					try {
						List<Serializable> eliminaciones = null;
						List<Serializable> inserciones = null;
						Class<?> propClazz = propUtils.getPropertyType(original, propertyName);
						if(propClazz==null){
							propClazz = propUtils.getPropertyType(nuevo, propertyName);
						}
						if (groovyMetaClass != null) {
							if (groovyMetaClass.isAssignableFrom(propClazz)) {
								continue;
							}
						}
						
						property1 = (Serializable) propUtils.getProperty(original, propertyName);
						Serializable property2 = (Serializable) propUtils.getProperty(nuevo, propertyName);
						if (property1 != null && property2 != null) {
							if (BeanUtils.isSimpleProperty(propClazz)) {
								if (!property1.equals(property2)) {
									cambios.getCambios().put(propertyName, property2);
									hayCambios = true;
								}
							} else {
								if (Iterable.class.isAssignableFrom(propClazz)) {
									Iterable itProp1 = (Iterable) property1;
									Iterable itProp2 = (Iterable) property2;
									Iterator<Serializable> itProp1It = itProp1.iterator();
									Iterator<Serializable> itProp2It = itProp2.iterator();

									if (itProp1It.hasNext() && itProp2It.hasNext()) {
										ArrayList<DescriptorCambios> cambiosList = new ArrayList<DescriptorCambios>();

										if (doIterables(cambios, propUtils, propertyName, itProp1, itProp2, "eliminaciones", (itProp1Obj, itProp2Obj) -> {
											boolean hayChanges = false;

											DescriptorCambios propVal = extraeCambios(itProp1Obj, itProp2Obj);
											if (propVal != null) {
												cambiosList.add(propVal);
												hayChanges = true;
											}
											return hayChanges;
										})) {
											hayCambios = true;
										}
										;

										if (!cambiosList.isEmpty()) {
											cambios.getCambios().put(propertyName, cambiosList);
										}

										if (doIterables(cambios, propUtils, propertyName, itProp2, itProp1, "inserciones", null)) {
											hayCambios = true;
										}
									} else {
										if (itProp1It.hasNext() && !itProp2It.hasNext()) {
											aseguraInicializacionListas(cambios, propertyName);
											eliminaciones = eliminaciones==null?cambios.getEliminaciones().get(propertyName):eliminaciones;
											while (itProp1It.hasNext()) {
												eliminaciones.add(itProp1It.next());
											}
											hayCambios = true;
										} else {
											if (!itProp1It.hasNext() && itProp2It.hasNext()) {
												aseguraInicializacionListas(cambios, propertyName);
												inserciones = inserciones==null?cambios.getInserciones().get(propertyName):inserciones;
												while (itProp2It.hasNext()) {
													inserciones.add(itProp2It.next());
												}
												hayCambios = true;
											}
										}
									}
								} else {
									if (Serializable.class.isAssignableFrom(propClazz) && !Map.class.isAssignableFrom(propClazz)) {
										DescriptorCambios propVal = extraeCambios((Serializable) property1, (Serializable) property2);
										if (propVal != null) {
											cambios.getCambios().put(propertyName, propVal);
											hayCambios = true;
										}
									}
								}
							}
						} else {
							if (property1 != null && property2 == null) {
								hayCambios = true;
								if (BeanUtils.isSimpleProperty(propClazz)) {
									cambios.getCambios().put(propertyName, null);
								} else {
									if (Iterable.class.isAssignableFrom(propClazz)) {
										aseguraInicializacionListas(cambios, propertyName);
										eliminaciones = cambios.getEliminaciones().get(propertyName);
										
										for (Serializable s : (Iterable<Serializable>) property1) {
											eliminaciones.add(s);
										}
									} else {
										if (Serializable.class.isAssignableFrom(propClazz) && !Map.class.isAssignableFrom(propClazz)) {
											cambios.getCambios().put(propertyName, null);
										}
									}
								}
							}else {
								if (property1 == null && property2 != null) {
									hayCambios = true;
									if (BeanUtils.isSimpleProperty(propClazz)) {
										cambios.getCambios().put(propertyName, property2);
									} else {
										if (Iterable.class.isAssignableFrom(propClazz)) {
											aseguraInicializacionListas(cambios, propertyName);
											inserciones = cambios.getInserciones().get(propertyName);
											
											for (Serializable s : (Iterable<Serializable>) property2) {
												inserciones.add(s);
											}
										} else {
											if (Serializable.class.isAssignableFrom(propClazz) && !Map.class.isAssignableFrom(propClazz)) {
												cambios.getCambios().put(propertyName, property2);
											}
										}
									}
								}
							}
						}
					} catch (InvocationTargetException | NoSuchMethodException e) {
						procesados.clear();
						throw new RuntimeException("No se pudo obtener la propiedad " + propertyName + " de la clase " + clazz.getName(), e);
					}
				}

				if (hayCambios) {
					if (cambios != null) {
						cambios.setClazz(clazz);
						cambios.setIdMap(new HashMap<String, Serializable>());
						Set<Field> idProp = findIdentificadorFields(clazz);
						for (Field f : idProp) {
							try {
								cambios.getIdMap().put(f.getName(), (Serializable) propUtils.getProperty(original, f.getName()));
							} catch (InvocationTargetException | NoSuchMethodException e) {
								throw new RuntimeException("No fue posible asignar el identificador para un objeto de la clase " + clazz.getName(), e);
							}
						}
					}
				}
			} catch (IllegalAccessException e) {
				procesados.clear();
				throw new RuntimeException("No fue posible crear una nueva instancia de " + clazz.getName(), e);
			}

		}
		if (isFirstIt) {
			procesados.clear();
		}
		return hayCambios ? cambios : null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List getObjectId(Serializable itPropObj, PropertyUtilsBean propUtils) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Set<Field> idProp = findIdentificadorFields(itPropObj.getClass());
		List idObj = new ArrayList();

		for (Field f : idProp) {
			idObj.add(propUtils.getProperty(itPropObj, f.getName()));
		}
		return idObj;
	}

	private void aseguraInicializacionListas(DescriptorCambios cambios, String propertyName) {
		if (cambios.getEliminaciones() == null) {
			cambios.setEliminaciones(new HashMap<String, List<Serializable>>());
			cambios.getEliminaciones().put(propertyName, new ArrayList<Serializable>());
		} else {
			if (cambios.getEliminaciones().get(propertyName) == null) {
				cambios.getEliminaciones().put(propertyName, new ArrayList<Serializable>());
			}
		}

		if (cambios.getInserciones() == null) {
			cambios.setInserciones(new HashMap<String, List<Serializable>>());
			cambios.getInserciones().put(propertyName, new ArrayList<Serializable>());
		} else {
			if (cambios.getInserciones().get(propertyName) == null) {
				cambios.getInserciones().put(propertyName, new ArrayList<Serializable>());
			}
		}
	}
	
	private Set<Field> findValidAnnotatedFields(Class<?> classs) {
		Set<Field> set = validCache.get(classs);
		if (set == null) {
			set = new HashSet<>(findAnnotatedFields(classs, Valid.class));
			validCache.put(classs, set);
		}
		return set;
	}
	
	private Set<Field> findIdentificadorFields(Class<?> classs) {
		Set<Field> set = identificadoresCache.get(classs);
		if (set == null) {
			set = new HashSet<>(findAnnotatedFields(classs, Identificador.class));
			identificadoresCache.put(classs, set);
		}
		return set;
	}

	private Set<Field> findAnnotatedFields(Class<?> classs, Class<? extends Annotation> annotationClass) {
		Set<Field> set = new HashSet<>();
		Class<?> c = classs;
		while (c != null) {
			for (Field field : c.getDeclaredFields()) {
				if (field.isAnnotationPresent(annotationClass)) {
					set.add(field);
				}
			}
			c = c.getSuperclass();
		}
		return set.isEmpty() ? EMPTY_FIELD_SET : set;
	}
	
	protected void addProgressUpdate(String mensaje, float avance, boolean isCancelado) {
		ProgressUpdate pu = new ProgressUpdate();
		pu.setMensaje(mensaje);
		pu.setProgreso(avance);
		pu.setCancelado(isCancelado);
		pu.setTimeStamp(System.currentTimeMillis());
		pu.setProcessId(Context.getExecutionContext().getExecution().getProcessInstanceId());
		activitiService.addProgressUpdate(pu.getProcessId(), pu);
	}
	
	protected String gMssg(String mKey, String defaultMessage, Object... mArgs) {
		return Utils.procesaMensaje(ms, getClass(), mKey, defaultMessage, mArgs);
	}
	
	private void aseguraLogInicializado(){
		if (log == null) {
			log = GunixLogger.getLogger(getClass());
		}
	}
	
	protected void logD(Supplier<String> mensajeSupplier) {
		aseguraLogInicializado();
		log.log(Level.DEBUG, mensajeSupplier, null);
	}

	protected void logE(Supplier<Throwable> throwableSupplier) {
		aseguraLogInicializado();
		log.log(Level.ERROR, null, throwableSupplier);
	}

	protected void logE(Supplier<String> mensajeSupplier, Supplier<Throwable> throwableSupplier) {
		aseguraLogInicializado();
		log.log(Level.ERROR, mensajeSupplier, throwableSupplier);
	}
	
	protected void log(Level nivel, Supplier<String> mensajeSupplier, Supplier<Throwable> throwableSupplier) {
		aseguraLogInicializado();
		log.log(nivel, mensajeSupplier, throwableSupplier);
	}
	
	protected SystemConfigurationParameter getParametro(String clave){
		return scps.getParameterByKey(clave);
	}
	
	protected List<SystemConfigurationParameter> getAllParametros(){
		return scps.getAllParameters();
	}
	
}
