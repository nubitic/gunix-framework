package mx.com.gunix.domain.persistence;

import java.util.List;

import mx.com.gunix.framework.security.domain.Funcion;
import mx.com.gunix.framework.security.domain.Parametro;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface FuncionMapper {
	@Select("Select ID_APLICACION, ID_MODULO, ID_FUNCION, TITULO, DESCRIPCION, PROCESS_KEY, ORDEN, ID_FUNCION_PADRE from SEGURIDAD.FUNCION where ID_APLICACION=#{idAplicacion} AND ID_MODULO=#{idModulo}")
	@ResultMap("funcionMap")
	public List<Funcion> getByIdModulo(@Param("idAplicacion") String idAplicacion, @Param("idModulo") String idModulo);

	public Funcion getByIdFuncion(String idAplicacion, String idModulo, String idFuncion);

	public void inserta(Funcion funcion);

	@Select("Select ID_PARAM, VALOR from SEGURIDAD.PARAM_FUNCION where ID_APLICACION=#{idAplicacion} AND ID_MODULO=#{idModulo} AND ID_FUNCION=#{idFuncion}")
	@Results({ @Result(id = true, column = "ID_PARAM", property = "nombre"), @Result(id = true, column = "VALOR", property = "valor") })
	public List<Parametro> getParametrosByIdFuncion(@Param("idAplicacion") String idAplicacion, @Param("idModulo") String idModulo, @Param("idFuncion") String idFuncion);

	@Insert("INSERT INTO SEGURIDAD.PARAM_FUNCION VALUES (#{funcion.modulo.aplicacion.idAplicacion},#{funcion.modulo.idModulo},#{funcion.idFuncion},#{parametro.nombre},#{parametro.valor})")
	public void insertaParametro(@Param("funcion") Funcion funcion, @Param("parametro") Parametro parametro);
}
