package mx.com.gunix.ui.vaadin.view;

import java.util.List;

import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;
import mx.com.gunix.framework.ui.vaadin.view.AbstractGunixView;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

@GunixVaadinView
public class OperacionExitosaView extends AbstractGunixView {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doConstruct() {
		addComponent(new Label(getVariable("operación")+" Exitosa<\br>Cliente:"+getVariable("cliente"), ContentMode.HTML));
		
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
