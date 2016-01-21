package mx.com.gunix.framework.ui.vaadin.view;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;

import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.processes.domain.Tarea;
import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.service.ActivitiService;
import mx.com.gunix.framework.ui.vaadin.component.GunixBeanFieldGroup;
import mx.com.gunix.framework.ui.vaadin.component.GunixBeanFieldGroup.OnBeanValidationErrorCallback;
import mx.com.gunix.framework.ui.vaadin.component.Header.TareaActualNavigator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public abstract class AbstractGunixView<S extends Serializable> extends VerticalLayout implements View {
	private static final ThreadLocal<Map<Notification.Type, Set<String>>> notificacionesMap = new ThreadLocal<Map<Notification.Type, Set<String>>>();

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

	private static final long serialVersionUID = 1L;
	@Autowired
	@Lazy
	ApplicationContext applicationContext;

	@Autowired
	@Lazy
	ActivitiService as;

	TareaActualNavigator taNav;
	private Tarea tarea;

	@PostConstruct
	private void postConstruct() {
		setWidth("100%");
		setHeight("100%");
		setSpacing(false);
		setMargin(true);
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
				Class<S> clazz = ((Class<S>) typeArguments[0]);
				fieldGroup = new GunixBeanFieldGroup<S>(clazz);
				try {
					fieldGroup.setItemDataSource(clazz.newInstance());
				} catch (InstantiationException | IllegalAccessException e) {
					throw new IllegalArgumentException("No fue posible inicializar el ItemDataSource para el fieldGroup con una nueva instancia de " + clazz.getName(), e);
				}
			}
		}
	}

	protected final void completaTarea() {
		tarea.setVariables(getVariablesTarea());
		tarea.setComentario(getComentarioTarea());

		if (LOGGER.isDebugEnabled() && tarea.getInstancia() != null && tarea.getInstancia().getVariables() != null) {
			for (Variable<?> var : tarea.getInstancia().getVariables()) {
				LOGGER.debug(var.toString());
			}
		}

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
		Optional<Variable<?>> valor = tarea.getInstancia().getVariables().stream().filter(var -> var.getNombre().equals(nombreVariable)).findFirst();
		return valor.isPresent() ? valor.get().getValor() : null;
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

	protected abstract void doEnter(ViewChangeEvent event);

	protected abstract void doConstruct();

	protected abstract List<Variable<?>> getVariablesTarea();

	protected abstract String getComentarioTarea();
}
