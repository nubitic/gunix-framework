package mx.com.gunix.adminapp.domain.persistence;

import mx.com.gunix.framework.security.domain.Usuario;

import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

public interface UsuarioAdminMapper {
	@Select("Select ID_USUARIO,PASSWORD,ESTATUS from SEGURIDAD.USUARIO where ID_USUARIO=#{idUsuario}")
	@ResultMap("usuarioMap")
	public Usuario getByidUsuario(String idUsuario);
}
