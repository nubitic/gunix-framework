package mx.com.gunix.framework.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.josso.gateway.GatewayServiceLocator;
import org.josso.gateway.WebserviceGatewayServiceLocator;
import org.josso.selfservices.password.PasswordGenerator;
import org.josso.selfservices.password.generator.PasswordGeneratorImpl;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider;
import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.BasicSecurityConfiguration;
import org.opensaml.xml.signature.SignatureConstants;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLBootstrap;
import org.springframework.security.saml.SAMLDiscovery;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.SAMLLogoutFilter;
import org.springframework.security.saml.SAMLLogoutProcessingFilter;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.SAMLWebSSOHoKProcessingFilter;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.processor.HTTPArtifactBinding;
import org.springframework.security.saml.processor.HTTPPAOS11Binding;
import org.springframework.security.saml.processor.HTTPPostBinding;
import org.springframework.security.saml.processor.HTTPRedirectDeflateBinding;
import org.springframework.security.saml.processor.HTTPSOAP11Binding;
import org.springframework.security.saml.processor.SAMLBinding;
import org.springframework.security.saml.processor.SAMLProcessorImpl;
import org.springframework.security.saml.websso.ArtifactResolutionProfile;
import org.springframework.security.saml.websso.ArtifactResolutionProfileImpl;
import org.springframework.security.saml.websso.SingleLogoutProfile;
import org.springframework.security.saml.websso.WebSSOProfile;
import org.springframework.security.saml.websso.WebSSOProfileConsumer;
import org.springframework.security.saml.websso.WebSSOProfileConsumerHoKImpl;
import org.springframework.security.saml.websso.WebSSOProfileConsumerImpl;
import org.springframework.security.saml.websso.WebSSOProfileECPImpl;
import org.springframework.security.saml.websso.WebSSOProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfileOptions;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.RequestCacheAwareFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.hunteron.core.Context;

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
import mx.com.gunix.framework.ui.springmvc.saml.DataBaseSAMLUserDetailsService;
import mx.com.gunix.framework.ui.springmvc.saml.logoutprofile.SingleLogoutProfileWSO2Impl;
import mx.com.gunix.framework.ui.springmvc.saml.logoutprofile.TomcatSPWSO2IDPSecurityContextLogoutHandler;
import mx.com.gunix.framework.ui.vaadin.VaadinUtils;

@Configuration
@ComponentScan({"mx.com.gunix.framework.security" ,"org.springframework.security.saml"})
@EnableWebSecurity
public abstract class AbstractSecurityConfig extends WebSecurityConfigurerAdapter implements InitializingBean{
	private final String REMEMBER_ME_KEY = UUID.randomUUID().toString();
	
	public static final String JOSSO_SSO_IMPLEMENTATION = "josso";
	public static final String SAML_SSO_IMPLEMENTATION = "saml";
	
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
		return mx.com.gunix.framework.util.Utils.passwordEncoder();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		if (!Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get())) {
			auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
		} else {
			if(Context.VIEW_SSO_IMPLEMENTATION.get().equals(JOSSO_SSO_IMPLEMENTATION)) {
				auth.authenticationProvider(jossoAuthenticationProvider(gatewayServiceLocator()));
			}else {
				if (Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
					auth.authenticationProvider(samlAuthenticationProvider(userDetailsService()));
				}
			}
		}
	}
	
	@Bean
	public AuthenticationProvider jossoAuthenticationProvider(GatewayServiceLocator gatewayServiceLocator) throws Exception {
		JOSSOAuthenticationProvider jap = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(JOSSO_SSO_IMPLEMENTATION)) {
			jap = new JOSSOAuthenticationProvider(Context.VIEW_SSO_PARTNER_ID.get());
			jap.setGatewayServiceLocator(gatewayServiceLocator);
			jap.setUserDetailsService(userDetailsService());
		}
		return jap;
	}

	@Bean
	public AuthenticationEntryPoint jossoProcessingFilterEntryPoint() {
		JOSSOProcessingFilterEntryPoint jpfep = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(JOSSO_SSO_IMPLEMENTATION)) {
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
		
		if(Boolean.parseBoolean(Context.VIEW_ENABLE_ANONYMOUS.get())) {
			Usuario anonymous = usuarioService.getAnonymous();
			
			if(anonymous!=null) {
				http.anonymous()
					.principal(new UserDetails(anonymous));
			}
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
		
		if(!Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get())) {
			http
				.rememberMe()
					.rememberMeServices(rememberMeService())
					.key(REMEMBER_ME_KEY)
				.and()
				.exceptionHandling()
					.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint(doConfigure(http)));
		}else {
			if(Context.VIEW_SSO_IMPLEMENTATION.get().equals(JOSSO_SSO_IMPLEMENTATION)) {
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
			}else {
				if(Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
					http
						.httpBasic().authenticationEntryPoint(samlEntryPoint())
					.and()
						.addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class)
						.addFilterAfter(samlFilter(), BasicAuthenticationFilter.class)
						.sessionManagement()
						.maximumSessions(1)
						.expiredUrl(SAMLLogoutFilter.FILTER_URL);
					doConfigure(http);
				}	
			}
		}
	}

	@Bean
	public Filter jossoSessionPingFilter() throws Exception {
		JOSSOSessionPingFilter jspf = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(JOSSO_SSO_IMPLEMENTATION)) {
			jspf = new JOSSOSessionPingFilter(Context.VIEW_SSO_PARTNER_ID.get());
			jspf.setGatewayServiceLocator(gatewayServiceLocator());
			jspf.setLogoutUrl("/logout");
		}
		return jspf;
	}
	
	@Bean
	public Filter jossoSessionCookieProcessingFilter() throws Exception {
		JOSSOSessionCookieProcessingFilter jscpf = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(JOSSO_SSO_IMPLEMENTATION)) {
			jscpf = new JOSSOSessionCookieProcessingFilter();
			jscpf.setAuthenticationManager(authenticationManagerBean());
		}
		return jscpf;
	}

	@Bean
	public GatewayServiceLocator gatewayServiceLocator() {
		GatewayServiceLocator gsl = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(JOSSO_SSO_IMPLEMENTATION)) {
			gsl = new WebserviceGatewayServiceLocator();
			gsl.setTransportSecurity("https".equalsIgnoreCase(Context.VIEW_SSO_GATEWAY_ENDPOINT_TRANSPORT_PROTOCOL.get()) ? "confidential" : "none");
			gsl.setEndpoint(Context.VIEW_SSO_GATEWAY_ENDPOINT_HOST_PORT.get());
			gsl.setServicesWebContext(Context.VIEW_SSO_GATEWAY_ENDPOINT_WEB_CONTEXT.get());
			gsl.setUsername("wsclient");
			gsl.setPassword(Context.VIEW_SSO_GATEWAY_ENDPOINT_PASSWORD.get());
		}
		return gsl;
	}

	@Bean
	public Filter jossoAuthenticationProcessingFilter() throws Exception {
		JOSSOAuthenticationProcessingFilter japf = null;
		if(Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(JOSSO_SSO_IMPLEMENTATION)) {
			japf = new JOSSOAuthenticationProcessingFilter(Context.VIEW_SSO_PARTNER_ID.get());
			japf.setAuthenticationManager(authenticationManagerBean());
			japf.setGatewayServiceLocator(gatewayServiceLocator());	
			japf.setAuthenticationSuccessHandler(new SavedRequestAwareAuthenticationSuccessHandler() {
						private final Logger log = Logger.getLogger("mx.com.gunix.framework.config.SavedRequestAwareAuthenticationSuccessHandler");
						@Override
						protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
							String targetUrl = super.determineTargetUrl(request, response);
							String selectedTab = request.getParameter(VaadinUtils.SELECTED_APP_TAB_REQUEST_PARAMETER);
							if (selectedTab != null && !"".equals(selectedTab) && !"null".equalsIgnoreCase(selectedTab)) {
								StringBuilder selectedTabParam = new StringBuilder(targetUrl);
								selectedTabParam.append("?");
								selectedTabParam.append(VaadinUtils.SELECTED_APP_TAB_REQUEST_PARAMETER);
								selectedTabParam.append("=");
								selectedTabParam.append(selectedTab);
								targetUrl = selectedTabParam.toString();
							}
							if (log.isDebugEnabled()) {
								log.debug("TargetURL: " + targetUrl);
							}
							return targetUrl;
						}

					});
		}
		return japf;
	}
	
	/* SAML CONFIG (Filtros) */
    @Bean
    public SAMLAuthenticationProvider samlAuthenticationProvider(UserDetailsService userDetailsService) {
        SAMLAuthenticationProvider samlAuthenticationProvider = null;
        if(Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
        	samlAuthenticationProvider = new SAMLAuthenticationProvider();
            samlAuthenticationProvider.setUserDetails(new DataBaseSAMLUserDetailsService((UserDetailsServiceImpl) userDetailsService));
            samlAuthenticationProvider.setForcePrincipalAsString(false);
        }
        return samlAuthenticationProvider;
    }

	@Bean
	public FilterChainProxy samlFilter() throws Exception {
		FilterChainProxy filterChainProxy = null;
		if(Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			List<SecurityFilterChain> chains = new ArrayList<SecurityFilterChain>();
			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(SAMLEntryPoint.FILTER_URL + "/**"), samlEntryPoint()));
			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(SAMLLogoutFilter.FILTER_URL + "/**"), samlLogoutFilter()));
			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(MetadataDisplayFilter.FILTER_URL + "/**"), metadataDisplayFilter()));
			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(SAMLProcessingFilter.FILTER_URL + "/**"), samlWebSSOProcessingFilter()));
			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(SAMLWebSSOHoKProcessingFilter.WEBSSO_HOK_URL + "/**"), samlWebSSOHoKProcessingFilter()));
			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(SAMLLogoutProcessingFilter.FILTER_URL + "/**"), samlLogoutProcessingFilter()));
			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(SAMLDiscovery.FILTER_URL + "/**"), samlIDPDiscovery()));
			filterChainProxy = new FilterChainProxy(chains);
			}
		return filterChainProxy;
	}
	
	@Bean
	public SAMLEntryPoint samlEntryPoint() {
		SAMLEntryPoint samlEntryPoint = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			samlEntryPoint = new SAMLEntryPoint();
			WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
			webSSOProfileOptions.setIncludeScoping(false);
			samlEntryPoint.setDefaultProfileOptions(webSSOProfileOptions);
		}
		return samlEntryPoint;
	}
	
	@Bean
	public SAMLLogoutFilter samlLogoutFilter() {
		SAMLLogoutFilter sAMLLogoutFilter = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			sAMLLogoutFilter = new SAMLLogoutFilter(successLogoutHandler(), new LogoutHandler[] { logoutHandler() }, new LogoutHandler[] { logoutHandler() });
		}
		return sAMLLogoutFilter;
	}
	
	@Bean
	public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
		SimpleUrlLogoutSuccessHandler successLogoutHandler = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			successLogoutHandler = new SimpleUrlLogoutSuccessHandler();
			successLogoutHandler.setDefaultTargetUrl(Context.VIEW_SSO_SAML_SUCCESS_LOGOUT.get());
		}

		return successLogoutHandler;
	}

	@Bean
	public SecurityContextLogoutHandler logoutHandler() {
		SecurityContextLogoutHandler logoutHandler = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			logoutHandler = new TomcatSPWSO2IDPSecurityContextLogoutHandler(usuarioService);
			logoutHandler.setInvalidateHttpSession(false);
		}
		return logoutHandler;
	}

	@Bean
	public MetadataDisplayFilter metadataDisplayFilter() throws MetadataProviderException, IOException, XMLParserException, ResourceException {
		MetadataDisplayFilter filter = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			filter = new MetadataDisplayFilter();
		}
		return filter;
	}
	
	@Bean
	@Qualifier("metadata")
	public CachingMetadataManager metadata() throws MetadataProviderException, IOException, XMLParserException, ResourceException {
		CachingMetadataManager cachingMetadataManager = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			List<MetadataProvider> providers = new ArrayList<MetadataProvider>();
			providers.add(ssoCircleExtendedMetadataProvider());
			cachingMetadataManager = new CachingMetadataManager(providers);
			cachingMetadataManager.setDefaultIDP(Context.VIEW_SSO_SAML_IPID.get());
		}
		return cachingMetadataManager;
	}
	
	@Bean
	@Qualifier("idp-ssocircle")
	public ExtendedMetadataDelegate ssoCircleExtendedMetadataProvider() throws MetadataProviderException, IOException, XMLParserException, ResourceException {
		ExtendedMetadataDelegate extendedMetadataDelegate = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			ResourceBackedMetadataProvider httpMetadataProvider = new ResourceBackedMetadataProvider(new Timer(), new ClasspathResource(Context.VIEW_SSO_SAML_METADATA_FILE.get()));
			httpMetadataProvider.setParserPool(parserPool());
			extendedMetadataDelegate = new ExtendedMetadataDelegate(httpMetadataProvider, new ExtendedMetadata());
		}
		
		return extendedMetadataDelegate;
	}

	@Bean
	public StaticBasicParserPool parserPool() throws XMLParserException {
		StaticBasicParserPool staticBasicParserPool = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			staticBasicParserPool = new StaticBasicParserPool();
			Map<String, Boolean> featuresMap = new HashMap<String, Boolean>();
			featuresMap.put("http://apache.org/xml/features/dom/defer-node-expansion", Boolean.FALSE);
			staticBasicParserPool.setBuilderFeatures(featuresMap);
			staticBasicParserPool.initialize();
		}
		return staticBasicParserPool;
	}

	@Bean(name = "parserPoolHolder")
	public ParserPoolHolder parserPoolHolder() {
		ParserPoolHolder parserPoolHolder = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			parserPoolHolder = new ParserPoolHolder();
		}
		return parserPoolHolder;
	}

	@Bean
	public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
		SAMLProcessingFilter samlWebSSOProcessingFilter = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			samlWebSSOProcessingFilter = new SAMLProcessingFilter();
			samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManager());
			samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
			samlWebSSOProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
		}
		return samlWebSSOProcessingFilter;
	}

	@Bean
	public SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {
		SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			successRedirectHandler = new SavedRequestAwareAuthenticationSuccessHandler();
			successRedirectHandler.setDefaultTargetUrl(Context.VIEW_SSO_SAML_SUCCESS_PAGE.get());
		}
		return successRedirectHandler;
	}

	@Bean
	public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
		SimpleUrlAuthenticationFailureHandler failureHandler = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			failureHandler = new SimpleUrlAuthenticationFailureHandler();
			failureHandler.setUseForward(true);
		}
		return failureHandler;
	}
	
	@Bean
	public SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter() throws Exception {
		SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			samlWebSSOHoKProcessingFilter = new SAMLWebSSOHoKProcessingFilter();
			samlWebSSOHoKProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
			samlWebSSOHoKProcessingFilter.setAuthenticationManager(authenticationManager());
			samlWebSSOHoKProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
		}
		return samlWebSSOHoKProcessingFilter;
	}

	@Bean
	public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
		SAMLLogoutProcessingFilter sAMLLogoutProcessingFilter = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			sAMLLogoutProcessingFilter = new SAMLLogoutProcessingFilter(successLogoutHandler(), logoutHandler());
		}
		return sAMLLogoutProcessingFilter;
	}
	
	@Bean
	public SAMLDiscovery samlIDPDiscovery() {
		SAMLDiscovery idpDiscovery = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			idpDiscovery = new SAMLDiscovery();
		}
		return idpDiscovery;
	}
	
	@Bean
	public MetadataGeneratorFilter metadataGeneratorFilter() {
		MetadataGeneratorFilter metadataGeneratorFilter = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			MetadataGenerator metadataGenerator = new MetadataGenerator();
            ExtendedMetadata extendedMetadata = new ExtendedMetadata();
			extendedMetadata.setIdpDiscoveryEnabled(false);
            metadataGenerator.setExtendedMetadata(extendedMetadata);
            metadataGenerator.setKeyManager(keyManager()); 
			metadataGeneratorFilter = new MetadataGeneratorFilter(metadataGenerator);
		}
		return metadataGeneratorFilter;
	}

	/* SAML Parte 2 (Bindings) */
	@Bean
	public SAMLProcessorImpl processor() throws Exception {
		SAMLProcessorImpl sAMLProcessorImpl = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			Collection<SAMLBinding> bindings = new ArrayList<SAMLBinding>();
			bindings.add(redirectBinding());
			bindings.add(postBinding());
			bindings.add(artifactBinding(parserPool(), velocityEngine()));
			bindings.add(soapBinding());
			bindings.add(paosBinding());
			sAMLProcessorImpl = new SAMLProcessorImpl(bindings);
		}
		return sAMLProcessorImpl;
	}
    
	@Bean
	public HTTPRedirectDeflateBinding redirectBinding() throws XMLParserException {
		HTTPRedirectDeflateBinding hTTPRedirectDeflateBinding = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			hTTPRedirectDeflateBinding = new HTTPRedirectDeflateBinding(parserPool());
		}
		return hTTPRedirectDeflateBinding;
	}
	
	@Bean
	public HTTPPostBinding postBinding() throws Exception {
		HTTPPostBinding hTTPPostBinding = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			hTTPPostBinding = new HTTPPostBinding(parserPool(), velocityEngine());
		}
		return hTTPPostBinding;
	}
	
	@Bean
	public VelocityEngine velocityEngine() throws Exception {
		VelocityEngine velocityEngine = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			velocityEngine = new VelocityEngine();
            velocityEngine.setProperty(RuntimeConstants.ENCODING_DEFAULT, "UTF-8");
            velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            velocityEngine.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            velocityEngine.setProperty("runtime.log.logsystem.class","org.apache.velocity.runtime.log.NullLogChute");
            velocityEngine.init();
		}
		return velocityEngine;
	}
	
	@Bean
	public HTTPArtifactBinding artifactBinding(ParserPool parserPool, VelocityEngine velocityEngine) throws XMLParserException {
		HTTPArtifactBinding hTTPArtifactBinding = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			hTTPArtifactBinding = new HTTPArtifactBinding(parserPool, velocityEngine, artifactResolutionProfile());
		}
		return hTTPArtifactBinding;
	}

	private ArtifactResolutionProfile artifactResolutionProfile() throws XMLParserException {
		ArtifactResolutionProfileImpl artifactResolutionProfile = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			artifactResolutionProfile = new ArtifactResolutionProfileImpl(new HttpClient(new MultiThreadedHttpConnectionManager()));
			artifactResolutionProfile.setProcessor(new SAMLProcessorImpl(soapBinding()));
		}
		return artifactResolutionProfile;
	}
	
	@Bean
	public HTTPSOAP11Binding soapBinding() throws XMLParserException {
		HTTPSOAP11Binding hTTPSOAP11Binding = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			hTTPSOAP11Binding = new HTTPSOAP11Binding(parserPool());
		}
		return hTTPSOAP11Binding;
	}
	
	@Bean
	public HTTPPAOS11Binding paosBinding() throws XMLParserException {
		HTTPPAOS11Binding hTTPPAOS11Binding = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			hTTPPAOS11Binding = new HTTPPAOS11Binding(parserPool());
		}
		return hTTPPAOS11Binding;
	}

	/* SAML Parte 3 (Seguridad) */
	@Bean
	public KeyManager keyManager() {
		KeyManager keyManager = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			Map<String, String> passwords = new HashMap<String, String>();
			passwords.put(Context.VIEW_SSO_SAML_KEYSTORE_ALIAS.get(), Context.VIEW_SSO_SAML_KEYSTORE_PASS.get());
			keyManager = new JKSKeyManager(new ClassPathResource(Context.VIEW_SSO_SAML_KEYSTORE.get()),Context.VIEW_SSO_SAML_KEYSTORE_PASS.get(),passwords,Context.VIEW_SSO_SAML_KEYSTORE_ALIAS.get()); 
		}
		return keyManager;
	}
	
	/* SAML Parte 4 (profiles) */
	
    @Bean
    public WebSSOProfile webSSOprofile() {
    	WebSSOProfile webSSOProfile = null;
    	if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
    		webSSOProfile = new WebSSOProfileImpl();
    	}
        return webSSOProfile;
    }
    
    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
    	WebSSOProfileConsumerHoKImpl webSSOProfileConsumerHoKImpl = null;
    	if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
    		webSSOProfileConsumerHoKImpl = new WebSSOProfileConsumerHoKImpl();
    	}
        return webSSOProfileConsumerHoKImpl;
    }

    @Bean
    public WebSSOProfileECPImpl ecpprofile() {
    	WebSSOProfileECPImpl webSSOProfileECPImpl = null;
    	if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
    		webSSOProfileECPImpl = new WebSSOProfileECPImpl();
    	}
        return webSSOProfileECPImpl;
    }
    
    @Bean
    public SingleLogoutProfile logoutprofile() {
    	SingleLogoutProfile singleLogoutProfile = null;
    	if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
    		singleLogoutProfile = new SingleLogoutProfileWSO2Impl(usuarioService);
    	}
        return singleLogoutProfile;
    }
    
	/* SAML Parte 5 (consumers) */
    
    @Bean
    public WebSSOProfileConsumer webSSOprofileConsumer() {
    	WebSSOProfileConsumer webSSOProfileConsumer = null;
    	if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
    		webSSOProfileConsumer = new WebSSOProfileConsumerImpl();
    	}
        return webSSOProfileConsumer;
    }
    
	@Bean
	public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
		WebSSOProfileConsumerHoKImpl webSSOProfileConsumerHoKImpl = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			webSSOProfileConsumerHoKImpl = new WebSSOProfileConsumerHoKImpl();
		}
		return webSSOProfileConsumerHoKImpl;
	}
	
	/* SAML Parte 6 (Utils) */
	
    @Bean
    public SAMLContextProviderImpl contextProvider() {
        SAMLContextProviderImpl provider = null;
        if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
        	provider = new SAMLContextProviderImpl();
        }
        return provider;
    }
	
	@Bean
	public static SAMLBootstrap sAMLBootstrap() {
		SAMLBootstrap sAMLBootstrap = null;
		if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
			sAMLBootstrap = new SAMLBootstrap() {
				/*
				 * https://myshittycode.com/2016/02/23/spring-security-saml-replacing-sha-1-with-sha-256-on-signature-and-digest-algorithms/
				 * */
				@Override
				public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
					super.postProcessBeanFactory(beanFactory);
					BasicSecurityConfiguration config = (BasicSecurityConfiguration) org.opensaml.Configuration.getGlobalSecurityConfiguration();
					config.registerSignatureAlgorithmURI("RSA", SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
					config.setSignatureReferenceDigestMethod(SignatureConstants.ALGO_ID_DIGEST_SHA256);
				}
			};
		} else {
			sAMLBootstrap = new SAMLBootstrap() {
				@Override
				public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {}
			};
		}
		return sAMLBootstrap;
	}
    
    @Bean
    public SAMLDefaultLogger samlLogger() {
    	SAMLDefaultLogger sAMLDefaultLogger = null;
    	if (Boolean.parseBoolean(Context.VIEW_ENABLE_SSO.get()) && Context.VIEW_SSO_IMPLEMENTATION.get().equals(SAML_SSO_IMPLEMENTATION)) {
    		sAMLDefaultLogger = new SAMLDefaultLogger();
    		sAMLDefaultLogger.setLogAllMessages(true);
    	}
        return sAMLDefaultLogger;
    }
	
	protected abstract String doConfigure(HttpSecurity http) throws Exception;
}
