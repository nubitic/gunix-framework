package mx.com.gunix.framework.security;

import java.util.Collection;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import com.vaadin.navigator.View;

public class RolAccessDecisionVoter implements AccessDecisionVoter<View> {
	
	@Override
	public boolean supports(ConfigAttribute attribute) {
		return true;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	public int vote(Authentication authentication, View view, Collection<ConfigAttribute> attributes) {
		return ACCESS_GRANTED;
	}

}
