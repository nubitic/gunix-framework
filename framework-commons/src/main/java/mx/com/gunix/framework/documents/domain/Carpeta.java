package mx.com.gunix.framework.documents.domain;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import mx.com.gunix.framework.domain.HashCodeByTimeStampAware;
import mx.com.gunix.framework.domain.Identificador;

public class Carpeta extends HashCodeByTimeStampAware implements Serializable {
	private static final long serialVersionUID = 1L;

	@Identificador
	private Long id;

	@NotNull
	private String nombre;

	private Carpeta padre;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
	
	public void setPath(String path){
		//Se agrega este método unicamente para que el proceso de deserealizacion se pueda llevar a cabo correctamente
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
		return true;
	}

	@Override
	public String toString() {
		return "Carpeta [" + getPath() + ", id=" + id + "]";
	}

	@Override
	protected int doHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
}
