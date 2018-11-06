package mx.com.gunix.adminapp.domain.persistence;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import mx.com.gunix.framework.persistence.DescriptorCambios;
import mx.com.gunix.framework.security.domain.Funcion;
import mx.com.gunix.framework.security.domain.Parametro;

public interface FuncionMapper {

	public List<Funcion> getByIdModulo(@Param("idAplicacion") String idAplicacion, @Param("idModulo") String idModulo);

	public Funcion getByIdFuncion(String idAplicacion, String idModulo, String idFuncion);

	public void inserta(Funcion funcion);

	public List<Parametro> getParametrosByIdFuncion(@Param("idAplicacion") String idAplicacion, @Param("idModulo") String idModulo, @Param("idFuncion") String idFuncion);

	public void insertaParametro(@Param("funcion") Funcion funcion, @Param("parametro") Parametro parametro);

	public void update(DescriptorCambios dcFunc);

	public void updateParametro(@Param("idMapFuncion") Map<String, Serializable> idMapFuncion, @Param("dcParam") DescriptorCambios dcParam);
}
