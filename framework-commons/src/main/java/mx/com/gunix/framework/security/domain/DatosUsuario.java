package mx.com.gunix.framework.security.domain;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import mx.com.gunix.framework.domain.Identificador;

public class DatosUsuario implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@NotNull
	@Size(min=1,max=254)
	@Identificador
	private String idUsuario;
	
	@NotNull
	@Pattern(regexp = "[A-Z]{4}[0-9]{6}[HM][A-Z]{2}[B-DF-HJ-NP-TV-Z]{3}[A-Z0-9][0-9]")
	private String curp;
	
	@NotNull
	@Pattern(regexp = "[&A-Z\u00d1]{3,4}[0-9]{6}[1-9A-Z]{2}[0-9A]{1}")
	private String rfc;
	
	@NotNull
	@Size(min=1,max=50)
	private String apPaterno;
	
	@Size(min=0,max=50)
	private String apMaterno;
	
	@NotNull
	@Size(min=1,max=100)
	private String nombre;
	
	@NotNull
	private String correoElectronico;
	
	@Size(min=0,max=30)
	private String telefono;

	public String getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(String idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getCurp() {
		return curp;
	}

	public void setCurp(String curp) {
		this.curp = curp;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getApPaterno() {
		return apPaterno;
	}

	public void setApPaterno(String apPaterno) {
		this.apPaterno = apPaterno;
	}

	public String getApMaterno() {
		return apMaterno;
	}

	public void setApMaterno(String apMaterno) {
		this.apMaterno = apMaterno;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCorreoElectronico() {
		return correoElectronico;
	}

	public void setCorreoElectronico(String correoElectronico) {
		this.correoElectronico = correoElectronico;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	

}
