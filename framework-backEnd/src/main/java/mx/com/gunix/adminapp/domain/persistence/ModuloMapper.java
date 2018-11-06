package mx.com.gunix.adminapp.domain.persistence;

import java.util.List;

import mx.com.gunix.framework.persistence.DescriptorCambios;
import mx.com.gunix.framework.security.domain.Modulo;

public interface ModuloMapper {

 	public List<Modulo> getByIdAplicacion(String idAplicacion);
	
	public void inserta(Modulo modulo);

	public void update(DescriptorCambios dcMod);
}
