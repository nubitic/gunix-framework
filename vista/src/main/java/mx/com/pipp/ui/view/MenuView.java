package mx.com.pipp.ui.view;

import javax.annotation.PostConstruct;

import mx.com.pipp.framework.ui.component.Header;
import mx.com.pipp.framework.ui.view.SecuredView;
import mx.com.pipp.ui.MainUI;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.navigator.annotation.VaadinView;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.VerticalLayout;

@VaadinView(name="",ui=MainUI.class)
public class MenuView extends VerticalLayout implements SecuredView{

	private static final long serialVersionUID = 1;
	
	@Autowired
	Header header;

	@PostConstruct
	private void postConstruct() {
		setSizeFull();
		setSpacing(true);
		setMargin(true);
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		header.renderHeader();
		addComponent(header);
	}

}