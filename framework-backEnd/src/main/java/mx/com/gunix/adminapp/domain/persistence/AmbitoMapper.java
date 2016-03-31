package mx.com.gunix.adminapp.domain.persistence;

import java.util.List;

import mx.com.gunix.framework.security.domain.Ambito;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface AmbitoMapper {
	@Select("select* from seguridad.acl_class where ID_APLICACION=#{idAplicacion}  ORDER BY CLASS")
 	@Results({@Result(id=true,column="ID_APLICACION",property="aplicacion.idAplicacion"),
		  @Result(id=true,column="CLASS",property="clase"),
		  @Result(column="DESCRIPCION",property="descripcion"),
		  @Result(column="get_all_uri",property="getAllUri")})
	public List<Ambito> getByIdAplicacion(String idAplicacion);
	
	@Insert("INSERT INTO seguridad.acl_class(id_aplicacion, class, descripcion, get_all_uri) values (#{aplicacion.idAplicacion},#{clase},#{descripcion},#{getAllUri})")
	public void inserta(Ambito ambito);
}
