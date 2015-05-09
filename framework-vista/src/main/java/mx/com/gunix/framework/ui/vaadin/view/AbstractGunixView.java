package mx.com.gunix.framework.ui.vaadin.view;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.processes.domain.Tarea;
import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.service.ActivitiService;
import mx.com.gunix.framework.ui.vaadin.component.Header.TareaActualNavigator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public abstract class AbstractGunixView extends VerticalLayout implements SecuredView {
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
		setId(getClass().getName()+":"+hashCode());
		taNav = (TareaActualNavigator) UI.getCurrent().getNavigator();
		tarea = taNav.getTareaActual();
		doConstruct();
	}

	protected void posicionaBotonCompletaTarea(Button boton) {
		addComponent(boton);
		setComponentAlignment(boton, Alignment.BOTTOM_RIGHT);
	}

	protected final void completaTarea() {
		tarea.setVariables(getVariablesTarea());
		tarea.setComentario(getComentarioTarea());
		Instancia instancia = as.completaTarea(tarea);
		String taskView = null;
		Tarea tareaAct = instancia.getTareaActual();
		if(tareaAct==null||(taskView = tareaAct.getVista()).equals(Tarea.DEFAULT_END_TASK_VIEW)){
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
	
	protected final Serializable getVariable(String nombreVariable){
		Optional<Variable<?>> valor = tarea.getInstancia().getVariables()
							.stream()
							.filter(var-> var.getNombre().equals(nombreVariable))
							.findFirst();
		return valor.isPresent()?valor.get().getValor():null;
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		tarea = taNav.getTareaActual();
		doEnter(event);
	}

	protected abstract void doEnter(ViewChangeEvent event);

	protected abstract void doConstruct();

	protected abstract List<Variable<?>> getVariablesTarea();

	protected abstract String getComentarioTarea();
}
