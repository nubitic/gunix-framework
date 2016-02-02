package mx.com.gunix.framework.security.domain;

import java.io.Serializable;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Usuario implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@NotNull(message = "Debe asignar al usuario al menos a un Rol")
	@Size(min=1)
	private List<Aplicacion> aplicaciones;
	
	@Valid
	private DatosUsuario datosUsuario;
	
	@NotNull
	@Size(min=1,max=254)
	private String idUsuario;
	
	@NotNull
	@Size(min=1,max=60)
	private String password;
	
	@NotNull
	private boolean eliminado;
	
	@NotNull
	private boolean bloqueado;
	
	@NotNull
	private boolean activo;
	
	private String encodePassword;
	
	public List<Aplicacion> getAplicaciones() {
		return aplicaciones;
	}
	public void setAplicaciones(List<Aplicacion> aplicaciones) {
		this.aplicaciones = aplicaciones;
	}
	public String getIdUsuario() {
		return idUsuario;
	}
	public void setIdUsuario(String idUsuario) {
		this.idUsuario = idUsuario;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isEliminado() {
		return eliminado;
	}
	public void setEliminado(boolean eliminado) {
		this.eliminado = eliminado;
	}
	public boolean isBloqueado() {
		return bloqueado;
	}
	public void setBloqueado(boolean bloqueado) {
		this.bloqueado = bloqueado;
	}
	public boolean isActivo() {
		return activo;
	}
	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idUsuario == null) ? 0 : idUsuario.hashCode());
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
		Usuario other = (Usuario) obj;
		if (idUsuario == null) {
			if (other.idUsuario != null)
				return false;
		} else if (!idUsuario.equals(other.idUsuario))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Usuario [idUsuario=" + idUsuario + ", eliminado=" + eliminado + ", bloqueado=" + bloqueado + ", activo=" + activo + "]";
	}
	public DatosUsuario getDatosUsuario() {
		return datosUsuario;
	}
	public void setDatosUsuario(DatosUsuario datosUsuario) {
		this.datosUsuario = datosUsuario;
	}
	public String getEncodePassword() {
		return encodePassword;
	}
	public void setEncodePassword(String encodePassword) {
		this.encodePassword = encodePassword;
	}
	
}
