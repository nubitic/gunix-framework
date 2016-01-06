package mx.com.gunix.framework.ui.vaadin.view;

import javax.annotation.PostConstruct;

import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.ui.vaadin.component.Header;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.spring.security.VaadinSecurity;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class MainViewLayout extends VerticalLayout implements PopupView.Content{
	@Autowired
	@Lazy
	ApplicationContext applicationContext;
	
	@Autowired
	private VaadinSecurity security;

	private static final long serialVersionUID = 1;
	private TabSheet aplicacionesTab;
	private PopupView userIdLabel;
	String userId;
	private VerticalLayout userDetailsLayout;

	@PostConstruct
	private void postConstruct() {
		setSizeUndefined();
		setWidth("100.0%");
		setSpacing(false);
		setMargin(true);
		addStyleName("MainViewLayout");
		Usuario u = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		userId = u.getIdUsuario();
		
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(false);
		hl.setSpacing(true);
		hl.setWidth("-1px");
		
		userIdLabel = new PopupView(this);
		hl.addComponent(userIdLabel);
		hl.setComponentAlignment(userIdLabel, Alignment.MIDDLE_RIGHT);

		addComponent(hl);
		setComponentAlignment(hl, Alignment.TOP_RIGHT);
		setExpandRatio(hl, 0.0f);
		
		if (u.getAplicaciones().size() > 1) {
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
		} else {
			if (!u.getAplicaciones().isEmpty()) {
				Header h = applicationContext.getBean(Header.class);
				Aplicacion app = u.getAplicaciones().get(0); 
				h.renderHeader(app);
				addComponent(h);
				UI.getCurrent().setNavigator(h.getNavigator());
				setExpandRatio(h, 1.0f);
				Page.getCurrent().setTitle(app.getDescripcion());
				setSpacing(true);
			}
		}
		
		userDetailsLayout = new VerticalLayout();
		Button logoutButton = new Button("Cerrar Sesión");
		logoutButton.addClickListener(clckEvnt->{
			security.logout();
		});
		userDetailsLayout.addComponent(logoutButton);
	}
	
	public void enter(ViewChangeEvent event) {
		Responsive.makeResponsive(this);
	}

	@Override
	public String getMinimizedValueAsHTML() {
		return userId;
	}

	@Override
	public Component getPopupComponent() {
		return userDetailsLayout;
	}
}
