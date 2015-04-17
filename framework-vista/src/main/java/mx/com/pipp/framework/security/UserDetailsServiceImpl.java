package mx.com.pipp.framework.security;

import mx.com.pipp.service.UsuarioService;

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
		return new mx.com.pipp.framework.security.UserDetails(usuarioService.getUsuario(idUsuario));
	}

}
