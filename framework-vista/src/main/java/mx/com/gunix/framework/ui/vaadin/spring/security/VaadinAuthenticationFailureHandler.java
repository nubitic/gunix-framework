package mx.com.gunix.framework.ui.vaadin.spring.security;

import org.springframework.security.core.AuthenticationException;

import com.vaadin.ui.Notification;

public class VaadinAuthenticationFailureHandler implements org.vaadin.spring.security.web.authentication.VaadinAuthenticationFailureHandler{

	@Override
	public void onAuthenticationFailure(AuthenticationException exception) throws Exception {
		Notification.show("Usuario y/o Contraseña incorrectos");
	}

}
