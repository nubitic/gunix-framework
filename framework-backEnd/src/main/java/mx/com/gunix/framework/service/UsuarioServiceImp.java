package mx.com.gunix.framework.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hunteron.core.Context;

import mx.com.gunix.framework.security.PersistentRememberMeToken;
import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.security.domain.persistence.UsuarioMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class UsuarioServiceImp implements UsuarioService {
	@Autowired
	UsuarioMapper um;
	
	private String idAplicacion = Context.ID_APLICACION.get() == null ? "MAIN" : Context.ID_APLICACION.get();
	

	@Autowired
	PersistentTokenRepository persistentTokenRepository;

	@Autowired
	PasswordEncoder pe;
	
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

	@Override
	public void guardaSAMLSSOAuth(String ssoIndex, String localSessionID, String idUsuario) {
		um.guardaSAMLSSOAuth(idAplicacion, ssoIndex, localSessionID, idUsuario);
	}

	@Override
	public List<String> getSAMLLocalSessions(String ssoIndex, String idUsuario) {
		return um.getSAMLLocalSessions(idAplicacion, ssoIndex, idUsuario);
	}

	@Override
	public void deleteSAMLLocalSessions(String ssoIndex, String idUsuario, List<String> sesionesExpiradas) {
		um.deleteSAMLLocalSessions(idAplicacion, ssoIndex, idUsuario, sesionesExpiradas);
	}
}
