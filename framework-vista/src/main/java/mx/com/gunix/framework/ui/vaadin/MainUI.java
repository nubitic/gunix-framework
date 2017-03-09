package mx.com.gunix.framework.ui.vaadin;

import org.vaadin.spring.annotation.VaadinUI;

import com.vaadin.annotations.Push;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ui.Transport;

@VaadinUI
@Push(value = PushMode.AUTOMATIC, transport = Transport.LONG_POLLING)
public class MainUI extends AbstractGunixUI {
	private static final long serialVersionUID = 1L;

}