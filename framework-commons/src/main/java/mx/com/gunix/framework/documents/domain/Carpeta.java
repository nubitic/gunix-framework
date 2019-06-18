package mx.com.gunix.framework.documents.domain;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import mx.com.gunix.framework.domain.HashCodeByTimeStampAware;
import mx.com.gunix.framework.domain.Identificador;

public class Carpeta extends HashCodeByTimeStampAware implements Serializable, Comparable<Carpeta>{
	private static final long serialVersionUID = 1L;

	@Identificador
	private Long id = -1L;

	@Identificador
	private String idStr = "";

	@NotNull
	private String nombre;

	private Carpeta padre;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getIdStr() {
		return idStr;
	}

	public void setIdStr(String idStr) {
		this.idStr = idStr;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Carpeta getPadre() {
		return padre;
	}

	public void setPadre(Carpeta padre) {
		this.padre = padre;
	}

	public String getPath() {
		StringBuilder pathStrBldr = new StringBuilder();
		if (padre != null) {
			pathStrBldr.append(padre.getPath()).append("/");
		}
		pathStrBldr.append(nombre);
		return pathStrBldr.toString();
	}

	public void setPath(String path) {
		// Se agrega este método unicamente para que el proceso de deserealizacion se
		// pueda llevar a cabo correctamente
	}

	@Override
	public String toString() {
		return "Carpeta [id=" + id + ", idStr=" + idStr + ", nombre=" + nombre + ", padre=" + padre + "]";
	}

	@Override
	public int doHashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((idStr == null) ? 0 : idStr.hashCode());
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
		Carpeta other = (Carpeta) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (idStr == null) {
			if (other.idStr != null)
				return false;
		} else if (!idStr.equals(other.idStr))
			return false;
		return true;
	}

	@Override
	public int compareTo(Carpeta o) {
		if (o == null) {
			throw new IllegalArgumentException("El objeto a comparar no puede ser null");

		}

		return o.getPath().compareTo(this.getPath());
	}

}
