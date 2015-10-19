package mx.com.gunix.domain.persistence;

import java.util.List;

import mx.com.gunix.framework.security.domain.Aplicacion;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

public interface AplicacionMapper {
	@Select("Select ACL_ID as ID,ID_APLICACION, DESCRIPCION, ICONO from SEGURIDAD.APLICACION where ACL_ID=#{id}")
	@ResultMap("aplicacionMap")
	public Aplicacion getById(Long id);

	@Select("Select ACL_ID as ID,ID_APLICACION, DESCRIPCION, ICONO from SEGURIDAD.APLICACION where ID_APLICACION=#{idAplicacion}")
	@ResultMap("aplicacionMap")
	public Aplicacion getByidAplicacion(String idAplicacion);

	@Select("Select ACL_ID as ID,ID_APLICACION, DESCRIPCION, ICONO from SEGURIDAD.APLICACION")
	@ResultMap("aplicacionMap")
	public List<Aplicacion> getAll();

	public List<Aplicacion> getByExample(Aplicacion aplicacion);

	@Insert("INSERT INTO SEGURIDAD.APLICACION VALUES(#{idAplicacion},#{id},#{descripcion},#{icono})")
	public void inserta(Aplicacion aplicacion);
}
