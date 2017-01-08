package mx.com.gunix.framework.service;

import java.util.Date;

import mx.com.gunix.framework.security.PersistentRememberMeToken;
import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.security.domain.persistence.UsuarioMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class UsuarioServiceImp implements UsuarioService {
	@Autowired
	UsuarioMapper um;
	

	@Autowired
	PersistentTokenRepository persistentTokenRepository;

	private PasswordEncoder pe = new BCryptPasswordEncoder(16);
	
	@Override
	public Usuario getUsuario(String idUsuario) {
		Usuario usuarioSeg = um.getUsuario(idUsuario);
		return usuarioSeg;
	}

	@Override
	public void createNewToken(PersistentRememberMeToken token) {
		persistentTokenRepository.createNewToken(token);
	}

	@Override
	public void updateToken(String series, String tokenValue, Date lastUsed) {
		persistentTokenRepository.updateToken(series, tokenValue, lastUsed);
	}

	@Override
	public PersistentRememberMeToken getTokenForSeries(String seriesId) {
		org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken rememberMeToken = persistentTokenRepository.getTokenForSeries(seriesId);
		return rememberMeToken !=null?new PersistentRememberMeToken(rememberMeToken.getUsername(), rememberMeToken.getSeries(), rememberMeToken.getTokenValue(), rememberMeToken.getDate()):null;
	}

	@Override
	public void removeUserTokens(String username) {
		persistentTokenRepository.removeUserTokens(username);
	}

	@Override
	public void createNewToken(org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken token) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Usuario getAnonymous() {
		return um.getUsuario(ANONYMOUS);
	}

	@Override
	public void updatePassword(Usuario usuario, String passwordActual) {
		if(pe.matches(passwordActual, um.getPasswordActual(usuario.getIdUsuario()))){
			String encodedPassword = pe.encode(usuario.getPassword());
			usuario.setEncodePassword(encodedPassword);
			um.updatePassword(usuario);	
		} else {
			throw new IllegalArgumentException("La contraseña indicada no corresponde con la contraseña almacenada en la base de datos");
		}
	}

}
