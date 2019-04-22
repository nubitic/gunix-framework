package mx.com.gunix.framework.security;

import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImpl implements UserDetailsService {
	@Autowired @Lazy
	UsuarioService usuarioService;
	
	@Override
	public UserDetails loadUserByUsername(String idUsuario) throws UsernameNotFoundException {
		Usuario u = usuarioService.getUsuario(idUsuario);
		if(u==null){
			throw new UsernameNotFoundException("El usuario "+idUsuario+" no existe");
		}
		return new mx.com.gunix.framework.security.UserDetails(u);
	}
	
	public void guardaSAMLSSOAuth(String ssoIndex, String localSessionID, String idUsuario) {
		usuarioService.guardaSAMLSSOAuth(ssoIndex, localSessionID, idUsuario);
	}

}
