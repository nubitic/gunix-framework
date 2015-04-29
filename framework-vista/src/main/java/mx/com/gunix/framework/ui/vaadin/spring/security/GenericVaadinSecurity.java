package mx.com.gunix.framework.ui.vaadin.spring.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

public class GenericVaadinSecurity extends org.vaadin.spring.security.GenericVaadinSecurity {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public boolean hasAccessToSecuredObject(Object securedObject) {
        final Secured secured = AnnotationUtils.findAnnotation(AopUtils.getTargetClass(securedObject), Secured.class);
        Assert.notNull(secured, "securedObject did not have @Secured annotation");
        return hasAccessToObject(securedObject, secured.value());
	}

	@Override
	public boolean hasAccessToObject(Object securedObject, String... securityConfigurationAttributes) {
		final AbstractAccessDecisionManager adm = (AbstractAccessDecisionManager) getAccessDecisionManager();
		final Authentication authentication = getAuthentication();
		if (adm == null || authentication == null || !authentication.isAuthenticated()) {
			if (adm == null) {
				logger.warn("Access was denied to object because there was no AccessDecisionManager set!");
			}
			return false;
		}

		final List<AccessDecisionVoter<? extends Object>> orgVoters = new ArrayList<AccessDecisionVoter<? extends Object>>(adm.getDecisionVoters());
		try {
			final List<AccessDecisionVoter<? extends Object>> selectedVoters = new ArrayList<AccessDecisionVoter<? extends Object>>();
			final Collection<ConfigAttribute> configAttributes = new ArrayList<ConfigAttribute>(securityConfigurationAttributes.length);

			for (AccessDecisionVoter<? extends Object> voter : orgVoters) {
				if (voter.supports(AopUtils.getTargetClass(securedObject))) {
					selectedVoters.add(voter);
				}
			}

			for (String securityConfigString : securityConfigurationAttributes) {
				ConfigAttribute ca = new SecurityConfig(securityConfigString);
				for (AccessDecisionVoter<? extends Object> voter : selectedVoters) {
					if (!configAttributes.contains(ca)&&voter.supports(ca)) {
						configAttributes.add(ca);
					}
				}
			}
			adm.getDecisionVoters().clear();
			adm.getDecisionVoters().addAll(selectedVoters);
			
			try {
				adm.decide(authentication, securedObject, configAttributes);
				return true;
			} catch (AccessDeniedException ex) {
				return false;
			} catch (InsufficientAuthenticationException ex) {
				return false;
			}
		} finally {
			adm.getDecisionVoters().clear();
			adm.getDecisionVoters().addAll(orgVoters);
		}
	}
}
