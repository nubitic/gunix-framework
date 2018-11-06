package mx.com.gunix.adminapp.domain.persistence;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import mx.com.gunix.framework.security.domain.Ambito;

public interface AmbitoMapper {

	public List<Ambito> getByIdAplicacion(String idAplicacion);

	public void inserta(Ambito ambito);

	public boolean puedeLeerTodo(@Param("idUsuario") String idUsuario, @Param("ambito") Ambito ambito);

	public void deleteFullReadAccessFor(@Param("idUsuario") String idUsuario, @Param("ambito") Ambito ambito);

	public void insertFullReadAccessFor(@Param("idUsuario") String idUsuario, @Param("ambito") Ambito ambito);
}
