package mx.com.gunix.framework.security;

import java.io.Serializable;
import java.util.Date;

public class PersistentRememberMeToken extends org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken implements Serializable {
	private static final long serialVersionUID = 1L;

	public PersistentRememberMeToken(String username, String series, String tokenValue, Date date) {
		super(username, series, tokenValue, date);
	}

}
