package mx.com.gunix.framework.config;

import java.util.ArrayList;
import java.util.List;

import mx.com.gunix.framework.security.RolAccessDecisionVoter;
import mx.com.gunix.framework.vaadin.spring.security.GenericVaadinSecurity;
import mx.com.gunix.framework.vaadin.spring.security.SecuredViewProviderAccessDelegate;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.ExpressionBasedPreInvocationAdvice;
import org.springframework.security.access.prepost.PreInvocationAuthorizationAdviceVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.UnanimousBased;
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
@ComponentScan({"mx.com.gunix.ui","mx.com.gunix.framework.ui","mx.com.gunix.framework.security"})
@EnableWebSecurity
@EnableVaadinSecurity
public class VaadinSecurityConfig extends WebSecurityConfigurerAdapter implements InitializingBean{
	@Autowired
    private VaadinSecurityContext vaadinSecurityContext;
	
	
	@Autowired
	private UserDetailsService uds;
	
	@Override
    public void afterPropertiesSet() throws Exception {
        vaadinSecurityContext.addAuthenticationSuccessHandler(redirectSaveHandler());
    }
	
    @Bean(name = VaadinSecurityConfiguration.Beans.ACCESS_DECISION_MANAGER)
    protected AccessDecisionManager accessDecisionManager() {
    	List<AccessDecisionVoter<? extends Object>> voters = new ArrayList<AccessDecisionVoter<? extends Object>>();
    	ExpressionBasedPreInvocationAdvice expressionAdvice = new ExpressionBasedPreInvocationAdvice();
		expressionAdvice.setExpressionHandler(new DefaultMethodSecurityExpressionHandler());
    	voters.add(new PreInvocationAuthorizationAdviceVoter(expressionAdvice));
    	voters.add(new AuthenticatedVoter());
        voters.add(new RolAccessDecisionVoter());
        return new UnanimousBased(voters);
    }
    @Bean(name = Beans.VAADIN_SECURITY)
    VaadinSecurity vaadinSecurity() {
        return new GenericVaadinSecurity();
    }
	
    @Bean(name = "authenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    SecuredViewProviderAccessDelegate securedViewProviderAccessDelegate() {
        return new SecuredViewProviderAccessDelegate();
    }
    
    /*
     * The HttpSessionRequestCache is where the initial request before
     * redirect to the login is cached so it can be used after successful login
     */
    @Bean
    public RequestCache requestCache() {
        RequestCache requestCache = new HttpSessionRequestCache();
        return requestCache;
    }

    /*
     * The RequestCacheAwareFilter is responsible for storing the initial request
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
    
    // TODO Disable SpringSecurityFilterChain DefaultFilters (/css, /jsm /images)
    @Override
    public void configure(WebSecurity web) throws Exception {
        web
            .ignoring()
                .antMatchers("/VAADIN/**");
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
        	.userDetailsService(uds)
        		.passwordEncoder(passwordEncoder());
    }
    
    
	@Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/login/**").permitAll()
                .antMatchers("/UIDL/**").permitAll()
                .antMatchers("/HEARTBEAT/**").authenticated()
                .antMatchers("/**").authenticated()
                .anyRequest().authenticated()
            .and()
            .sessionManagement()
                .sessionFixation()
                    .migrateSession()
            .and()
            .csrf().disable()
            .headers()
                .frameOptions().disable();
       http.exceptionHandling().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
        ;
    }
    
    @Bean
	public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
		HiddenHttpMethodFilter hiddenHttpMethodFilter = new HiddenHttpMethodFilter();
		return hiddenHttpMethodFilter;		
	}
}
