package mx.com.gunix.framework.processes.domain;

import java.io.Serializable;

import mx.com.gunix.framework.security.domain.Parametro;

public class Variable<T extends Serializable> implements Serializable {
	public static enum Scope {
		PROCESO,
		TAREA
	}

	private static final long serialVersionUID = 1L;
	private Scope scope = Scope.PROCESO;
	private T valor;
	private String nombre;
	
	public static Variable<String> fromParametro(Parametro parametro){
		Variable<String> v= new Variable<String>();
		v.setNombre(parametro.getNombre());
		v.setValor(parametro.getValor());
		v.setScope(Scope.PROCESO);
		return v;
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public T getValor() {
		return valor;
	}

	public void setValor(T valor) {
		this.valor = valor;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nombre == null) ? 0 : nombre.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime * result + ((valor == null) ? 0 : valor.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Variable<T> other = (Variable<T>) obj;
		if (nombre == null) {
			if (other.nombre != null)
				return false;
		} else if (!nombre.equals(other.nombre))
			return false;
		if (scope != other.scope)
			return false;
		if (valor == null) {
			if (other.valor != null)
				return false;
		} else if (!valor.equals(other.valor))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Variable [scope=" + scope + ", valor=" + valor + ", nombre=" + nombre + "]";
	}
	
	
}
