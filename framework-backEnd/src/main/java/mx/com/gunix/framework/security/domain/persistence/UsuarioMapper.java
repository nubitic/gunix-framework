package mx.com.gunix.framework.security.domain.persistence;

import mx.com.gunix.framework.security.domain.Usuario;

public interface UsuarioMapper {
	public Usuario getUsuario(String idUsuario);
	public void updatePassword(Usuario usuario);
	public String getPasswordActual(String idUsuario);

}
