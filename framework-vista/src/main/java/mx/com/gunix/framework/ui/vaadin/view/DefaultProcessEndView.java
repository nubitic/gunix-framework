package mx.com.gunix.framework.ui.vaadin.view;

import java.util.List;

import mx.com.gunix.framework.processes.domain.Variable;

import org.vaadin.spring.navigator.annotation.VaadinView;

import com.vaadin.ui.Label;

@VaadinView(name = "mx.com.gunix.framework.ui.vaadin.view.DefaultProcessEndView")
public class DefaultProcessEndView extends AbstractGunixView {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doConstruct() {
		addComponent(new Label("PROCESO TERMINADO CON EXITO!"));
	}

	@Override
	protected List<Variable> getVariablesTarea() {
		return null;
	}

	@Override
	protected String getComentarioTarea() {
		return null;
	}

}
