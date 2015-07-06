package mx.com.gunix.framework.ui.vaadin.view;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;

import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.ui.vaadin.PublicUI;
import mx.com.gunix.framework.ui.vaadin.component.Header;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@GunixVaadinView(tipo = GunixVaadinView.INDEX, ui = PublicUI.class)
public class PublicView extends VerticalLayout implements View {
	@Autowired
	@Lazy
	ApplicationContext applicationContext;

	private static final long serialVersionUID = 1;
	private String[] themes = { "valo", "tests-valo-blueprint", "tests-valo-dark", "tests-valo-facebook", "tests-valo-flat", "tests-valo-flatdark", "tests-valo-light", "tests-valo-metro" };
	private TabSheet aplicacionesTab;
	private Label userIdLabel;

	@PostConstruct
	private void postConstruct() {
		setWidth("-1px");
		setHeight("-1px");
		setSpacing(false);
		setMargin(false);
		Usuario u = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(false);
		hl.setSpacing(true);
		hl.setWidth("-1px");
		ComboBox themePicker = new ComboBox();
		themePicker.setValue(UI.getCurrent().getTheme());
		themePicker.addItems(Arrays.asList(themes));
		themePicker.setInputPrompt("Tema ...");
		themePicker.setNullSelectionAllowed(false);
		themePicker.addValueChangeListener(vchev -> {
			String theme = (String) vchev.getProperty().getValue();
			UI.getCurrent().setTheme(theme);
		});
		themePicker.setWidth("-1px");
		addComponent(themePicker);
		userIdLabel = new Label();
		userIdLabel.setValue(u.getIdUsuario());
		userIdLabel.setWidth("-1px");
		hl.addComponent(themePicker);
		hl.addComponent(userIdLabel);
		hl.setComponentAlignment(themePicker, Alignment.MIDDLE_LEFT);
		hl.setComponentAlignment(userIdLabel, Alignment.MIDDLE_RIGHT);

		addComponent(hl);
		setComponentAlignment(hl, Alignment.TOP_RIGHT);
		setExpandRatio(hl, 0.0f);

		// aplicacionesTab
		aplicacionesTab = new TabSheet();
		aplicacionesTab.setImmediate(true);

		u.getAplicaciones().stream().forEach(aplicacion -> {
			Header h = applicationContext.getBean(Header.class);
			h.renderHeader(aplicacion);
			aplicacionesTab.addTab(h, aplicacion.getDescripcion());
			aplicacionesTab.addSelectedTabChangeListener(selectedTab -> {
				UI.getCurrent().setNavigator(((Header) selectedTab.getTabSheet().getSelectedTab()).getNavigator());
			});
		});

		addComponent(aplicacionesTab);
		UI.getCurrent().setNavigator(((Header) aplicacionesTab.getSelectedTab()).getNavigator());
		setExpandRatio(aplicacionesTab, 1.0f);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

}
