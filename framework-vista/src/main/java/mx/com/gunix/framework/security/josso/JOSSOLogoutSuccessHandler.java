package mx.com.gunix.framework.security.josso;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

public class JOSSOLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
	private String gatewayLogoutUrl;

	public JOSSOLogoutSuccessHandler(String gatewayLogoutUrl, String defaultTargetUrl) {
		this.gatewayLogoutUrl = gatewayLogoutUrl;
		setDefaultTargetUrl(defaultTargetUrl);
	}

	/**
	 * This method builds a logout URL based on a HttpServletRequest. The url
	 * contains all necessary parameters required by the front-channel part of
	 * the SSO protocol.
	 * 
	 * @return
	 */
	@Override
	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
		if (logger.isDebugEnabled()) {
			logger.debug("josso_logout request received for uri '" + request.getRequestURI() + "'");
		}

		String logoutUrl = new StringBuilder(this.gatewayLogoutUrl).append(JOSSOUtils.buildBackToQueryString(request, getDefaultTargetUrl())).toString();

		if (logger.isDebugEnabled()) {
			logger.debug("Redirecting to logout url '" + logoutUrl + "'");
		}
		
		JOSSOUtils.cancelCookie(request, response);
		return logoutUrl;
	}
}
