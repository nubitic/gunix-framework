package mx.com.gunix.framework.security.domain.persistence;

import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import mx.com.gunix.framework.security.domain.Usuario;

public interface UsuarioMapper {
	@Select("select u.*, DU.curp, DU.rfc, DU.ap_paterno, DU.ap_materno, DU.nombre, DU.correo_electronico, DU.telefono from SEGURIDAD.menu_usuario(#{idUsuario}) u LEFT JOIN SEGURIDAD.DATOS_USUARIO DU ON (U.ID_USUARIO = DU.ID_USUARIO)")
	@ResultMap("usuarioResultMap")
	public Usuario getUsuario(String idUsuario);

	@Update("update SEGURIDAD.usuario set password = #{encodePassword} where id_usuario = #{idUsuario}")
	public void updatePassword(Usuario usuario);

	@Select("select password from SEGURIDAD.usuario where id_usuario = #{idUsuario}")
	public String getPasswordActual(String idUsuario);

}
