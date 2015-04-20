package mx.com.gunix.framework.security;

import mx.com.gunix.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {
	@Autowired @Lazy
	UsuarioService usuarioService;
	
	@Override
	public UserDetails loadUserByUsername(String idUsuario) throws UsernameNotFoundException {
		return new mx.com.gunix.framework.security.UserDetails(usuarioService.getUsuario(idUsuario));
	}

}
