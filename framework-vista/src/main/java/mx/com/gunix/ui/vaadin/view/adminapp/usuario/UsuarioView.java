package mx.com.gunix.ui.vaadin.view.adminapp.usuario;

import java.util.List;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;

import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;
import mx.com.gunix.framework.ui.vaadin.view.AbstractGunixView;
import mx.com.gunix.framework.ui.vaadin.view.SecuredView;


@GunixVaadinView
public class UsuarioView extends AbstractGunixView<UsuarioView.UsuarioViewBean> implements SecuredView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static class UsuarioViewBean extends Usuario {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
	
	
	@Override
	protected void doEnter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doConstruct() {
		// TODO Auto-generated method stub
		
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
