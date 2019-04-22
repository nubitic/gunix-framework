package mx.com.gunix.framework.ui.springmvc.saml;

import org.opensaml.saml2.core.AuthnStatement;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.web.context.request.RequestContextHolder;

import mx.com.gunix.framework.security.UserDetailsServiceImpl;

public class DataBaseSAMLUserDetailsService implements SAMLUserDetailsService {
	
	private UserDetailsServiceImpl userDetailsService;

	public DataBaseSAMLUserDetailsService(UserDetailsServiceImpl userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Override
	public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
		userDetailsService.guardaSAMLSSOAuth(((AuthnStatement) credential.getAuthenticationAssertion().getStatements().get(0)).getSessionIndex(), RequestContextHolder.currentRequestAttributes().getSessionId(), credential.getNameID().getValue());
		return userDetailsService.loadUserByUsername(credential.getNameID().getValue());
	}

}
