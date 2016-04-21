package mx.com.gunix.framework.documents.domain;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import mx.com.gunix.framework.domain.Identificador;

public class Carpeta implements Serializable {
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
}
