package mx.com.gunix.framework.ui.vaadin.component;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.data.Validator;
import com.vaadin.server.AbstractErrorMessage;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.ErrorEvent;
import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;

public class GunixViewErrorHandler extends DefaultErrorHandler {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(DefaultErrorHandler.class.getName());
	private static final ThreadLocal<List<Component>> invalidValueComponents = ThreadLocal.<List<Component>> withInitial(() -> {
		return new ArrayList<Component>();
	});

	@Override
	public void error(ErrorEvent event) {
		Throwable t = event.getThrowable();
		if (t instanceof SocketException) {
			// Most likely client browser closed socket
			log.info("SocketException in CommunicationManager." + " Most likely client (browser) closed socket.");
			return;
		}

		t = findRelevantThrowable(t);

		// Finds the original source of the error/exception
		AbstractComponent component = findAbstractComponent(event);
		if (component != null) {
			// Shows the error in AbstractComponent
			ErrorMessage errorMessage = AbstractErrorMessage.getErrorMessageForException(t);
			component.setComponentError(errorMessage);
			if ((t instanceof Validator.InvalidValueException)) {
				invalidValueComponents.get().add(component);
			}
		}
		if (!(t instanceof Validator.InvalidValueException)) {
			log.log(Level.SEVERE, "", t);
		}
	}

	public boolean isInvalidValueComponent(Component component) {
		return invalidValueComponents.get().contains(component);
	}
}
