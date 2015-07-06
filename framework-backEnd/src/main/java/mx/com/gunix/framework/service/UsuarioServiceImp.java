package mx.com.gunix.framework.service;

import java.util.Date;

import mx.com.gunix.framework.security.PersistentRememberMeToken;
import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.security.domain.persistence.UsuarioMapper;

import org.springframework.beans.factory.annotation.Autowired;
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

	@Override
	public Usuario getUsuario(String idUsuario) {
		return um.getUsuario(idUsuario);
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
		return (PersistentRememberMeToken) persistentTokenRepository.getTokenForSeries(seriesId);
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
	
}
