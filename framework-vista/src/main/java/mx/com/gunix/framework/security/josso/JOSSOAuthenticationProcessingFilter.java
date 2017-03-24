package mx.com.gunix.framework.security.josso;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.josso.gateway.GatewayServiceLocator;
import org.josso.gateway.assertion.exceptions.AssertionNotValidException;
import org.josso.gateway.identity.exceptions.IdentityProvisioningException;
import org.josso.gateway.identity.service.SSOIdentityProviderService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class JOSSOAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

	private static final Logger logger = Logger.getLogger(JOSSOAuthenticationProcessingFilter.class);
	public static final String JOSSO_SECURITY_CHECK_URI = "/josso_security_check";
	private static final String BACKTO_HOST = System.getenv("VIEW_SSO_BACKTO_HOST");
	private static final String BACKTO_CONTEXT = System.getenv("VIEW_SSO_BACKTO_CONTEXT");


	private SSOIdentityProviderService ip;
	private String partnerId;

	public JOSSOAuthenticationProcessingFilter(String partnerId) {
		super(JOSSO_SECURITY_CHECK_URI);
		Assert.notNull(partnerId);
		this.partnerId = partnerId;
		initAuthenticationSuccessHandler(getSuccessHandler());
	}

	@Override
	public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler successHandler) {
		initAuthenticationSuccessHandler(successHandler);
		super.setAuthenticationSuccessHandler(successHandler);
	}

	private void initAuthenticationSuccessHandler(AuthenticationSuccessHandler successHandler) {
		if (successHandler != null && successHandler instanceof SavedRequestAwareAuthenticationSuccessHandler && BACKTO_HOST != null) {
			((SavedRequestAwareAuthenticationSuccessHandler) successHandler).setAlwaysUseDefaultTargetUrl(true);
			((SavedRequestAwareAuthenticationSuccessHandler) successHandler).setDefaultTargetUrl(BACKTO_HOST + (BACKTO_CONTEXT != null ? BACKTO_CONTEXT : "") + "/");
		}
	}

	@Required
	public void setGatewayServiceLocator(GatewayServiceLocator gsl) throws Exception {
		this.ip = gsl.getSSOIdentityProvider();
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
		String assertionId = request.getParameter("josso_assertion_id");
		if (!StringUtils.hasText(assertionId)) {
			throw new AuthenticationCredentialsNotFoundException("HTTP parameter josso_assertion_id is missing or empty");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("josso_security_check received for uri " + StringUtils.quote(request.getRequestURI()) + " assertion id " + StringUtils.quote(assertionId));
		}

		try {
			String jossoSessionId = ip.resolveAuthenticationAssertion(partnerId, assertionId);
			JOSSOAuthenticationToken authRequest = new JOSSOAuthenticationToken(jossoSessionId, null);
			Authentication rv = getAuthenticationManager().authenticate(authRequest);
			return rv;
		} catch (AssertionNotValidException e) {
			throw new AuthenticationServiceException("Unable to authenticate user with assertionId " + assertionId, e);
		} catch (IdentityProvisioningException e) {
			e.printStackTrace();
			throw new AuthenticationServiceException("Unable to authenticate user with assertionId " + assertionId, e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, Authentication authResult) throws IOException, ServletException {
		super.successfulAuthentication(request, response, filterChain, authResult);

		JOSSOAuthenticationToken authentication = (JOSSOAuthenticationToken) authResult;
		JOSSOUtils.setCookie(request, response, authentication.getJossoSessionId());
	}
}
