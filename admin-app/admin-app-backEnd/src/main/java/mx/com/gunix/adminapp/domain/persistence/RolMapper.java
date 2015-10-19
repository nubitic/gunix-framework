package mx.com.gunix.adminapp.domain.persistence;

import java.util.List;

import mx.com.gunix.framework.security.domain.Funcion;
import mx.com.gunix.framework.security.domain.Rol;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface RolMapper {
	@Select("Select ID_APLICACION, ID_ROL, DESCRIPCION from SEGURIDAD.ROL where ID_APLICACION=#{idAplicacion}")
 	@Results({@Result(id=true,column="ID_APLICACION",property="aplicacion.idAplicacion"),
		  @Result(id=true,column="ID_ROL",property="idRol"),
		  @Result(column="DESCRIPCION",property="descripcion")})
 	public List<Rol> getByIdAplicacion(String idAplicacion);
	
	@Select("select ID_APLICACION, ID_MODULO, ID_FUNCION, ID_FUNCION_PADRE from FUNCIONES_ROL(#{idAplicacion},#{idRol})")
 	@ResultMap("funcionesRolMap")
	public List<Funcion> getFuncionesByIdRol(@Param("idAplicacion") String idAplicacion, @Param("idRol") String idRol);
	
	@Insert("INSERT INTO SEGURIDAD.ROL VALUES(#{aplicacion.idAplicacion},#{idRol},#{descripcion})")
	public void inserta(Rol rol);
	
	@Insert("INSERT INTO SEGURIDAD.ROL_FUNCION VALUES(#{funcion.modulo.aplicacion.idAplicacion},#{idRol},#{funcion.modulo.idModulo},#{funcion.idFuncion},#{funcion.acceso}::seguridad.nivel_acceso)")
	public void insertaFuncion(@Param("idRol") String idRol, @Param("funcion")Funcion funcion);
}
