package mx.com.gunix.framework.config;

import mx.com.gunix.framework.ui.vaadin.spring.security.GenericVaadinSecurity;
import mx.com.gunix.framework.ui.vaadin.spring.security.SecuredViewProviderAccessDelegate;
import mx.com.gunix.framework.ui.vaadin.spring.security.VaadinAuthenticationFailureHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.vaadin.spring.security.VaadinSecurity;
import org.vaadin.spring.security.VaadinSecurityContext;
import org.vaadin.spring.security.annotation.EnableVaadinSecurity;
import org.vaadin.spring.security.config.VaadinSecurityConfiguration;
import org.vaadin.spring.security.config.VaadinSecurityConfiguration.Beans;
import org.vaadin.spring.security.web.VaadinDefaultRedirectStrategy;
import org.vaadin.spring.security.web.VaadinRedirectStrategy;
import org.vaadin.spring.security.web.authentication.SavedRequestAwareVaadinAuthenticationSuccessHandler;
import org.vaadin.spring.security.web.authentication.VaadinAuthenticationSuccessHandler;

@Configuration
@ComponentScan({ "mx.com.gunix.ui", "mx.com.gunix.framework.ui.vaadin"})
@EnableVaadinSecurity
public class VaadinSecurityConfig  extends AbstractSecurityConfig{

	@Autowired
	private VaadinSecurityContext vaadinSecurityContext;

	@Override
	public void afterPropertiesSet() throws Exception {
		vaadinSecurityContext.addAuthenticationSuccessHandler(redirectSaveHandler());
		vaadinSecurityContext.addAuthenticationFailureHandler(new VaadinAuthenticationFailureHandler());
		super.afterPropertiesSet();
	}

	@Bean(name = VaadinSecurityConfiguration.Beans.ACCESS_DECISION_MANAGER)
	protected AccessDecisionManager accessDecisionManager() {
        return buildAccessDesicionManager();
	}

	@Bean(name = Beans.VAADIN_SECURITY)
	VaadinSecurity vaadinSecurity() {
		return new GenericVaadinSecurity();
	}

	@Bean(name = Beans.AUTHENTICATION_MANAGER)
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	SecuredViewProviderAccessDelegate securedViewProviderAccessDelegate() {
		return new SecuredViewProviderAccessDelegate();
	}


	/*
	 * The VaadinRedirectStategy
	 */
	@Bean
	public VaadinRedirectStrategy vaadinRedirectStrategy() {
		return new VaadinDefaultRedirectStrategy();
	}

	@Bean
	public VaadinAuthenticationSuccessHandler redirectSaveHandler() {
		SavedRequestAwareVaadinAuthenticationSuccessHandler handler = new SavedRequestAwareVaadinAuthenticationSuccessHandler();

		handler.setRedirectStrategy(vaadinRedirectStrategy());
		handler.setRequestCache(requestCache());
		handler.setDefaultTargetUrl("/");
		handler.setTargetUrlParameter("r");

		return handler;
	}

	// TODO Disable SpringSecurityFilterChain DefaultFilters (/css, /jsm
	// /images)
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/VAADIN/**");
	}

	@Bean
	public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
		HiddenHttpMethodFilter hiddenHttpMethodFilter = new HiddenHttpMethodFilter();
		return hiddenHttpMethodFilter;
	}

	@Override
	protected void doConfigure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers("/login/**").permitAll()
				.antMatchers("/public/**").permitAll()
				.antMatchers("/UIDL/**").permitAll()
				.antMatchers("/HEARTBEAT/**").authenticated()
				.antMatchers("/**").authenticated()
				.anyRequest().authenticated();
		http.exceptionHandling()
			.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"));
	}
}
