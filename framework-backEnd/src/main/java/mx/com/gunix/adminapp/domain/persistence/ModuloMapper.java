package mx.com.gunix.adminapp.domain.persistence;

import java.util.List;

import mx.com.gunix.framework.persistence.DescriptorCambios;
import mx.com.gunix.framework.security.domain.Modulo;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface ModuloMapper {
	@Select("Select ID_APLICACION, ID_MODULO, DESCRIPCION, ICONO from SEGURIDAD.MODULO where ID_APLICACION=#{idAplicacion} ORDER BY ID_MODULO")
 	@Results({@Result(id=true,column="ID_APLICACION",property="aplicacion.idAplicacion"),
		  @Result(id=true,column="ID_MODULO",property="idModulo"),
		  @Result(column="DESCRIPCION",property="descripcion"),
		  @Result(column="ICONO",property="icono")})
 	public List<Modulo> getByIdAplicacion(String idAplicacion);
	
	@Insert("INSERT INTO SEGURIDAD.MODULO VALUES(#{aplicacion.idAplicacion},#{idModulo},#{descripcion},#{icono})")
	public void inserta(Modulo modulo);

	public void update(DescriptorCambios dcMod);
}
