package mx.com.pipp.ui.view;

import javax.annotation.PostConstruct;

import mx.com.pipp.framework.ui.view.SecuredView;
import mx.com.pipp.ui.MainUI;

import org.vaadin.spring.navigator.annotation.VaadinView;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@VaadinView(name="",ui=MainUI.class)
public class MenuView extends VerticalLayout implements SecuredView{

	private static final long serialVersionUID = 1;

	@PostConstruct
	private void postConstruct() {
		
		setSizeFull();
		setSpacing(true);
		setMargin(true);
		
		addComponent(new Label("<h3>Secured View</h3>", ContentMode.HTML));
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		
	}

}