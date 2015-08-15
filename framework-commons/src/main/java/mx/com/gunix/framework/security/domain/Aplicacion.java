package mx.com.gunix.framework.security.domain;

import java.util.List;

import javax.validation.constraints.NotNull;

public class Aplicacion  implements ACLType {
	private static final long serialVersionUID = 1L;
	
	@NotNull
	private Long id;
	@NotNull
	private String idAplicacion;
	@NotNull
	private String descripcion;
	@NotNull
	private String icono;
	@NotNull
	private List<Rol> roles;
	@NotNull
	private List<Modulo> modulos;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
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
	public List<Modulo> getModulos() {
		return modulos;
	}
	public void setModulos(List<Modulo> modulos) {
		this.modulos = modulos;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (idAplicacion == null) {
			if (other.idAplicacion != null)
				return false;
		} else if (!idAplicacion.equals(other.idAplicacion))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Aplicacion [id=" + id + ", idAplicacion=" + idAplicacion + "]";
	}
	
	
}
