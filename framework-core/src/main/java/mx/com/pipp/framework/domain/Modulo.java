package mx.com.pipp.framework.domain;

import java.io.Serializable;
import java.util.List;

public class Modulo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String idModulo;
	private String descripcion;
	private String icono;
	private List<Funcion> funciones;
	
	public String getIdModulo() {
		return idModulo;
	}
	public void setIdModulo(String idModulo) {
		this.idModulo = idModulo;
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
	public List<Funcion> getFunciones() {
		return funciones;
	}
	public void setFunciones(List<Funcion> funciones) {
		this.funciones = funciones;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idModulo == null) ? 0 : idModulo.hashCode());
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
		Modulo other = (Modulo) obj;
		if (idModulo == null) {
			if (other.idModulo != null)
				return false;
		} else if (!idModulo.equals(other.idModulo))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Modulo [idModulo=" + idModulo + "]";
	}
	
}
