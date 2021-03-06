package mx.com.gunix.framework.security.josso;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.josso.gateway.signon.Constants;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.hunteron.core.Context;

import mx.com.gunix.framework.ui.vaadin.VaadinUtils;

public class JOSSOUtils {
	private static final String TIMESTAMP_PREFIX = "omx.com.gunix.framework.security.josso.";
	private static final Logger logger = Logger.getLogger(JOSSOUtils.class);
	private static final String BACKTO_HOST = Context.VIEW_SSO_BACKTO_HOST.get();
	private static final String BACKTO_CONTEXT = Context.VIEW_SSO_BACKTO_CONTEXT.get();

	/**
	 * This method builds the back_to URL value pointing to the given URI.
	 * <p/>
	 * The determines the host used to build the back_to URL in the following order :
	 * <p/>
	 * First, checks the singlePointOfAccess agent's configuration property. Then checks the reverse-proxy-host HTTP header value from the request. Finally uses current host name.
	 */
	public static String buildBackToQueryString(HttpServletRequest request, String uri) {
		String contextPath = null;
		// Using default host
		StringBuffer mySelf = request.getRequestURL();

		try {
			StringBuilder rv = null;
			if (BACKTO_HOST != null) {
				contextPath = (BACKTO_CONTEXT == null || "".equals(BACKTO_CONTEXT)) ? "/" : BACKTO_CONTEXT;
				rv = new StringBuilder(BACKTO_HOST);
			} else {
				// Build the back to url.
				contextPath = request.getContextPath();

				// This is the root context
				if (!StringUtils.hasText(contextPath)) {
					contextPath = "/";
				}
				URL url = new URL(mySelf.toString());
				rv = new StringBuilder(url.getProtocol()).append("://").append(url.getHost()).append(((url.getPort() > 0) ? ":" + url.getPort() : ""));
			}
			rv.append((contextPath.endsWith("/") ? contextPath.substring(0, contextPath.length() - 1) : contextPath));
			rv.append(uri);
			
			String selectedTab = request.getParameter(VaadinUtils.SELECTED_APP_TAB_REQUEST_PARAMETER);
			if (selectedTab != null && !"".equals(selectedTab) && !"null".equalsIgnoreCase(selectedTab)) {
				StringBuilder selectedTabParam = new StringBuilder("?");
				selectedTabParam.append(VaadinUtils.SELECTED_APP_TAB_REQUEST_PARAMETER);
				selectedTabParam.append("=");
				selectedTabParam.append(selectedTab);
				rv.append(encodeURIComponent(selectedTabParam.toString()));
			}

			rv = new StringBuilder("?").append(Constants.PARAM_JOSSO_BACK_TO).append('=').append(rv);
			rv.append("&").append(Constants.PARAM_JOSSO_PARTNERAPP_CONTEXT).append("=").append(contextPath);
			rv.append("&").append(Constants.PARAM_JOSSO_PARTNERAPP_HOST).append("=").append(request.getServerName());
			if (logger.isDebugEnabled()) {
				logger.debug("Using josso_back_to : " + rv);
			}
			return rv.toString();
		} catch (java.net.MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	static String encodeURIComponent(String s) {
	    String result;

	    try {
	        result = URLEncoder.encode(s, "UTF-8")
	                .replaceAll("\\+", "%20")
	                .replaceAll("\\%21", "!")
	                .replaceAll("\\%27", "'")
	                .replaceAll("\\%28", "(")
	                .replaceAll("\\%29", ")")
	                .replaceAll("\\%7E", "~");
	    } catch (UnsupportedEncodingException e) {
	        result = s;
	    }

	    return result;
	}
	
	/**
	 * Locates the JOSSO_SESSION cookie in the request.
	 * 
	 * @param request
	 *            the submitted request which is to be authenticated
	 * @return the cookie value (if present), null otherwise.
	 */
	public static Cookie getJossoSessionCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();

		if ((cookies == null)) {
			return null;
		}

		for (int i = 0; i < cookies.length; i++) {
			if (Constants.JOSSO_SINGLE_SIGN_ON_COOKIE.equals(cookies[i].getName())) {
				return cookies[i];
			}
		}

		return null;
	}

	/**
	 * Sets a "cancel cookie" (with maxAge = 0) on the response to disable persistent logins.
	 * 
	 * @param request
	 * @param response
	 */
	public static void cancelCookie(HttpServletRequest request, HttpServletResponse response) {
		Cookie cookie = new Cookie(Constants.JOSSO_SINGLE_SIGN_ON_COOKIE, null);
		cookie.setMaxAge(0);
		String path = StringUtils.hasLength(request.getContextPath()) ? request.getContextPath() : "/";
		cookie.setPath(path);
		response.addCookie(cookie);
	}

	/**
	 * Sets a new JOSSO Cookie for the given value.
	 * 
	 * @param path
	 *            the path associated with the cookie, normaly the partner application context.
	 * @param value
	 *            the SSO Session ID
	 * @return
	 */
	public static void setCookie(HttpServletRequest request, HttpServletResponse response, String jossoSessionId) {
		Cookie cookie = new Cookie(Constants.JOSSO_SINGLE_SIGN_ON_COOKIE, jossoSessionId);
		cookie.setMaxAge(-1);
		String path = StringUtils.hasLength(request.getContextPath()) ? request.getContextPath() : "/";
		cookie.setPath(path);
		response.addCookie(cookie);
	}

	public static void setTimestamp(HttpServletRequest request, String jossoSessionId, Long timestamp) {
		Assert.hasText(jossoSessionId);
		Assert.notNull(timestamp);
		HttpSession session = request.getSession();
		session.setAttribute(TIMESTAMP_PREFIX + jossoSessionId, timestamp);
	}

	public static Long getTimestamp(HttpServletRequest request, String jossoSessionId) {
		Assert.hasText(jossoSessionId);
		HttpSession session = request.getSession();
		return (Long) session.getAttribute(TIMESTAMP_PREFIX + jossoSessionId);
	}

}