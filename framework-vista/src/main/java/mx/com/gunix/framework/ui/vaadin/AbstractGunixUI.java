package mx.com.gunix.framework.ui.vaadin;

import mx.com.gunix.framework.ui.vaadin.spring.SpringViewProvider;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
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
	}

}
