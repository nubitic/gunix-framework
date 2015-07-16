package mx.com.gunix.framework.ui.vaadin.view;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.processes.domain.Tarea;
import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.service.ActivitiService;
import mx.com.gunix.framework.ui.vaadin.component.Header.TareaActualNavigator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public abstract class AbstractGunixView<S extends Serializable> extends VerticalLayout implements View {

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private BeanFieldGroup<S> fieldGroup;

	protected FormLayout flyt = new FormLayout();

	@SuppressWarnings("unchecked")
	private void initFieldGroup() {
		Type genSuperType = getClass().getGenericSuperclass();
		if (genSuperType instanceof ParameterizedType) {
			Type[] typeArguments = ((ParameterizedType) genSuperType).getActualTypeArguments();
			if (typeArguments.length == 1) {
				Class<S> clazz = ((Class<S>) typeArguments[0]);
				fieldGroup = new BeanFieldGroup<S>(clazz);
				try {
					fieldGroup.setItemDataSource(clazz.newInstance());
				} catch (InstantiationException | IllegalAccessException e) {
					throw new IllegalArgumentException("No fue posible inicializar el ItemDataSource para el fieldGroup con una nueva instancia de " + clazz.getName(), e);
				}
				fieldGroup.bindMemberFields(this);
			}
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
		doConstruct();
		addComponent(flyt);
		initFieldGroup();
	}

	protected final void completaTarea() {
		tarea.setVariables(getVariablesTarea());
		tarea.setComentario(getComentarioTarea());

		if (tarea.getInstancia() != null && tarea.getInstancia().getVariables() != null) {
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
		if (fieldGroup == null) {
			throw new IllegalStateException("No se puede dar commit dado que no se indicó el tipo específico para la clase genérica AbstractGunixView");
		}
		fieldGroup.commit();
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
