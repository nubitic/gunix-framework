package mx.com.gunix.framework.service;

import java.util.List;

import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import com.hunteron.core.Hessian;

import mx.com.gunix.framework.security.PersistentRememberMeToken;
import mx.com.gunix.framework.security.domain.Usuario;

@Hessian("/usuarioService")
public interface UsuarioService extends PersistentTokenRepository{
	public static final String ANONYMOUS="anonymous";
	
	public Usuario getUsuario(String idUsuario);

	void createNewToken(PersistentRememberMeToken token);

	public Usuario getAnonymous();
	
	public void updatePassword(Usuario usuario, String passwordActual);
	
	public void guardaSAMLSSOAuth(String ssoIndex, String localSessionID, String idUsuario);

	public List<String> getSAMLLocalSessions(String ssoIndex, String idUsuario);

	public void deleteSAMLLocalSessions(String ssoIndex, String idUsuario, List<String> sesionesExpiradas);
}
