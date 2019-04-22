package mx.com.gunix.framework.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
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

import com.hunteron.core.Context;

import mx.com.gunix.framework.ui.vaadin.spring.security.GenericVaadinSecurity;
import mx.com.gunix.framework.ui.vaadin.spring.security.SecuredViewProviderAccessDelegate;
import mx.com.gunix.framework.ui.vaadin.spring.security.VaadinAuthenticationFailureHandler;

@Configuration
@ComponentScan({ "mx.com.gunix.ui", "mx.com.gunix.framework.ui.vaadin"})
@EnableVaadinSecurity
public class VaadinSecurityConfig  extends AbstractSecurityConfig{
	public static final String STATIC_RESOURCES_LOCATION = "/static/";
	public static final String STATIC_RESOURCES_PATTERN = STATIC_RESOURCES_LOCATION + "**"; 
	public static final String VAADIN_LOCATION = "vdn/";
	
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
	public VaadinSecurity vaadinSecurity() {
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
		VaadinDefaultRedirectStrategy redirectStr = new VaadinDefaultRedirectStrategy();
		return redirectStr;
	}

	@Bean
	public VaadinAuthenticationSuccessHandler redirectSaveHandler() {
		SavedRequestAwareVaadinAuthenticationSuccessHandler handler = new SavedRequestAwareVaadinAuthenticationSuccessHandler();
		handler.setRedirectStrategy(vaadinRedirectStrategy());
		return handler;
	}

	// TODO Disable SpringSecurityFilterChain DefaultFilters (/css, /jsm
	// /images)
	@Override
	public void configure(WebSecurity web) throws Exception {
		web
			.ignoring()
				.antMatchers("/VAADIN/**");
	}

	@Bean
	public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
		HiddenHttpMethodFilter hiddenHttpMethodFilter = new HiddenHttpMethodFilter();
		return hiddenHttpMethodFilter;
	}

	@Override
	protected String doConfigure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers("/" + VaadinSecurityConfig.VAADIN_LOCATION + "login/**").permitAll()
				.antMatchers(Context.VIEW_SSO_SAML_SUCCESS_LOGOUT.get()).permitAll()
				.antMatchers(MetadataDisplayFilter.FILTER_URL + "/**").permitAll()
				.antMatchers(SAMLProcessingFilter.FILTER_URL + "/**").permitAll()
				.antMatchers("/" + VaadinSecurityConfig.VAADIN_LOCATION + "public/**").permitAll()
				.antMatchers("/" + VaadinSecurityConfig.VAADIN_LOCATION + "UIDL/**").permitAll()
				.antMatchers(STATIC_RESOURCES_PATTERN).permitAll()
				.anyRequest().authenticated();
		return "/" + VaadinSecurityConfig.VAADIN_LOCATION + "login";
	}
}
