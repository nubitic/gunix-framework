package mx.com.gunix.framework.security.josso;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.josso.gateway.GatewayServiceLocator;
import org.josso.gateway.session.exceptions.NoSuchSessionException;
import org.josso.gateway.session.exceptions.SSOSessionException;
import org.josso.gateway.session.service.SSOSessionManagerService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

public class JOSSOSessionPingFilter extends GenericFilterBean {

	private static final Logger logger = Logger.getLogger(JOSSOSessionPingFilter.class);
	private static final long DEFAULT_SESSION_ACCESS_MIN_INTERVAL = 5000;

	private long sessionAccessMinInterval = DEFAULT_SESSION_ACCESS_MIN_INTERVAL;
	private SSOSessionManagerService sm;
	private String logoutUrl;
	private String partnerId;

	public JOSSOSessionPingFilter(String partnerId) {
		this.partnerId = partnerId;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication instanceof JOSSOAuthenticationToken) {
			JOSSOAuthenticationToken token = (JOSSOAuthenticationToken) authentication;
			long now = System.currentTimeMillis();
			Long timestamp = JOSSOUtils.getTimestamp((HttpServletRequest) request, token.getJossoSessionId());
			if (timestamp == null || (now - timestamp) > this.sessionAccessMinInterval) {
				try {
					sm.accessSession(partnerId, token.getJossoSessionId());
					if (logger.isDebugEnabled()) {
						logger.debug("JOSSO session " + StringUtils.quote(token.getJossoSessionId()) + " pinged at " + now);
					}
					JOSSOUtils.setTimestamp((HttpServletRequest) request, token.getJossoSessionId(), now);
				} catch (NoSuchSessionException e) {
					if (logger.isDebugEnabled()) {
						logger.debug("Unable to ping JOSSO session " + token.getJossoSessionId() + ". " + e.getMessage());
					}
					String redirect = getContextualLogoutUrl((HttpServletRequest) request, logoutUrl);
					((HttpServletResponse) response).sendRedirect(redirect);
					return;
				} catch (SSOSessionException e) {
					if (logger.isDebugEnabled()) {
						logger.warn("Unable to ping JOSSO session " + token.getJossoSessionId() + ". " + e.getMessage());
					}
					String redirect = getContextualLogoutUrl((HttpServletRequest) request, logoutUrl);
					((HttpServletResponse) response).sendRedirect(redirect);
					return;
				}
			}
		}
		chain.doFilter(request, response);
	}

	private String getContextualLogoutUrl(HttpServletRequest request, String url) {
		String contextPath = request.getContextPath();
		if ("/".equals(contextPath)) {
			contextPath = "";
		}
		if (contextPath.endsWith("/")) {
			contextPath = contextPath.substring(0, contextPath.length() - 1);
		}
		String rv = contextPath + url;
		return rv;
	}

	public void setSessionAccessMinInterval(long sessionAccessMinInterval) {
		this.sessionAccessMinInterval = sessionAccessMinInterval;
	}

	@Required
	public void setGatewayServiceLocator(GatewayServiceLocator gsl) throws Exception {
		this.sm = gsl.getSSOSessionManager();
	}

	@Required
	public void setLogoutUrl(String logoutUrl) {
		this.logoutUrl = logoutUrl;
	}

}
