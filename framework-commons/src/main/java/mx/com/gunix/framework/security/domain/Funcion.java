package mx.com.gunix.framework.security.domain;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

public class Funcion implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@NotNull
	private String idFuncion;
	@NotNull
	private Modulo modulo;
	@NotNull
	private String titulo;
	@NotNull
	private String descripcion;
	@NotNull
	private String processKey;
	@NotNull
	private float orden;
	private Funcion padre;
	private List<Funcion> hijas;
	private List<Parametro> parametros;
	
	public String getIdFuncion() {
		return idFuncion;
	}
	public void setIdFuncion(String idFuncion) {
		this.idFuncion = idFuncion;
	}
	public Modulo getModulo() {
		return modulo;
	}
	public void setModulo(Modulo modulo) {
		this.modulo = modulo;
	}
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public String getProcessKey() {
		return processKey;
	}
	public void setProcessKey(String processKey) {
		this.processKey = processKey;
	}
	public float getOrden() {
		return orden;
	}
	public void setOrden(float orden) {
		this.orden = orden;
	}
	public Funcion getPadre() {
		return padre;
	}
	public void setPadre(Funcion padre) {
		this.padre = padre;
	}
	public List<Funcion> getHijas() {
		return hijas;
	}
	public void setHijas(List<Funcion> hijas) {
		this.hijas = hijas;
	}
	public List<Parametro> getParametros() {
		return parametros;
	}
	public void setParametros(List<Parametro> parametros) {
		this.parametros = parametros;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idFuncion == null) ? 0 : idFuncion.hashCode());
		result = prime * result + ((modulo == null) ? 0 : modulo.hashCode());
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
		Funcion other = (Funcion) obj;
		if (idFuncion == null) {
			if (other.idFuncion != null)
				return false;
		} else if (!idFuncion.equals(other.idFuncion))
			return false;
		if (modulo == null) {
			if (other.modulo != null)
				return false;
		} else if (!modulo.equals(other.modulo))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Funcion [idFuncion=" + idFuncion + ", modulo=" + modulo + ", titulo=" + titulo + ", processKey=" + processKey +"]";
	}
	
	
}
