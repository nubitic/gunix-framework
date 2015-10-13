package mx.com.gunix.framework.security.domain.persistence;

import mx.com.gunix.framework.security.domain.Usuario;

import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

public interface UsuarioMapper {
	@Select("select * from SEGURIDAD.menu_usuario(#{idUsuario})")
	@ResultMap("usuarioResultMap")
	public Usuario getUsuario(String idUsuario);
}
