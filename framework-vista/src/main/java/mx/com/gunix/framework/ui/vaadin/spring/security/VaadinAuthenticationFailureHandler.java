package mx.com.gunix.framework.ui.vaadin.spring.security;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;

import com.vaadin.ui.Notification;

public class VaadinAuthenticationFailureHandler implements org.vaadin.spring.security.web.authentication.VaadinAuthenticationFailureHandler {
	private static final Logger logger = LoggerFactory.getLogger(VaadinAuthenticationFailureHandler.class);

	@Override
	public void onAuthenticationFailure(AuthenticationException exception) throws Exception {
		Throwable cause = null;
		if (!((cause = ExceptionUtils.getCause(exception)) instanceof AuthenticationException)) {
			logger.error("Error al autenticar: " + cause != null ? cause.getMessage() : exception.getMessage(), cause != null ? cause : exception);
		}
		Notification.show("Usuario y/o Contraseña incorrectos");
	}

}
