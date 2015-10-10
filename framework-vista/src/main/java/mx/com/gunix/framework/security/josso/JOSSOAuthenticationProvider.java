package mx.com.gunix.framework.security.josso;

import java.security.Principal;

import org.apache.log4j.Logger;
import org.josso.gateway.GatewayServiceLocator;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.NoSuchUserException;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.SSOIdentityManagerService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public class JOSSOAuthenticationProvider implements AuthenticationProvider {
	private static final Logger logger = Logger.getLogger(JOSSOAuthenticationProvider.class);

	private SSOIdentityManagerService im;
	private String partnerId;
	private UserDetailsService uds;

	public JOSSOAuthenticationProvider(String partnerId) {
		Assert.notNull(partnerId);
		this.partnerId = partnerId;
	}

	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		JOSSOAuthenticationToken auth = (JOSSOAuthenticationToken) authentication;
		String jossoSessionId = auth.getJossoSessionId();
		try {
			SSOUser ssoUser = im.findUserInSession(partnerId, jossoSessionId);
			if (ssoUser == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("No SSOUser found for JOSSO Session ID " + StringUtils.quote(jossoSessionId));
				}
				return null;
			}
			Principal principal = createPrincipal(ssoUser);
			if (principal == null) {
				logger.debug("No principal created from SSOUser " + ObjectUtils.getDisplayString(ssoUser));
				return null;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Principal found for JOSSO Session ID " + StringUtils.quote(jossoSessionId) + ": " + principal);
			}
			UserDetails details = uds.loadUserByUsername(principal.getName());
			if (logger.isDebugEnabled()) {
				logger.debug("Roles of principal " + principal + ":" + StringUtils.arrayToCommaDelimitedString(details.getAuthorities().toArray()));
			}

			Authentication rv = createSuccessAuthentication(principal, auth, details);
			return rv;
		} catch (NoSuchUserException e) {
			throw new UsernameNotFoundException("Unable to find user in session " + jossoSessionId, e);
		} catch (SSOIdentityException e) {
			throw new AuthenticationServiceException("Unable to get user in session " + jossoSessionId, e);
		}
	}

	@Required
	public void setGatewayServiceLocator(GatewayServiceLocator gsl) throws Exception {
		this.im = gsl.getSSOIdentityManager();
	}

	@Required
	public void setUserDetailsService(UserDetailsService uds) {
		this.uds = uds;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return JOSSOAuthenticationToken.class.isAssignableFrom(authentication);
	}

	protected Principal createPrincipal(SSOUser user) {
		return user;
	}

	private Authentication createSuccessAuthentication(Object principal, JOSSOAuthenticationToken auth, UserDetails details) {
		JOSSOAuthenticationToken rv = new JOSSOAuthenticationToken(auth.getJossoSessionId(), details.getAuthorities());
		rv.setDetails(details);
		rv.setAuthenticated(true);
		return rv;
	}
}
