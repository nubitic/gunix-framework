package mx.com.gunix.framework.service;

import com.hunteron.core.Hessian;

import mx.com.gunix.framework.domain.Usuario;

@Hessian("/usuarioService")
public interface UsuarioService {
	public Usuario getUsuario(String idUsuario);
}
