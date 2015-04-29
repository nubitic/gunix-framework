package mx.com.gunix.framework.service;

import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import mx.com.gunix.framework.security.PersistentRememberMeToken;
import mx.com.gunix.framework.security.domain.Usuario;

import com.hunteron.core.Hessian;

@Hessian("/usuarioService")
public interface UsuarioService extends PersistentTokenRepository{
	public Usuario getUsuario(String idUsuario);

	void createNewToken(PersistentRememberMeToken token);
}
