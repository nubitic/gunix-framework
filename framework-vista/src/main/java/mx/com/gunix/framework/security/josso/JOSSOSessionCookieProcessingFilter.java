package mx.com.gunix.framework.security.josso;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.josso.gateway.signon.Constants;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

public class JOSSOSessionCookieProcessingFilter extends GenericFilterBean implements ApplicationEventPublisherAware {

	private static final Logger logger = Logger.getLogger(JOSSOSessionCookieProcessingFilter.class);

	private ApplicationEventPublisher eventPublisher;
	private AuthenticationManager authenticationManager;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			Cookie sessionCookie = JOSSOUtils.getJossoSessionCookie((HttpServletRequest) request);
			if (sessionCookie != null) {
				String sessionId = sessionCookie.getValue();
				if (logger.isDebugEnabled()) {
					logger.debug("Authentication empty, JOSSO session cookie found " + StringUtils.quote(sessionId));
				}
				JOSSOAuthenticationToken authRequest = new JOSSOAuthenticationToken(sessionId, null);
				try {
					Authentication auth = authenticationManager.authenticate(authRequest);
					if (auth == null) {
						onUnsuccessfulAuthentication((HttpServletRequest) request, (HttpServletResponse) response, null);
					} else {
						SecurityContextHolder.getContext().setAuthentication(auth);
						// Fire event
						if (eventPublisher != null) {
							eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(SecurityContextHolder.getContext().getAuthentication(), this.getClass()));
						}
					}
				} catch (AuthenticationServiceException e) {
					if (logger.isDebugEnabled()) {
						logger.debug("Unable to autenticate using " + Constants.JOSSO_SINGLE_SIGN_ON_COOKIE + " cookie value " + StringUtils.quote(sessionId) + ". " + e.getMessage());
					}
					onUnsuccessfulAuthentication((HttpServletRequest) request, (HttpServletResponse) response, null);
				}
			}
		}
		chain.doFilter(request, response);
	}

	protected void onUnsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException object) {
		JOSSOUtils.cancelCookie(request, response);
		if (logger.isDebugEnabled()) {
			logger.debug("Unsuccessful authentication." + (object == null ? "" : " " + object.getMessage()) + " JOSSO session id cookie removed.");
		}
	}

	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void afterPropertiesSet() {
		Assert.notNull(authenticationManager, "authenticationManager must be specified");
	}
}
