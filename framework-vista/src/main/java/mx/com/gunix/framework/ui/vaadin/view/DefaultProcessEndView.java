package mx.com.gunix.framework.ui.vaadin.view;

import java.util.List;

import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Label;

@SuppressWarnings("rawtypes")
@GunixVaadinView
public class DefaultProcessEndView extends AbstractGunixView {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doConstruct() {
		addComponent(new Label("PROCESO TERMINADO CON EXITO!"));
	}

	@Override
	protected List<Variable<?>> getVariablesTarea() {
		return null;
	}

	@Override
	protected String getComentarioTarea() {
		return null;
	}

	@Override
	protected void doEnter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

}
