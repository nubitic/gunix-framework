package mx.com.pipp.framework.domain.persistence;

import mx.com.pipp.framework.domain.Usuario;

import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

public interface UsuarioMapper {
	@Select("select * from menu_usuario(#{idUsuario})")
	@ResultMap("usuarioResultMap")
	public Usuario getUsuario(String idUsuario);
}
