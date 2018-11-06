package mx.com.gunix.adminapp.domain.persistence;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import mx.com.gunix.framework.persistence.DescriptorCambios;
import mx.com.gunix.framework.security.domain.Funcion;
import mx.com.gunix.framework.security.domain.Rol;

public interface RolMapper {

 	public List<Rol> getByIdAplicacion(String idAplicacion);
	
	public List<Funcion> getFuncionesByIdRol(@Param("idAplicacion") String idAplicacion, @Param("idRol") String idRol);

	public void inserta(Rol rol);

	public void insertaFuncion(@Param("idRol") String idRol, @Param("funcion") Funcion funcion);

	public void update(DescriptorCambios dcRol);

	public void updateFuncion(@Param("idRolMap") Map<String, Serializable> idRolMap, @Param("dcFuncionRol") DescriptorCambios dcFuncionRol);

	public void deleteFuncion(@Param("idRol") String idRol, @Param("funcion") Funcion funcion);
}
