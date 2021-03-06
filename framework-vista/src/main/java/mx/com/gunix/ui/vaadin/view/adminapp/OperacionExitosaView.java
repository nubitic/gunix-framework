package mx.com.gunix.ui.vaadin.view.adminapp;

import java.util.List;

import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;
import mx.com.gunix.framework.ui.vaadin.view.AbstractGunixView;
import mx.com.gunix.framework.ui.vaadin.view.SecuredView;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Label;

@SuppressWarnings("rawtypes")
@GunixVaadinView
public class OperacionExitosaView extends AbstractGunixView implements SecuredView {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doConstruct() {
		flyt.setCaption(new StringBuilder($("operación").toString()).append(" de Aplicaciones").toString());
		flyt.addComponent(new Label("Operación de " + $("operación") + " exitosa"));
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
