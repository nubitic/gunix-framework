package mx.com.gunix.framework.security.domain.persistence;

import mx.com.gunix.framework.security.domain.Usuario;

import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

public interface UsuarioMapper {
	@Select("select u.*, DU.curp, DU.rfc, DU.ap_paterno, DU.ap_materno, DU.nombre, DU.correo_electronico, DU.telefono from SEGURIDAD.menu_usuario(#{idUsuario}) u LEFT JOIN SEGURIDAD.DATOS_USUARIO DU ON (U.ID_USUARIO = DU.ID_USUARIO)")
	@ResultMap("usuarioResultMap")
	public Usuario getUsuario(String idUsuario);

}
