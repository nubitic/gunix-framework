package mx.com.gunix.framework.service;

import mx.com.gunix.framework.security.PersistentRememberMeToken;
import mx.com.gunix.framework.security.domain.Usuario;

import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import com.hunteron.core.Hessian;

@Hessian("/usuarioService")
public interface UsuarioService extends PersistentTokenRepository{
	public static final String ANONYMOUS="anonymous";
	
	public Usuario getUsuario(String idUsuario);

	void createNewToken(PersistentRememberMeToken token);

	public Usuario getAnonymous();
	
	public void updatePassword(Usuario usuario, String passwordActual);
}
