package mx.com.gunix.framework.ui.vaadin.view;

import javax.annotation.PostConstruct;

import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.ui.vaadin.component.Header;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class MainViewLayout extends VerticalLayout {
	@Autowired
	@Lazy
	ApplicationContext applicationContext;

	private static final long serialVersionUID = 1;
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

		userIdLabel = new Label();
		userIdLabel.setValue(u.getIdUsuario());
		userIdLabel.setWidth("-1px");
		hl.addComponent(userIdLabel);
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
	
	public void enter(ViewChangeEvent event) {
		Responsive.makeResponsive(this);
	}
}
