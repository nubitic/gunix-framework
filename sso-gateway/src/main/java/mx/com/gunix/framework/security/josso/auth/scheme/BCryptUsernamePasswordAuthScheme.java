package mx.com.gunix.framework.security.josso.auth.scheme;

import org.josso.auth.exceptions.SSOAuthenticationException;
import org.josso.auth.scheme.UsernamePasswordAuthScheme;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class BCryptUsernamePasswordAuthScheme extends UsernamePasswordAuthScheme {
	PasswordEncoder encoder = new BCryptPasswordEncoder();

	@Override
	protected String createPasswordHash(String password) throws SSOAuthenticationException {
		return password;
	}

	@Override
	protected boolean validatePassword(String inputPassword, String expectedPassword) {
		return encoder.matches(inputPassword, expectedPassword);
	}
}
