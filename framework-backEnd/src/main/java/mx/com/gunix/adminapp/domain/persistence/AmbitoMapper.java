package mx.com.gunix.adminapp.domain.persistence;

import java.util.List;

import mx.com.gunix.framework.security.domain.Ambito;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface AmbitoMapper {
	@Select("select * from seguridad.acl_class where ID_APLICACION=#{idAplicacion}  ORDER BY CLASS")
 	@Results({@Result(id=true,column="ID_APLICACION",property="aplicacion.idAplicacion"),
		  @Result(id=true,column="CLASS",property="clase"),
		  @Result(column="DESCRIPCION",property="descripcion"),
		  @Result(column="get_all_uri",property="getAllUri")})
	public List<Ambito> getByIdAplicacion(String idAplicacion);
	
	@Insert("INSERT INTO seguridad.acl_class(id_aplicacion, class, descripcion, get_all_uri) values (#{aplicacion.idAplicacion},#{clase},#{descripcion},#{getAllUri})")
	public void inserta(Ambito ambito);
	
	@Select("select count(1) from seguridad.acl_fullreadaccess_sid inner join seguridad.acl_sid on (acl_sid.id = acl_fullreadaccess_sid.sid) inner join seguridad.acl_class on(acl_class.id = acl_fullreadaccess_sid.object_id_class) where acl_sid.sid=#{idUsuario} and acl_class.class=#{ambito.clase}")
	public boolean puedeLeerTodo(@Param("idUsuario") String idUsuario, @Param("ambito") Ambito ambito);
	
	@Delete("delete from seguridad.acl_fullreadaccess_sid where sid=(select id from seguridad.acl_sid where sid=#{idUsuario}) and object_id_class=(select id from seguridad.acl_class where class=#{ambito.clase})")
	public void deleteFullReadAccessFor(@Param("idUsuario") String idUsuario, @Param("ambito") Ambito ambito);
	
	@Insert("insert into seguridad.acl_fullreadaccess_sid values((select id from seguridad.acl_sid where sid=#{idUsuario}),(select id from seguridad.acl_class where class=#{ambito.clase}))")
	public void insertFullReadAccessFor(@Param("idUsuario") String idUsuario, @Param("ambito") Ambito ambito);
}
