package mx.com.gunix.framework.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SpringGlobalMethodSecurity extends GlobalMethodSecurityConfiguration {
	
	@Autowired
	@Lazy
	AccessDecisionManager adm;
	
	@Override
	protected AccessDecisionManager accessDecisionManager() {
		return adm;
	}
}
