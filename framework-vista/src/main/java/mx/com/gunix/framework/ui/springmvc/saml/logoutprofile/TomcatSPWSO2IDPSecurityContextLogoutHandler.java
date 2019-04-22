package mx.com.gunix.framework.ui.springmvc.saml.logoutprofile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.session.StandardSessionFacade;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import mx.com.gunix.framework.service.UsuarioService;

/**
 * "back channel" WSO2 - SLORequest https://jira.spring.io/browse/SES-162
 * @see mx.com.gunix.framework.ui.springmvc.saml.logoutprofile.SingleLogoutProfileWSO2Impl
 * */
public class TomcatSPWSO2IDPSecurityContextLogoutHandler extends SecurityContextLogoutHandler {
	
	private static Field facadeSessionField;
	
	private Manager sessionManager;

	static {
		try {
			facadeSessionField = StandardSessionFacade.class.getDeclaredField("session");
			facadeSessionField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	private UsuarioService usuarioService;

	public TomcatSPWSO2IDPSecurityContextLogoutHandler(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		RequestAttributes reqAttrs = RequestContextHolder.currentRequestAttributes();
		String sessionIndex = (String)reqAttrs.getAttribute(SingleLogoutProfileWSO2Impl.LOGOUT_REQ_SAML_SESSION_INDEX, RequestAttributes.SCOPE_REQUEST);
		
		if (authentication != null || sessionIndex == null) {
			String idUsuario = null;
			List<String> sesionesExpiradas = null;
			
			if (authentication != null) {
				SAMLCredential credential = (SAMLCredential) authentication.getCredentials();
				idUsuario = credential.getNameID().getValue();
				sessionIndex = credential.getAuthenticationAssertion().getAuthnStatements().get(0).getSessionIndex();
				sesionesExpiradas = Arrays.asList(reqAttrs.getSessionId());
			}
			
			super.logout(request, response, authentication);
			
			if(idUsuario !=null && sessionIndex!=null) {
				usuarioService.deleteSAMLLocalSessions(sessionIndex, idUsuario, sesionesExpiradas);	
			}
		} else {
			//El logout proviene de una solicitud de logout del tipo "back channel"
			String idUsuario = (String)reqAttrs.getAttribute(SingleLogoutProfileWSO2Impl.LOGOUT_REQ_SAML_AUTH_INFO, RequestAttributes.SCOPE_REQUEST);
			List<String> sesionesLocales = usuarioService.getSAMLLocalSessions(sessionIndex, idUsuario);
			
			if (sesionesLocales != null && !sesionesLocales.isEmpty()) {
				if(sessionManager == null) {
					StandardSession stdSession = null;
					try {
						stdSession = (StandardSession) facadeSessionField.get(reqAttrs.getSessionMutex());
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new RuntimeException(e);
					}
					sessionManager = stdSession.getManager(); 	
				}
				
				List<String> sesionesExpiradas = new ArrayList<String>();
				sesionesLocales.forEach(sessionId -> {
					try {
						Session sesion = sessionManager.findSession(sessionId);
						if (sesion != null) {
							sesion.expire();
							sesionesExpiradas.add(sessionId);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
				
				usuarioService.deleteSAMLLocalSessions(sessionIndex, idUsuario, sesionesExpiradas);
			}
		}
	}
}
