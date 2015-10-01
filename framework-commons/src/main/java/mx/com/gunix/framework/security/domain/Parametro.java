package mx.com.gunix.framework.security.domain;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import mx.com.gunix.framework.domain.HashCodeByTimeStampAware;

public class Parametro extends HashCodeByTimeStampAware implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@NotNull
	@Size(min=1,max=15)
	private String nombre;
	
	@NotNull
	@Size(min=1,max=500)
	private String valor;
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getValor() {
		return valor;
	}
	public void setValor(String valor) {
		this.valor = valor;
	}
		
	@Override
	public int doHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nombre == null) ? 0 : nombre.hashCode());
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
		Parametro other = (Parametro) obj;
		if (nombre == null) {
			if (other.nombre != null)
				return false;
		} else if (!nombre.equals(other.nombre))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Parametro [" + nombre + "=" + valor + "]";
	}
	
	
}
