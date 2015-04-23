package mx.com.gunix.ui.view;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import mx.com.gunix.framework.domain.Usuario;
import mx.com.gunix.framework.service.ActivitiService;
import mx.com.gunix.framework.ui.component.Header;
import mx.com.gunix.framework.ui.view.SecuredView;
import mx.com.gunix.ui.MainUI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.spring.navigator.annotation.VaadinView;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@VaadinView(name="",ui=MainUI.class)
public class MenuView extends VerticalLayout implements SecuredView{
	
	@Autowired
	@Lazy
	ActivitiService as;
	
	private static final long serialVersionUID = 1;
	private String[] themes = { "valo", "tests-valo-blueprint","tests-valo-dark","tests-valo-facebook","tests-valo-flat","tests-valo-flatdark","tests-valo-light","tests-valo-metro"};
	private TabSheet aplicacionesTab;
	private Label userIdLabel;
	
	@PostConstruct
	private void postConstruct() {
		setSizeFull();
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
	        themePicker.addValueChangeListener(vchev->{
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
		
		u.getAplicaciones()
					.stream()
					.forEach(aplicacion->{
						aplicacionesTab.addTab(new Header(as,aplicacion), aplicacion.getDescripcion());
					});
		

		addComponent(aplicacionesTab);
		setExpandRatio(aplicacionesTab, 1.0f);
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
	}

}