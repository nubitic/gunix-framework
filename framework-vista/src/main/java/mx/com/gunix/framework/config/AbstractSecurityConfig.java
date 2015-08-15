package mx.com.gunix.framework.config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import mx.com.gunix.framework.security.PersistentTokenBasedRememberMeServices;
import mx.com.gunix.framework.security.RolAccessDecisionVoter;
import mx.com.gunix.framework.security.UserDetails;
import mx.com.gunix.framework.security.UserDetailsServiceImpl;
import mx.com.gunix.framework.service.UsuarioService;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.ExpressionBasedPreInvocationAdvice;
import org.springframework.security.access.prepost.PreInvocationAuthorizationAdviceVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.RequestCacheAwareFilter;

@Configuration
@ComponentScan({"mx.com.gunix.framework.security" })
@EnableWebSecurity
public abstract class AbstractSecurityConfig extends WebSecurityConfigurerAdapter implements InitializingBean{
	private final String REMEMBER_ME_KEY = UUID.randomUUID().toString();
	
	@Autowired
	@Lazy
	UsuarioService usuarioService;
	
	@Autowired
	@Lazy
	private PersistentTokenBasedRememberMeServices ptbrms;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		ptbrms.setTokenRepository(usuarioService);
	}
	
	@Bean
	public UserDetailsService userDetailsService(){
		return new UserDetailsServiceImpl();
	}
	
	protected AccessDecisionManager buildAccessDesicionManager() {
		List<AccessDecisionVoter<? extends Object>> voters = new ArrayList<AccessDecisionVoter<? extends Object>>();
    	ExpressionBasedPreInvocationAdvice expressionAdvice = new ExpressionBasedPreInvocationAdvice();
		expressionAdvice.setExpressionHandler(new DefaultMethodSecurityExpressionHandler());
    	voters.add(new PreInvocationAuthorizationAdviceVoter(expressionAdvice));
    	voters.add(new AuthenticatedVoter());
        voters.add(new RolAccessDecisionVoter());
        return new UnanimousBased(voters);
        }

	@Bean
	public PersistentTokenBasedRememberMeServices rememberMeService() {
		ptbrms = new PersistentTokenBasedRememberMeServices(REMEMBER_ME_KEY, userDetailsService());
		return ptbrms;
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
		doConfigure(http);
	}
	
	protected abstract void doConfigure(HttpSecurity http) throws Exception;
}
