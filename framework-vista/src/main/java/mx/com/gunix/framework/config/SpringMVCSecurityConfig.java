package mx.com.gunix.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@Import(SpringGlobalMethodSecurity.class)
public class SpringMVCSecurityConfig extends AbstractSecurityConfig {
	public static final String STATIC_RESOURCES_LOCATION = "/static/";
	public static final String STATIC_RESOURCES_PATTERN = STATIC_RESOURCES_LOCATION+"**"; 
	@Override
	protected String doConfigure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers(STATIC_RESOURCES_PATTERN).permitAll()
				.antMatchers("/loginForm/**").permitAll()
				.anyRequest().authenticated()
		.and()
			.formLogin();
		return "/loginForm";
	}

	@Bean
	protected AccessDecisionManager accessDecisionManager() {
		return buildAccessDesicionManager();
	}
	
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
}