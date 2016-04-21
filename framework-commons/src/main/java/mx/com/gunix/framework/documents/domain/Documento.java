package mx.com.gunix.framework.documents.domain;

import java.io.Serializable;
import java.util.Map;

import javax.validation.constraints.NotNull;

import mx.com.gunix.framework.domain.Identificador;

public class Documento implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String CONTENT_TYPE="content-type";
	public static final String SIZE="size";

	@Identificador
	private Long id;

	@NotNull
	private String nombre;

	@NotNull
	private Carpeta carpeta;

	private Map<String, String> atributos;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Carpeta getCarpeta() {
		return carpeta;
	}

	public void setCarpeta(Carpeta carpeta) {
		this.carpeta = carpeta;
	}

	/*public Map<String, String> getAtributos() {
		return atributos;
	}

	public void setAtributos(Map<String, String> atributos) {
		this.atributos = atributos;
	}*/

}
