package mx.com.gunix.framework.config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.Filter;

import mx.com.gunix.framework.security.PersistentTokenBasedRememberMeServices;
import mx.com.gunix.framework.security.RolAccessDecisionVoter;
import mx.com.gunix.framework.security.UserDetails;
import mx.com.gunix.framework.security.UserDetailsServiceImpl;
import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.security.josso.JOSSOAuthenticationProcessingFilter;
import mx.com.gunix.framework.security.josso.JOSSOAuthenticationProvider;
import mx.com.gunix.framework.security.josso.JOSSOLogoutSuccessHandler;
import mx.com.gunix.framework.security.josso.JOSSOProcessingFilterEntryPoint;
import mx.com.gunix.framework.security.josso.JOSSOSessionCookieProcessingFilter;
import mx.com.gunix.framework.security.josso.JOSSOSessionPingFilter;
import mx.com.gunix.framework.service.UsuarioService;

import org.josso.gateway.GatewayServiceLocator;
import org.josso.gateway.WebserviceGatewayServiceLocator;
import org.josso.selfservices.password.PasswordGenerator;
import org.josso.selfservices.password.generator.PasswordGeneratorImpl;
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
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.RequestCacheAwareFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

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
	
	@Bean
	public PasswordGenerator passwordGenerator(){
		PasswordGeneratorImpl pg = new PasswordGeneratorImpl();
		pg.setPasswordLength(8);
		pg.setUseSimpleRandom(false);
		pg.setSecureRandomAlgorithm("SHA1PRNG");
		pg.setSecureRandomProvider("SUN");
		pg.setGenerateNumerals(false);
		pg.setGenerateCapitalLetters(true);
		pg.setIncludeAmbigousChars(false);
		pg.setIncludeSpecialSymbols(true);
		pg.setRegexStartsNoSmallLetter(false);
		pg.setRegexEndsNoSmallLetter(false);
		pg.setRegexStartsNoUpperLetter(false);
		pg.setRegexEndsNosUpperLetter(false);
		pg.setRegexStartsNoDigit(false);
		pg.setRegexEndsNoDigit(false);
		pg.setRegexStartsNoSymbol(false);
		pg.setRegexEndsNoSymbol(false);
		pg.setRegexOnlyOneCapital(false);
		pg.setRegexOnlyOneSymbol(false);
		pg.setRegexAtLeastTwoSymbols(false);
		pg.setRegexOnlyOneDigit(false);
		pg.setRegexAtLeastTwoDigits(false);
		pg.setMaxAttempts(10000);
		return pg;
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
		return new BCryptPasswordEncoder(16);
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		if (!Boolean.parseBoolean(System.getenv("VIEW_ENABLE_SSO"))) {
			auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
		} else {
			auth.authenticationProvider(jossoAuthenticationProvider(gatewayServiceLocator()));
		}
	}
	
	@Bean
	public AuthenticationProvider jossoAuthenticationProvider(GatewayServiceLocator gatewayServiceLocator) throws Exception {
		JOSSOAuthenticationProvider jap = null;
		if (Boolean.parseBoolean(System.getenv("VIEW_ENABLE_SSO"))) {
			jap = new JOSSOAuthenticationProvider(System.getenv("VIEW_SSO_PARTNER_ID"));
			jap.setGatewayServiceLocator(gatewayServiceLocator);
			jap.setUserDetailsService(userDetailsService());
		}
		return jap;
	}

	@Bean
	public AuthenticationEntryPoint jossoProcessingFilterEntryPoint() {
		JOSSOProcessingFilterEntryPoint jpfep = null;
		if (Boolean.parseBoolean(System.getenv("VIEW_ENABLE_SSO"))) {
			GatewayServiceLocator gsl = gatewayServiceLocator();
			jpfep = new JOSSOProcessingFilterEntryPoint();
			jpfep.setGatewayLoginUrl(new StringBuilder(gsl.getEndpointBase()).append(gsl.getServicesWebContext()).append((gsl.getServicesWebContext() == null || "".equals(gsl.getServicesWebContext())) ? "" : "/").append("signon/login.do").toString());
		}
		return jpfep;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		CharacterEncodingFilter utf8Filter = new CharacterEncodingFilter();
		utf8Filter.setEncoding("UTF-8");
		utf8Filter.setForceEncoding(true);
		http.addFilterBefore(utf8Filter, CsrfFilter.class);
		
		Usuario anonymous = usuarioService.getAnonymous();
		
		if(anonymous!=null) {
			http.anonymous()
				.principal(new UserDetails(anonymous));
		}
		
		http.sessionManagement()
				.sessionFixation()
				.migrateSession()
			.and()
			.csrf()
				.disable()
			.headers()
				.frameOptions()
					.disable();
		
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
		
		if(!Boolean.parseBoolean(System.getenv("VIEW_ENABLE_SSO"))) {
			http
				.rememberMe()
					.rememberMeServices(rememberMeService())
					.key(REMEMBER_ME_KEY)
				.and()
				.exceptionHandling()
					.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint(doConfigure(http)));
		}else {
			GatewayServiceLocator gsl = gatewayServiceLocator();
			http
				.addFilterBefore(jossoAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(jossoSessionCookieProcessingFilter(), RememberMeAuthenticationFilter.class)
				.addFilterBefore(jossoSessionPingFilter(), AbstractPreAuthenticatedProcessingFilter.class)
				.logout()
					.addLogoutHandler(new SecurityContextLogoutHandler())
					.logoutSuccessHandler(new JOSSOLogoutSuccessHandler(new StringBuilder(gsl.getEndpointBase()).append(gsl.getServicesWebContext()).append((gsl.getServicesWebContext() == null || "".equals(gsl.getServicesWebContext())) ? "" : "/").append("signon/logout.do").toString(), "/"))
				.and()
				.exceptionHandling()
					.authenticationEntryPoint(jossoProcessingFilterEntryPoint());
			doConfigure(http);
		}
	}

	@Bean
	public Filter jossoSessionPingFilter() throws Exception {
		JOSSOSessionPingFilter jspf = null;
		if (Boolean.parseBoolean(System.getenv("VIEW_ENABLE_SSO"))) {
			jspf = new JOSSOSessionPingFilter(System.getenv("VIEW_SSO_PARTNER_ID"));
			jspf.setGatewayServiceLocator(gatewayServiceLocator());
			jspf.setLogoutUrl("/logout");
		}
		return jspf;
	}
	
	@Bean
	public Filter jossoSessionCookieProcessingFilter() throws Exception {
		JOSSOSessionCookieProcessingFilter jscpf = null;
		if (Boolean.parseBoolean(System.getenv("VIEW_ENABLE_SSO"))) {
			jscpf = new JOSSOSessionCookieProcessingFilter();
			jscpf.setAuthenticationManager(authenticationManagerBean());
		}
		return jscpf;
	}

	@Bean
	public GatewayServiceLocator gatewayServiceLocator() {
		GatewayServiceLocator gsl = null;
		if (Boolean.parseBoolean(System.getenv("VIEW_ENABLE_SSO"))) {
			gsl = new WebserviceGatewayServiceLocator();
			gsl.setTransportSecurity("https".equalsIgnoreCase(System.getenv("VIEW_SSO_GATEWAY_ENDPOINT_TRANSPORT_PROTOCOL")) ? "confidential" : "none");
			gsl.setEndpoint(System.getenv("VIEW_SSO_GATEWAY_ENDPOINT_HOST_PORT"));
			gsl.setServicesWebContext(System.getenv("VIEW_SSO_GATEWAY_ENDPOINT_WEB_CONTEXT"));
			gsl.setUsername("wsclient");
			gsl.setPassword(System.getenv("VIEW_SSO_GATEWAY_ENDPOINT_PASSWORD"));
		}
		return gsl;
	}

	@Bean
	public Filter jossoAuthenticationProcessingFilter() throws Exception {
		JOSSOAuthenticationProcessingFilter japf = null;
		if(Boolean.parseBoolean(System.getenv("VIEW_ENABLE_SSO"))) {
			japf = new JOSSOAuthenticationProcessingFilter(System.getenv("VIEW_SSO_PARTNER_ID"));
			japf.setAuthenticationManager(authenticationManagerBean());
			japf.setGatewayServiceLocator(gatewayServiceLocator());	
		}
		return japf;
	}

	protected abstract String doConfigure(HttpSecurity http) throws Exception;
}
