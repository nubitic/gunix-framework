package mx.com.gunix.framework.security.domain;

import java.io.Serializable;
import java.util.List;

public class Aplicacion  implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String idAplicacion;
	private String descripcion;
	private String icono;
	private List<Rol> roles;
	public String getIdAplicacion() {
		return idAplicacion;
	}
	public void setIdAplicacion(String idAplicacion) {
		this.idAplicacion = idAplicacion;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public String getIcono() {
		return icono;
	}
	public void setIcono(String icono) {
		this.icono = icono;
	}
	public List<Rol> getRoles() {
		return roles;
	}
	public void setRoles(List<Rol> roles) {
		this.roles = roles;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idAplicacion == null) ? 0 : idAplicacion.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Aplicacion other = (Aplicacion) obj;
		if (idAplicacion == null) {
			if (other.idAplicacion != null)
				return false;
		} else if (!idAplicacion.equals(other.idAplicacion))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Aplicacion [idAplicacion=" + idAplicacion + "]";
	}
	
	
}
