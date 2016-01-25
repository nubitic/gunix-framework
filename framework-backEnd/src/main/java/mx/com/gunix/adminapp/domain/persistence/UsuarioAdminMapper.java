package mx.com.gunix.adminapp.domain.persistence;

import mx.com.gunix.framework.security.domain.DatosUsuario;
import mx.com.gunix.framework.security.domain.Usuario;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

public interface UsuarioAdminMapper {
	@Select("Select ID_USUARIO,PASSWORD,ESTATUS from SEGURIDAD.USUARIO where ID_USUARIO=#{idUsuario}")
	@ResultMap("usuarioAdminMap")
	public Usuario getByidUsuario(String idUsuario);
	
	@Insert("INSERT INTO SEGURIDAD.USUARIO VALUES(#{idUsuario},#{encodePassword},#{estatus})")
	public void inserta(Usuario usuario);
	
	@Insert("INSERT INTO SEGURIDAD.DATOS_USUARIO VALUES(#{idUsuario},#{datosUsuario.curp},#{datosUsuario.rfc},#{datosUsuario.apPaterno},#{datosUsuario.apMaterno},#{datosUsuario.nombre},#{datosUsuario.correoElectronico},#{datosUsuario.telefono})")
	public void insertaDatos(@Param("idUsuario") String idUsuario,@Param("datosUsuario") DatosUsuario datosUsuario);	
	
	@Insert("INSERT INTO SEGURIDAD.USUARIO_APLICACION VALUES(#{idUsuario},#{idAplicacion})")
	public void insertaUsuarioApp(@Param("idUsuario") String idUsuario,@Param("idAplicacion") String idAplicacion);
	
	@Insert("INSERT INTO SEGURIDAD.USUARIO_ROL VALUES(#{idUsuario},#{idAplicacion},#{idRol})")
	public void insertaUsuarioRol(@Param("idUsuario") String idUsuario,@Param("idAplicacion") String idAplicacion,@Param("idRol") String idRol);
}
