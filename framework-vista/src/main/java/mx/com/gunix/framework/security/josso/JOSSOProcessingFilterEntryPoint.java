package mx.com.gunix.framework.security.josso;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class JOSSOProcessingFilterEntryPoint implements AuthenticationEntryPoint {
	private static final Logger logger = Logger.getLogger(JOSSOProcessingFilterEntryPoint.class);
	private String gatewayLoginUrl;

	@Override
	public void commence(HttpServletRequest servletRequest, HttpServletResponse servletResponse, AuthenticationException authException) throws IOException, ServletException {
		final HttpServletRequest request = (HttpServletRequest) servletRequest;
		final HttpServletResponse response = (HttpServletResponse) servletResponse;

		String loginUrl = new StringBuilder(getGatewayLoginUrl()).append(JOSSOUtils.buildBackToQueryString(request, JOSSOAuthenticationProcessingFilter.JOSSO_SECURITY_CHECK_URI)).toString();
		if (logger.isDebugEnabled()) {
			logger.debug("Redirecting to login url '" + loginUrl + "'");
		}

		response.sendRedirect(response.encodeRedirectURL(loginUrl));
	}

	public String getGatewayLoginUrl() {
		return gatewayLoginUrl;
	}

	public void setGatewayLoginUrl(String gatewayLoginUrl) {
		this.gatewayLoginUrl = gatewayLoginUrl;
	}
}
