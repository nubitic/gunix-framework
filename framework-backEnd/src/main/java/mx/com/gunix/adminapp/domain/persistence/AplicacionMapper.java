package mx.com.gunix.adminapp.domain.persistence;

import java.util.List;

import mx.com.gunix.framework.persistence.DescriptorCambios;
import mx.com.gunix.framework.security.domain.Aplicacion;

public interface AplicacionMapper {

	public Aplicacion getById(String id);

	public Aplicacion getByidAplicacion(String idAplicacion);

	public List<Aplicacion> getAll();

	public List<Aplicacion> getByExample(Aplicacion aplicacion);

	public void inserta(Aplicacion aplicacion);

	public void update(DescriptorCambios dc);
}
