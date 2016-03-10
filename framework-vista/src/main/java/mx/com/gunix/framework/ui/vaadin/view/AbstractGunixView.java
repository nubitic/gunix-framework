package mx.com.gunix.framework.ui.vaadin.view;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.processes.domain.Tarea;
import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.service.ActivitiService;
import mx.com.gunix.framework.service.GetterService;
import mx.com.gunix.framework.ui.GunixFile;
import mx.com.gunix.framework.ui.vaadin.component.GunixBeanFieldGroup;
import mx.com.gunix.framework.ui.vaadin.component.GunixBeanFieldGroup.OnBeanValidationErrorCallback;
import mx.com.gunix.framework.ui.vaadin.component.GunixTableFieldFactory;
import mx.com.gunix.framework.ui.vaadin.component.GunixTableFieldFactory.GunixFieldPropertyRel;
import mx.com.gunix.framework.ui.vaadin.component.GunixTableBeanErrorGenerator;
import mx.com.gunix.framework.ui.vaadin.component.GunixUploadField;
import mx.com.gunix.framework.ui.vaadin.component.GunixViewErrorHandler;
import mx.com.gunix.framework.ui.vaadin.component.Header.TareaActualNavigator;
import mx.com.gunix.framework.util.ActivitiGunixFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Responsive;
import com.vaadin.server.UserError;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public abstract class AbstractGunixView<S extends Serializable> extends VerticalLayout implements View {
	private static final ThreadLocal<Map<Notification.Type, Set<String>>> notificacionesMap = new ThreadLocal<Map<Notification.Type, Set<String>>>();
	private Class<S> clazz;
	private Map<String, Serializable> varCache = new HashMap<String, Serializable>();
	private static final long serialVersionUID = 1L;
	
	private static final Serializable NULL_OBJECT = new Serializable() {private static final long serialVersionUID = 1L;};
	
	TareaActualNavigator taNav;
	private Tarea tarea;
	
	@Autowired
	@Lazy
	ApplicationContext applicationContext;

	@Autowired
	@Lazy
	ActivitiService as;
	
	@Autowired
	@Lazy
	GetterService gs;
	
	public static void appendNotification(Notification.Type type, String notificacion) {
		Map<Notification.Type, Set<String>> notificaciones = notificacionesMap.get();
		if (notificaciones == null) {
			notificaciones = new HashMap<Notification.Type, Set<String>>();
			notificacionesMap.set(notificaciones);
		}
		
		Set<String> notificacionesPuntuales = notificaciones.get(type);
		if(notificacionesPuntuales == null) {
			notificacionesPuntuales = new LinkedHashSet<String>();
			notificaciones.put(type, notificacionesPuntuales);
		}
		
		notificacionesPuntuales.add(notificacion);
		UI.getCurrent().markAsDirty();
	}
	
	public static void doNotification() {
		Map<Notification.Type, Set<String>> notificaciones = notificacionesMap.get();
		if (notificaciones != null && !notificaciones.isEmpty()) {
			notificaciones.keySet().forEach(type -> {
				Set<String> notificacionesPuntuales = notificaciones.get(type);
				if (notificacionesPuntuales != null && !notificacionesPuntuales.isEmpty()) {
					StringBuilder notificationMessage = new StringBuilder();
					notificacionesPuntuales.forEach(notificacion -> {
						notificationMessage.append(notificacion).append("\n\r");
					});
					try {
						Notification.show(notificationMessage.toString(), type);
					} catch (IllegalStateException ignorar) {

					}
				}
			});
		}
		notificacionesMap.set(null);
	}
	
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private GunixBeanFieldGroup<S> fieldGroup;

	protected FormLayout flyt = new FormLayout();

	private void postInitFieldGroup() {
		if (fieldGroup != null) {
			fieldGroup.bindMemberFields(this);
		}
	}

	@PostConstruct
	private void postConstruct() {
		setWidth("100%");
		setHeight("100%");
		setSpacing(false);
		setMargin(true);
		setErrorHandler(GunixViewErrorHandler.getCurrent());
		setId(new StringBuilder(getClass().getName()).append(":").append(hashCode()).toString());
		taNav = (TareaActualNavigator) UI.getCurrent().getNavigator();
		tarea = taNav.getTareaActual();
		preInitFieldGroup();
		doConstruct();
		addComponent(flyt);
		postInitFieldGroup();
	}

	@SuppressWarnings("unchecked")
	private void preInitFieldGroup() {
		Type genSuperType = getClass().getGenericSuperclass();
		if (genSuperType instanceof ParameterizedType) {
			Type[] typeArguments = ((ParameterizedType) genSuperType).getActualTypeArguments();
			if (typeArguments.length == 1) {
				if(typeArguments[0] instanceof Class) {
					clazz = ((Class<S>) typeArguments[0]);
					fieldGroup = new GunixBeanFieldGroup<S>(clazz);
					try {
						fieldGroup.setItemDataSource(clazz.newInstance());
					} catch (InstantiationException | IllegalAccessException e) {
						throw new IllegalArgumentException("No fue posible inicializar el ItemDataSource para el fieldGroup con una nueva instancia de " + clazz.getName(), e);
					}	
				}
			}
		}
	}
	
	protected final void camposVaciosSonValidos() {
		if (fieldGroup != null) {
			fieldGroup.setRequiredEnabled(false);
		}
	}
	
	protected void initBean(S fuente) {
		if (fuente != null) {
			BeanUtils.copyProperties(fuente, getBean());
		}
	}
	
	protected final void completaTarea() {
		tarea.setVariables(getVariablesTarea());
		tarea.setComentario(getComentarioTarea());

		Instancia instancia = as.completaTarea(tarea);
		String taskView = null;
		Tarea tareaAct = instancia.getTareaActual();
		if (tareaAct == null || (taskView = tareaAct.getVista()).equals(Tarea.DEFAULT_END_TASK_VIEW)) {
			taskView = DefaultProcessEndView.class.getName();
		}

		try {
			taNav.setTareaActual(instancia.getTareaActual());
			taNav.navigateTo(taskView);
		} finally {
			taNav.setTareaActual(null);
		}
	}

	protected final <T extends Component> T getBeanComponent(Class<T> beanClass) {
		return applicationContext.getBean(beanClass);
	}

	protected final Serializable $(String nombreVariable) {
		Serializable s = varCache.get(nombreVariable);
		if (s == null) {
			s = as.getVar(tarea.getInstancia(), nombreVariable);
			if (s == null) {
				varCache.put(nombreVariable, NULL_OBJECT);
			} else {
				varCache.put(nombreVariable, s);
			}
		} else {
			if (s == NULL_OBJECT) {
				s = null;
			}
		}
		return s;
	}

	protected final S getBean() {
		if (fieldGroup == null) {
			throw new IllegalStateException("No se puede obtener el Bean dado que no se indicó el tipo específico para la clase genérica AbstractGunixView");
		}
		return fieldGroup.getItemDataSource().getBean();
	}

	protected final void commit() throws CommitException {
		validaFieldGroup();
		fieldGroup.commit();
	}
	
	protected final void commit(OnBeanValidationErrorCallback<S> onBVECallback) throws CommitException {
		validaFieldGroup();
		fieldGroup.commit(onBVECallback);
	}

	private void validaFieldGroup() {
		if (fieldGroup == null) {
			throw new IllegalStateException("No se puede dar commit dado que no se indicó el tipo específico para la clase genérica AbstractGunixView");
		}
	}

	@Override
	public void enter(ViewChangeEvent event) {
		tarea = taNav.getTareaActual();
		doEnter(event);
		Responsive.makeResponsive(this);
	}
	
	Tarea getTarea() {
		return tarea;
	}
	
	@SuppressWarnings("unchecked")
	protected List<S> getBeans(Table tabla) {
		List<S> beans = new ArrayList<S>();
		if (tabla.getContainerDataSource() == null) {
			throw new IllegalArgumentException("Se requiere que el ContainerDataSource sea diferente a null");
		}
		tabla.getContainerDataSource().getItemIds().forEach(beanId -> {
			beans.add((S) beanId);
		});
		return beans;
	}
	
	protected boolean isValido(Table tabla, S bean) {
		return isValid(tabla, false, bean);
	}
	
	@SuppressWarnings("unchecked")
	private boolean isValid(Table tabla, boolean vacioEsError, S bean){
		Map<String, Boolean> hayErroresHolder = new HashMap<String, Boolean>();
		hayErroresHolder.put("hayErrores", false);
		Container container = tabla.getContainerDataSource();
		if (bean == null && vacioEsError && container.size() == 0) {
			hayErroresHolder.put("hayErrores", true);
			tabla.setComponentError(new UserError("La tabla debe contener al menos un registro"));
		}
		if (bean != null && !container.getItemIds().contains(bean)) {
			throw new IllegalArgumentException("El bean especificado no se encuentra en la tabla");
		}
		tabla.setComponentError(null);
		Map<Field<?>, Property<?>> prevPropDS = new HashMap<Field<?>, Property<?>>();
		GunixBeanFieldGroup<S> bfgf = new GunixBeanFieldGroup<S>(clazz);
		GunixTableFieldFactory grff = (GunixTableFieldFactory) tabla.getTableFieldFactory();
		if (bean == null) {
			container.getItemIds().forEach(beanId -> {
				validaBean(bfgf, (S) beanId, grff, tabla, container, prevPropDS, hayErroresHolder);
			});
		} else {
			validaBean(bfgf, bean, grff, tabla, container, prevPropDS, hayErroresHolder);
		}
		if(hayErroresHolder.get("hayErrores")) {
			tabla.setComponentError(new UserError("La tabla tiene errores"));
		}
		return !hayErroresHolder.get("hayErrores");
	}
	
	@SuppressWarnings("unchecked")
	private void validaBean(GunixBeanFieldGroup<S> bfgf, S beanId, GunixTableFieldFactory grff, Table tabla, Container container, Map<Field<?>, Property<?>> prevPropDS, Map<String, Boolean> hayErroresHolder) {
		bfgf.setItemDataSource(beanId);
		List<GunixFieldPropertyRel> fieldProps = grff.getFieldsBy(tabla, container, beanId);
		fieldProps.forEach(fieldProp -> {
			if (!(GunixViewErrorHandler.getCurrent().isInvalidValueComponent(fieldProp.getField()))) {
				prevPropDS.put(fieldProp.getField(), fieldProp.getField().getPropertyDataSource());
				((AbstractComponent) fieldProp.getField()).setComponentError(null);
				bfgf.bind(fieldProp.getField(), fieldProp.getPropertyId());
			} else {
				hayErroresHolder.put("hayErrores", true);
			}
		});
		try {
			bfgf.commit(ibve -> {
				if ("".equals(ibve.getPropertyPath().toString())) {
					GunixTableBeanErrorGenerator<S> bErrGen = initTableBeanErrorGenerator(tabla);
					bErrGen.addBeanError((S) ibve.getLeafBean(), ibve.getMessage());
				} else {
					tabla.setComponentError(new UserError(ibve.getMessage()));
				}
			});
		} catch (CommitException e) {
			hayErroresHolder.put("hayErrores", true);
			for (Field<?> f : e.getInvalidFields().keySet()) {
				((AbstractComponent) f).setComponentError(new UserError(e.getInvalidFields().get(f).getCauses()[0].getMessage()));
			}
		} finally {
			for (Field<?> f : prevPropDS.keySet()) {
				bfgf.unbind(f);
				f.setPropertyDataSource(prevPropDS.get(f));
				f.setBuffered(false);
				f.setInvalidAllowed(false);
			}
			prevPropDS.clear();
		}
	}

	@SuppressWarnings("unchecked")
	private GunixTableBeanErrorGenerator<S> initTableBeanErrorGenerator(Table tabla) {
		GunixTableBeanErrorGenerator<S> gtbeg = null;
		if (tabla.getItemDescriptionGenerator() == null && tabla.getCellStyleGenerator() == null) {
			gtbeg = new GunixTableBeanErrorGenerator<S>();
			tabla.setItemDescriptionGenerator(gtbeg);
			tabla.setCellStyleGenerator(gtbeg);
		} else {
			if (tabla.getItemDescriptionGenerator() instanceof GunixTableBeanErrorGenerator) {
				gtbeg = (GunixTableBeanErrorGenerator<S>) tabla.getItemDescriptionGenerator();
			}
		}
		return gtbeg;
	}

	protected boolean isValida(Table tabla, boolean vacioEsError) {
		return isValid(tabla, vacioEsError, null);
	}
	
	protected Variable<ActivitiGunixFile> buildGunixFileVariable(String nombreVariable, GunixUploadField uploadField) {
		Variable<ActivitiGunixFile> fileVar = new Variable<ActivitiGunixFile>();
		fileVar.setNombre(nombreVariable);
		GunixFile gf = (GunixFile) uploadField.getValue();
		fileVar.setValor(new ActivitiGunixFile(nombreVariable, gf.getFileName(), gf.getMimeType(), gf.getFile()));
		return fileVar;
	}
	
	protected Serializable get(String uri, Object... args) {
		return gs.get(uri, args);
	}

	protected abstract void doEnter(ViewChangeEvent event);

	protected abstract void doConstruct();

	protected abstract List<Variable<?>> getVariablesTarea();

	protected abstract String getComentarioTarea();
}
