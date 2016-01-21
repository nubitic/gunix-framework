package mx.com.gunix.framework.ui.vaadin;

import mx.com.gunix.framework.ui.vaadin.spring.SpringViewProvider;
import mx.com.gunix.framework.ui.vaadin.view.AbstractGunixView;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.CustomizedSystemMessages;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.server.SystemMessages;
import com.vaadin.server.SystemMessagesInfo;
import com.vaadin.server.SystemMessagesProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.UI;

public abstract class AbstractGunixUI extends UI {
	private static final long serialVersionUID = 1L;
	@Autowired
	private SpringViewProvider viewProvider;

	@Override
	protected void init(VaadinRequest request) {
		Navigator navigator = new Navigator(this, this);
		navigator.addProvider(viewProvider);
		setNavigator(navigator);

		VaadinService.getCurrent().setSystemMessagesProvider(new SystemMessagesProvider() {
			private static final long serialVersionUID = 1L;

			@Override
			public SystemMessages getSystemMessages(SystemMessagesInfo systemMessagesInfo) {
				CustomizedSystemMessages messages = new CustomizedSystemMessages();
				messages.setSessionExpiredCaption("La sesión expiró");
				messages.setSessionExpiredMessage("Tome nota de cualquier dato modificado y de <u>clic aquí</u> o presione la tecla ESC para continuar.");
				
				messages.setCommunicationErrorCaption("Problema con la comunicación");
				messages.setCommunicationErrorMessage("Tome nota de cualquier dato modificado y de <u>clic aquí</u> o presione la tecla ESC para continuar.");
				
				messages.setInternalErrorCaption("Error interno");
				messages.setInternalErrorMessage("Por favor notifique a su administrador.<br/>Tome nota de cualquier dato modificado y de <u>clic aquí</u> o presione la tecla ESC para continuar.");

				messages.setCookiesDisabledCaption("Las cookies etán deshabilitadas");
				messages.setCookiesDisabledMessage("Esta aplicación utiliza cookies para funcionar.<br/>Por favor habilite las cookies en su navegador y de <u>clic aquí</u> o presione la tecla ESC para continuar.");
				return messages;
			}
		});
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		AbstractGunixView.doNotification();
		super.paintContent(target);
	}

}
