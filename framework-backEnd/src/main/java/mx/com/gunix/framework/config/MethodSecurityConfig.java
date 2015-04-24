package mx.com.gunix.framework.config;

import mx.com.gunix.framework.security.Utils;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.UserDetailsAwareConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
	@Override
	protected AccessDecisionManager accessDecisionManager() {
		return Utils.buildAccesDecisionManager();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
		.authenticationProvider(new AuthenticationProvider(){

			@Override
			public Authentication authenticate(Authentication authentication) throws AuthenticationException {
				return SecurityContextHolder.getContext().getAuthentication();
			}

			@Override
			public boolean supports(Class<?> authentication) {
				return true;
			}
			
		})
		.apply(new UserDetailsAwareConfigurer<AuthenticationManagerBuilder, UserDetailsService>() {

			@Override
			public UserDetailsService getUserDetailsService() {
				return new UserDetailsService() {

					@Override
					public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
						Authentication auth = SecurityContextHolder.getContext().getAuthentication();
						return auth!=null?(UserDetails)auth.getPrincipal():null;
					}

				};
			}
		});
	}
}
