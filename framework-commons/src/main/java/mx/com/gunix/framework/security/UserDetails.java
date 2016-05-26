package mx.com.gunix.framework.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mx.com.gunix.framework.security.domain.Usuario;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class UserDetails extends Usuario implements org.springframework.security.core.userdetails.UserDetails {
	private static final long serialVersionUID = 1L;

	private Collection<GrantedAuthority> autorities = new ArrayList<GrantedAuthority>();
	private GrantedAuthority selectedAuthority;

	public UserDetails(Usuario usuario) {
		if (usuario.getAplicaciones() != null) {
			usuario.getAplicaciones().stream().forEach(aplicacion -> {
				aplicacion.getRoles().stream().forEach(rol -> {
					autorities.add(new GunixSimpleGrantedAuthority(aplicacion.getIdAplicacion() + "_" + rol.getIdRol()));
				});
			});
		}
		setAplicaciones(usuario.getAplicaciones());
		setIdUsuario(usuario.getIdUsuario());
		setPassword(usuario.getPassword());
		setEliminado(usuario.isEliminado());
		setBloqueado(usuario.isBloqueado());
		setActivo(usuario.isActivo());
		setDatosUsuario(usuario.getDatosUsuario());
	}

	public UserDetails() {
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return autorities;
	}

	@SuppressWarnings("unchecked")
	public <T extends GrantedAuthority> void setAuthorities(Collection<T> autorities) {
		this.autorities = (List<GrantedAuthority>) autorities;
	}

	@Override
	public String getUsername() {
		return getIdUsuario();
	}

	public void setUsername(String idUsuario) {
		setIdUsuario(idUsuario);
	}

	@Override
	public boolean isAccountNonExpired() {
		return !isEliminado();
	}

	public void setAccountNonExpired(boolean eliminado) {
		setEliminado(eliminado);
	}

	@Override
	public boolean isAccountNonLocked() {
		return !isBloqueado();
	}

	public void setAccountNonLocked(boolean bloqueado) {
		setBloqueado(bloqueado);
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	public void setCredentialsNonExpired(boolean ignorar) {
	}

	@Override
	public boolean isEnabled() {
		return isActivo();
	}

	public void setEnabled(boolean enabled) {
		setActivo(enabled);
	}

	public void setSelectedAuthority(String rol) {
		selectedAuthority = autorities.stream().filter(sga -> (sga.getAuthority().equals(rol))).findFirst().orElse(null);
	}

	public String getSelectedAuthority() {
		return selectedAuthority.getAuthority();
	}

	public static final class GunixSimpleGrantedAuthority implements GrantedAuthority {
		private static final long serialVersionUID = 1L;

		private SimpleGrantedAuthority sga;

		public GunixSimpleGrantedAuthority() {
		}

		public GunixSimpleGrantedAuthority(String authority) {
			setAuthority(authority);
		}

		@Override
		public String getAuthority() {
			return sga != null ? sga.getAuthority() : null;
		}

		public void setAuthority(String authority) {
			sga = new SimpleGrantedAuthority(authority);
		}

		@Override
		public int hashCode() {
			return ((sga == null) ? 0 : sga.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GunixSimpleGrantedAuthority other = (GunixSimpleGrantedAuthority) obj;
			if (sga == null) {
				if (other.sga != null)
					return false;
			} else if (!sga.equals(other.sga))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return sga != null ? sga.toString() : "";
		}

	}
}
