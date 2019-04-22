package mx.com.gunix.framework.ui.springmvc.saml.logoutprofile;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.opensaml.common.SAMLException;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.impl.AssertionImpl;
import org.opensaml.saml2.core.impl.AuthnStatementImpl;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.transport.http.HTTPTransportUtils;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.SAMLStatusException;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.util.SAMLUtil;
import org.springframework.security.saml.websso.SingleLogoutProfileImpl;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.w3c.dom.Element;

import mx.com.gunix.framework.service.UsuarioService;

/**
 * "back channel" WSO2 - SLORequest https://jira.spring.io/browse/SES-162
 * @see mx.com.gunix.framework.ui.springmvc.saml.logoutprofile.TomcatSPWSO2IDPSecurityContextLogoutHandler
 * */
public class SingleLogoutProfileWSO2Impl extends SingleLogoutProfileImpl {
	
	private UsuarioService usuarioService;
	
	public static final String LOGOUT_REQ_SAML_AUTH_INFO = "LOGOUT_REQ_SAML_AUTH_INFO";
	public static final String LOGOUT_REQ_SAML_SESSION_INDEX = "LOGOUT_REQ_SAML_SESSION_INDEX";
	
	public SingleLogoutProfileWSO2Impl(UsuarioService usuarioService) {
		super();
		this.usuarioService = usuarioService;
	}

	@Override
	public boolean processLogoutRequest(SAMLMessageContext context, SAMLCredential credential) throws SAMLException {
		if (credential == null) {
			SAMLObject message = context.getInboundSAMLMessage();

	        // Verify type
	        if (message == null || !(message instanceof LogoutRequest)) {
	            throw new SAMLException("Message is not of a LogoutRequest object type");
	        }
	        
	        LogoutRequest logoutRequest = (LogoutRequest) message;
	        
	        NameID authInfo =null;
	        try {
				authInfo = getNameID(context, logoutRequest);
			} catch (DecryptionException e) {
				throw new SAMLStatusException(StatusCode.RESPONDER_URI, "The NameID can't be decrypted", e);
			}
	        
	        String sessionIndex = logoutRequest.getSessionIndexes().get(0).getSessionIndex();
	        
	        RequestAttributes reqAttrs = RequestContextHolder.currentRequestAttributes();
	        
	        String idUsuario = authInfo.getValue();
	        reqAttrs.setAttribute(LOGOUT_REQ_SAML_AUTH_INFO, idUsuario, RequestAttributes.SCOPE_REQUEST);
	        reqAttrs.setAttribute(LOGOUT_REQ_SAML_SESSION_INDEX, sessionIndex, RequestAttributes.SCOPE_REQUEST);
	        
			List<String> sesionesLocalesEncontradas = usuarioService.getSAMLLocalSessions(sessionIndex, idUsuario);
			if (sesionesLocalesEncontradas != null && !sesionesLocalesEncontradas.isEmpty()) {
				Assertion assertion = new AssertionImpl("", "", "") {
					@Override
					public List<AuthnStatement> getAuthnStatements() {
						return Arrays.asList(new AuthnStatementImpl("", "", "") {
							@Override
							public String getSessionIndex() {
								return sessionIndex;
							}
							
						});
					}
				};
				credential = new SAMLCredential(authInfo, assertion, null, null);
			} else {
				return false;
			}
	        
		}
		return super.processLogoutRequest(context, credential);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void sendLogoutResponse(SAMLMessageContext context, String statusCode, String statusMessage) throws MetadataProviderException, SAMLException, MessageEncodingException {
		SAMLObjectBuilder<LogoutResponse> responseBuilder = (SAMLObjectBuilder<LogoutResponse>) builderFactory.getBuilder(LogoutResponse.DEFAULT_ELEMENT_NAME);
		LogoutResponse logoutResponse = responseBuilder.buildObject();

		IDPSSODescriptor idpDescriptor = SAMLUtil.getIDPDescriptor(metadata, context.getPeerEntityId());
		SPSSODescriptor spDescriptor = (SPSSODescriptor) context.getLocalEntityRoleMetadata();
		String binding = SAMLUtil.getLogoutBinding(idpDescriptor, spDescriptor);
		SingleLogoutService logoutService = SAMLUtil.getLogoutServiceForBinding(idpDescriptor, binding);

		logoutResponse.setID(generateID());
		logoutResponse.setIssuer(getIssuer(context.getLocalEntityId()));
		logoutResponse.setVersion(SAMLVersion.VERSION_20);
		logoutResponse.setIssueInstant(new DateTime());
		logoutResponse.setInResponseTo(context.getInboundSAMLMessageId());
		logoutResponse.setDestination(logoutService.getLocation());

		Status status = getStatus(statusCode, statusMessage);
		logoutResponse.setStatus(status);
		
		String messageStr = base64Encode(logoutResponse);
		
		HttpServletResponseAdapter out = (HttpServletResponseAdapter) context.getOutboundMessageTransport();
        HTTPTransportUtils.addNoCacheHeaders(out);
        HTTPTransportUtils.setUTF8Encoding(out);
        out.setStatusCode(200);
        
        try {
        	PrintWriter writer =  out.getWrappedResponse().getWriter();
        	writer.print(messageStr);
        	writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String base64Encode(SAMLObject message) throws MessageEncodingException {
		log.debug("Base64 encoding SAML message");
		try {
			return Base64.encodeBytes(XMLHelper.nodeToString(marshallMessage(message)).getBytes("UTF-8"), Base64.DONT_BREAK_LINES);
		} catch (UnsupportedEncodingException e) {
			throw new MessageEncodingException("Unable to Base64 encode SAML message", e);
		}
	}
	
    private Element marshallMessage(XMLObject message) throws MessageEncodingException {
        log.debug("Marshalling message");

        try {
            Marshaller marshaller = org.opensaml.xml.Configuration.getMarshallerFactory().getMarshaller(message);
            if (marshaller == null) {
                log.error("Unable to marshall message, no marshaller registered for message object: "
                        + message.getElementQName());
                throw new MessageEncodingException(
                        "Unable to marshall message, no marshaller registered for message object: "
                        + message.getElementQName());
            }
            Element messageElem = marshaller.marshall(message);
            if (log.isTraceEnabled()) {
                log.trace("Marshalled message into DOM:\n{}", XMLHelper.nodeToString(messageElem));
            }
            return messageElem;
        } catch (MarshallingException e) {
            log.error("Encountered error marshalling message to its DOM representation", e);
            throw new MessageEncodingException("Encountered error marshalling message into its DOM representation", e);
        }
    }
	
}
