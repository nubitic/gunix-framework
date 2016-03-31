package mx.com.gunix.framework.security.domain;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import mx.com.gunix.framework.domain.HashCodeByTimeStampAware;
import mx.com.gunix.framework.domain.Identificador;
import mx.com.gunix.framework.domain.validation.GunixValidationGroups.BeanValidations;

import org.hibernate.validator.constraints.NotBlank;

public class Ambito extends HashCodeByTimeStampAware implements Serializable {
	private static final long serialVersionUID = 1L;

	@NotNull(groups = BeanValidations.class)
	@Identificador
	private Aplicacion aplicacion;

	@NotBlank
	@Size(min = 1, max = 100)
	@Identificador
	private String clase;

	@NotBlank
	@Size(min = 1, max = 100)
	private String getAllUri;

	@NotBlank
	@Size(min = 1, max = 30)
	private String descripcion;

	private List<Permiso> permisos;

	public Aplicacion getAplicacion() {
		return aplicacion;
	}

	public void setAplicacion(Aplicacion aplicacion) {
		this.aplicacion = aplicacion;
	}

	public String getClase() {
		return clase;
	}

	public void setClase(String clase) {
		this.clase = clase;
	}

	public String getGetAllUri() {
		return getAllUri;
	}

	public void setGetAllUri(String getAllUri) {
		this.getAllUri = getAllUri;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public List<Permiso> getPermisos() {
		return permisos;
	}

	public void setPermisos(List<Permiso> permisos) {
		this.permisos = permisos;
	}

	@Override
	public int doHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aplicacion == null) ? 0 : aplicacion.hashCode());
		result = prime * result + ((clase == null) ? 0 : clase.hashCode());
		result = prime * result + ((permisos == null) ? 0 : permisos.hashCode());
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
		Ambito other = (Ambito) obj;
		if (aplicacion == null) {
			if (other.aplicacion != null)
				return false;
		} else if (!aplicacion.equals(other.aplicacion))
			return false;
		if (clase == null) {
			if (other.clase != null)
				return false;
		} else if (!clase.equals(other.clase))
			return false;
		if (permisos == null) {
			if (other.permisos != null)
				return false;
		} else if (!permisos.equals(other.permisos))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Ambito [aplicacion=" + aplicacion + ", clase=" + clase + ", getAllUri=" + getAllUri + ", descripcion=" + descripcion + "]";
	}

	public static class Permiso implements Serializable {
		private static final long serialVersionUID = 1L;

		private ACLType aclType;
		private boolean lectura;
		private boolean modificacion;
		private boolean eliminacion;
		
		public ACLType getAclType() {
			return aclType;
		}

		public void setAclType(ACLType aclType) {
			this.aclType = aclType;
		}

		public boolean isLectura() {
			return lectura;
		}

		public void setLectura(boolean lectura) {
			this.lectura = lectura;
		}

		public boolean isModificacion() {
			return modificacion;
		}

		public void setModificacion(boolean modificacion) {
			this.modificacion = modificacion;
		}

		public boolean isEliminacion() {
			return eliminacion;
		}

		public void setEliminacion(boolean eliminacion) {
			this.eliminacion = eliminacion;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((aclType == null) ? 0 : aclType.hashCode());
			result = prime * result + (eliminacion ? 1231 : 1237);
			result = prime * result + (lectura ? 1231 : 1237);
			result = prime * result + (modificacion ? 1231 : 1237);
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
			Permiso other = (Permiso) obj;
			if (aclType == null) {
				if (other.aclType != null)
					return false;
			} else if (!aclType.equals(other.aclType))
				return false;
			if (eliminacion != other.eliminacion)
				return false;
			if (lectura != other.lectura)
				return false;
			if (modificacion != other.modificacion)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Permiso [aclType=" + aclType + ", lectura=" + lectura + ", modificacion=" + modificacion + ", eliminacion=" + eliminacion + "]";
		}
	}
}
