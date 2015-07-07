package mx.com.gunix.framework.ui.vaadin.view;

import mx.com.gunix.framework.ui.vaadin.PublicUI;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;

@GunixVaadinView(tipo = GunixVaadinView.INDEX, ui = PublicUI.class)
public class PublicMainView extends MainViewLayout implements View {
	private static final long serialVersionUID = 1L;

	@Override
	public void enter(ViewChangeEvent event) {
	}

}
