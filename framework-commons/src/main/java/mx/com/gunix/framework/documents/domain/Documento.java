package mx.com.gunix.framework.documents.domain;

import java.util.Map;

import javax.validation.constraints.NotNull;

import mx.com.gunix.framework.domain.Identificador;
import mx.com.gunix.framework.util.ActivitiGunixFile;

public class Documento extends ActivitiGunixFile {
	private static final long serialVersionUID = 1L;

	public static final String CONTENT_TYPE = "content-type";
	public static final String SIZE = "size";

	@Identificador
	private Long id;

	@NotNull
	private Carpeta carpeta;

	private Map<String, String> atributos;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Carpeta getCarpeta() {
		return carpeta;
	}

	public void setCarpeta(Carpeta carpeta) {
		this.carpeta = carpeta;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Documento other = (Documento) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Documento [id=" + id + ", nombre=" + getFileName() + ", carpeta=" + carpeta + "]";
	}

	/*
	 * public Map<String, String> getAtributos() { return atributos; }
	 * 
	 * public void setAtributos(Map<String, String> atributos) { this.atributos = atributos; }
	 */

}
