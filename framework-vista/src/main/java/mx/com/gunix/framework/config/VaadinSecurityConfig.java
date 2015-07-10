package mx.com.gunix.framework.config;

import java.util.UUID;

import mx.com.gunix.framework.security.PersistentTokenBasedRememberMeServices;
import mx.com.gunix.framework.security.UserDetails;
import mx.com.gunix.framework.security.UserDetailsServiceImpl;
import mx.com.gunix.framework.security.Utils;
import mx.com.gunix.framework.service.UsuarioService;
import mx.com.gunix.framework.ui.vaadin.spring.security.GenericVaadinSecurity;
import mx.com.gunix.framework.ui.vaadin.spring.security.SecuredViewProviderAccessDelegate;
import mx.com.gunix.framework.ui.vaadin.spring.security.VaadinAuthenticationFailureHandler;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.RequestCacheAwareFilter;
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
@ComponentScan({ "mx.com.gunix.ui.vaadin", "mx.com.gunix.framework.ui.vaadin", "mx.com.gunix.framework.security" })
@EnableWebSecurity
@EnableVaadinSecurity
public class VaadinSecurityConfig extends WebSecurityConfigurerAdapter implements InitializingBean {
	private final String REMEMBER_ME_KEY = UUID.randomUUID().toString();
	@Autowired
	private VaadinSecurityContext vaadinSecurityContext;

	@Autowired
	@Lazy
	UsuarioService usuarioService;
	
	private PersistentTokenBasedRememberMeServices ptbrms;
	
	@Bean
	public UserDetailsService userDetailsService(){
		return new UserDetailsServiceImpl();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		vaadinSecurityContext.addAuthenticationSuccessHandler(redirectSaveHandler());
		vaadinSecurityContext.addAuthenticationFailureHandler(new VaadinAuthenticationFailureHandler());
		ptbrms.setTokenRepository(usuarioService);
	}

	@Bean(name = VaadinSecurityConfiguration.Beans.ACCESS_DECISION_MANAGER)
	protected AccessDecisionManager accessDecisionManager() {
		return Utils.buildAccesDecisionManager();
	}

	@Bean(name = Beans.VAADIN_SECURITY)
	VaadinSecurity vaadinSecurity() {
		return new GenericVaadinSecurity();
	}

	@Bean
	public RememberMeServices rememberMeService() {
		ptbrms = new PersistentTokenBasedRememberMeServices(REMEMBER_ME_KEY, userDetailsService());
		return ptbrms;
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
	 * The HttpSessionRequestCache is where the initial request before redirect
	 * to the login is cached so it can be used after successful login
	 */
	@Bean
	public RequestCache requestCache() {
		RequestCache requestCache = new HttpSessionRequestCache();
		return requestCache;
	}

	/*
	 * The RequestCacheAwareFilter is responsible for storing the initial
	 * request
	 */
	@Bean
	public RequestCacheAwareFilter requestCacheAwareFilter() {
		RequestCacheAwareFilter filter = new RequestCacheAwareFilter(requestCache());
		return filter;
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
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.anonymous()
				.principal(new UserDetails(usuarioService.getAnonymous()))
			.and()
				.authorizeRequests()
					.antMatchers("/login/**").permitAll()
					.antMatchers("/public/**").permitAll()
					.antMatchers("/UIDL/**").permitAll()
					.antMatchers("/HEARTBEAT/**").authenticated()
					.antMatchers("/**").authenticated()
					.anyRequest().authenticated()
			.and()
				.rememberMe()
					.rememberMeServices(rememberMeService())
					.key(REMEMBER_ME_KEY)
			.and()
				.sessionManagement()
					.sessionFixation()
					.migrateSession()
			.and()
				.csrf()
					.disable()
				.headers()
					.frameOptions()
						.disable();
		http.exceptionHandling()
			.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"));
	}

	@Bean
	public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
		HiddenHttpMethodFilter hiddenHttpMethodFilter = new HiddenHttpMethodFilter();
		return hiddenHttpMethodFilter;
	}
}
