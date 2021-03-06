package mx.com.gunix.framework.security.domain;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import mx.com.gunix.framework.domain.HashCodeByTimeStampAware;
import mx.com.gunix.framework.domain.Identificador;
import mx.com.gunix.framework.domain.validation.GunixValidationGroups.BeanValidations;
import mx.com.gunix.framework.domain.validation.GunixValidationGroups.DatabaseValidation;

public class Aplicacion extends HashCodeByTimeStampAware implements ACLType {
	private static final long serialVersionUID = 1L;

	@NotNull(groups = DatabaseValidation.class)
	@Min(value = 1, groups = DatabaseValidation.class)
	@Identificador
	private Long id;

	@NotBlank
	@Size(min = 1, max = 30)
	@Identificador
	private String idAplicacion;

	@NotBlank
	@Size(min = 1, max = 100)
	private String descripcion;

	@NotBlank
	@Size(min = 1, max = 25)
	private String icono;

	@NotNull(groups = BeanValidations.class)
	@Size(min = 1, groups = BeanValidations.class, message="Debe definir al menos un Rol")
	@Valid
	private List<Rol> roles;

	@NotNull(groups = BeanValidations.class)
	@Size(min = 1, groups = BeanValidations.class, message="Debe definir al menos un Módulo")
	@Valid
	private List<Modulo> modulos;

	@Valid
	private List<Ambito> ambito;
	
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

	public List<Ambito> getAmbito() {
		return ambito;
	}

	public void setAmbito(List<Ambito> ambito) {
		this.ambito = ambito;
	}

	@Override
	protected int doHashCode() {
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

	@Override
	public String getClaveNegocio() {
		return getIdAplicacion();
	}
}
