package mx.com.gunix.framework.security.josso;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class JOSSOAuthenticationToken extends AbstractAuthenticationToken {
	private static final long serialVersionUID = 1L;

	private String jossoSessionId;

	public JOSSOAuthenticationToken(String jossoSessionId, Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.jossoSessionId = jossoSessionId;
	}

	public String getJossoSessionId() {
		return jossoSessionId;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return getDetails();
	}
}
