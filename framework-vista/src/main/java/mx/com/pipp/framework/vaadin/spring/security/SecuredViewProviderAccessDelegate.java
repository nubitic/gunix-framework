package mx.com.pipp.framework.vaadin.spring.security;

import org.springframework.context.ApplicationContext;
import org.springframework.security.access.annotation.Secured;
import org.vaadin.spring.security.VaadinSecurity;

import com.vaadin.navigator.View;
import com.vaadin.ui.UI;

public class SecuredViewProviderAccessDelegate extends org.vaadin.spring.security.provider.SecuredViewProviderAccessDelegate {
	private VaadinSecurity security;
	private ApplicationContext applicationContext;
	
	@Override
	public void setVaadinSecurity(VaadinSecurity vaadinSecurity) {
		super.setVaadinSecurity(vaadinSecurity);
		security = vaadinSecurity;
		applicationContext = security.getApplicationContext();
	}

	@Override
	public boolean isAccessGranted(String beanName, UI ui, View view) {
		try {
	        Secured viewSecured = applicationContext.findAnnotationOnBean(beanName, Secured.class);

	        if ( viewSecured == null || !security.hasAccessDecisionManager() ) {
	            return true; // Decision is already done if there is no AccessDecisionManager
	        } else {
	            return security.hasAccessToSecuredObject(view);
	        }
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
