package mx.com.pipp.framework.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mx.com.pipp.framework.domain.Usuario;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class UserDetails extends Usuario implements org.springframework.security.core.userdetails.UserDetails{
	private static final long serialVersionUID = 1L;
	
	private List<SimpleGrantedAuthority> autorities = new ArrayList<SimpleGrantedAuthority>();

	public UserDetails(Usuario usuario) {
		if(usuario.getRoles()!=null){
			usuario.getRoles()
						.stream()
						.forEach(rol->{
							autorities.add(new SimpleGrantedAuthority(rol.getIdRol()));
						});
		}
		setRoles(usuario.getRoles());
		setIdUsuario(usuario.getIdUsuario());
		setPassword(usuario.getPassword());
		setEliminado(usuario.isEliminado());
		setBloqueado(usuario.isBloqueado());
		setActivo(usuario.isActivo());
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return autorities;
	}
	
	@Override
	public String getUsername() {
		return getIdUsuario();
	}

	@Override
	public boolean isAccountNonExpired() {
		return !isEliminado();
	}

	@Override
	public boolean isAccountNonLocked() {
		return !isBloqueado();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return isActivo();
	}

}
