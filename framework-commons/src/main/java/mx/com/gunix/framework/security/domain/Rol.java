package mx.com.gunix.framework.security.domain;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import mx.com.gunix.framework.domain.HashCodeByTimeStampAware;
import mx.com.gunix.framework.domain.Identificador;
import mx.com.gunix.framework.domain.validation.GunixValidationGroups.BeanValidations;

public class Rol extends HashCodeByTimeStampAware implements Serializable {
	private static final long serialVersionUID = 1L;

	@NotNull
	@Size(min = 1, max = 30)
	@Identificador
	private String idRol;

	@NotNull
	@Size(min = 1, max = 100)
	private String descripcion;

	@NotNull(groups = BeanValidations.class)
	@Identificador
	private Aplicacion aplicacion;

	@NotNull(groups = BeanValidations.class)
	@Size(min = 1, groups = BeanValidations.class, message="Debe indicar al menos una Función de algún Módulo")
	private List<Modulo> modulos;

	private boolean habilitado;

	public String getIdRol() {
		return idRol;
	}

	public void setIdRol(String idRol) {
		this.idRol = idRol;
	}

	public List<Modulo> getModulos() {
		return modulos;
	}

	public void setModulos(List<Modulo> modulos) {
		this.modulos = modulos;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Aplicacion getAplicacion() {
		return aplicacion;
	}

	public void setAplicacion(Aplicacion aplicacion) {
		this.aplicacion = aplicacion;
	}

	@Override
	public int doHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aplicacion == null) ? 0 : aplicacion.hashCode());
		result = prime * result + ((idRol == null) ? 0 : idRol.hashCode());
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
		Rol other = (Rol) obj;
		if (aplicacion == null) {
			if (other.aplicacion != null)
				return false;
		} else if (!aplicacion.equals(other.aplicacion))
			return false;
		if (idRol == null) {
			if (other.idRol != null)
				return false;
		} else if (!idRol.equals(other.idRol))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Rol [idRol=" + idRol + ", aplicacion=" + aplicacion + "]";
	}

	public boolean isHabilitado() {
		return habilitado;
	}

	public void setHabilitado(boolean habilitado) {
		this.habilitado = habilitado;
	}

}
