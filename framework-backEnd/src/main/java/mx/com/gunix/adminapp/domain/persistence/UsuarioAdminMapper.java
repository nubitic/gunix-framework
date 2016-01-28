package mx.com.gunix.adminapp.domain.persistence;

import java.util.List;

import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.DatosUsuario;
import mx.com.gunix.framework.security.domain.Rol;
import mx.com.gunix.framework.security.domain.Usuario;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

public interface UsuarioAdminMapper {
	@Select("Select ID_USUARIO from SEGURIDAD.USUARIO where ID_USUARIO=#{idUsuario}")
	@ResultMap("usuarioIDMap")
	public Usuario getByidUsuario(String idUsuario);
	
	public void insertaUsuario(Usuario usuario);
	
	@Insert("INSERT INTO SEGURIDAD.DATOS_USUARIO VALUES(#{idUsuario},#{datosUsuario.curp},#{datosUsuario.rfc},#{datosUsuario.apPaterno},#{datosUsuario.apMaterno},#{datosUsuario.nombre},#{datosUsuario.correoElectronico},#{datosUsuario.telefono})")
	public void insertaDatos(@Param("idUsuario") String idUsuario,@Param("datosUsuario") DatosUsuario datosUsuario);	
	
	@Insert("INSERT INTO SEGURIDAD.USUARIO_APLICACION VALUES(#{idUsuario},#{idAplicacion})")
	public void insertaUsuarioApp(@Param("idUsuario") String idUsuario,@Param("idAplicacion") String idAplicacion);
	
	@Insert("INSERT INTO SEGURIDAD.USUARIO_ROL VALUES(#{idUsuario},#{idAplicacion},#{idRol})")
	public void insertaUsuarioRol(@Param("idUsuario") String idUsuario,@Param("idAplicacion") String idAplicacion,@Param("idRol") String idRol);
	
	public List<Usuario> getByExample(Usuario usuario);
	
	@Select("SELECT UA.ID_USUARIO AS ID_USUARIO, UA.ID_APLICACION AS ID_APLICACION ,A.DESCRIPCION AS DESCRIPCION, A.ACL_ID AS ACL_ID, A.ICONO AS ICONO FROM SEGURIDAD.USUARIO_APLICACION UA INNER JOIN SEGURIDAD.APLICACION A ON (UA.ID_APLICACION = A.ID_APLICACION) where UA.ID_USUARIO=#{idUsuario}")
	@ResultMap("appUsuarioMap")
	public List<Aplicacion> getAppUsuario(String idUsuario);
	
	@Select("SELECT UR.ID_USUARIO AS ID_USUARIO, UR.ID_APLICACION AS ID_APLICACION, UR.ID_ROL AS ID_ROL, R.DESCRIPCION AS DESCRIPCION FROM SEGURIDAD.USUARIO_ROL UR INNER JOIN SEGURIDAD.ROL R ON (UR.ID_ROL = R.ID_ROL AND UR.ID_APLICACION = R.ID_APLICACION) WHERE UR.ID_USUARIO = =#{idUsuario} AND UR.ID_APLICACION =#{idAplicacion}")
	@ResultMap("rolAppUsuarioMap")
	public List<Rol> getRolAppUsuario(String idUsuario,String idAplicacion);
	
	public Usuario getDetalleUById(String idUsuario);
	
}
