package mx.com.gunix.ui.vaadin.view.adminapp.usuario;

import java.util.List;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Label;

import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;
import mx.com.gunix.framework.ui.vaadin.view.AbstractGunixView;
import mx.com.gunix.framework.ui.vaadin.view.SecuredView;

@SuppressWarnings("rawtypes")
@GunixVaadinView
public class OpUsuarioExitosaView extends AbstractGunixView implements SecuredView {

	private static final long serialVersionUID = 1L;
	private Usuario usuario;

	@Override
	protected void doEnter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doConstruct() {
		flyt.setCaption(new StringBuilder($("operación").toString()).append(" de Usuario").toString());
		flyt.addComponent(new Label("Operación de " + $("operación") + " exitosa"));
		usuario = (Usuario) $("usuario");
		
		flyt.addComponent(new Label("Usuario : " + usuario.getIdUsuario() + " fue dado de alta."));
		flyt.addComponent(new Label("Password: " + usuario.getPassword()));
	}

	@Override
	protected List<Variable<?>> getVariablesTarea() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getComentarioTarea() {
		// TODO Auto-generated method stub
		return null;
	}

}
