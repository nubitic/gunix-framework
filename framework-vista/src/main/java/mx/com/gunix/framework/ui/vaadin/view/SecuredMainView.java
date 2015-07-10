package mx.com.gunix.framework.ui.vaadin.view;

import com.vaadin.navigator.View;

import mx.com.gunix.framework.ui.vaadin.MainUI;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;

@GunixVaadinView(tipo = GunixVaadinView.INDEX, ui = MainUI.class)
public class SecuredMainView extends MainViewLayout implements View, SecuredView {
	private static final long serialVersionUID = 1L;

}