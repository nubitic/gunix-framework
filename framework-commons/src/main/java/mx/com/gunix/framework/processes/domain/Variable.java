package mx.com.gunix.framework.processes.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import mx.com.gunix.framework.security.domain.Parametro;

public class Variable<T extends Serializable> implements Serializable {
	public static enum Scope {
		PROCESO
		}

	private static final long serialVersionUID = 1L;
	private Scope scope = Scope.PROCESO;

	private String nombre;

	private transient T valor;

	@SuppressWarnings("rawtypes")
	private Collection iterableValue;
	@SuppressWarnings("rawtypes")
	private Map mapValue;
	private Serializable objectValue;

	public static Variable<String> fromParametro(Parametro parametro) {
		Variable<String> v = new Variable<String>();
		v.setNombre(parametro.getNombre());
		v.setValor(parametro.getValor());
		v.setScope(Scope.PROCESO);
		return v;
	}
	
	public static List<Variable<?>> fromParametros(List<Parametro> parametros){
		List<Variable<?>> vars = new ArrayList<Variable<?>>();
		parametros.stream().forEach(p -> {
			vars.add(fromParametro(p));
		});
		return vars;
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	@SuppressWarnings("unchecked")
	public T getValor() {
		if (valor == null && (iterableValue != null || mapValue != null || objectValue != null)) {
			if (iterableValue != null) {
				this.valor = (T) iterableValue;
			} else {
				if (mapValue != null) {
					this.valor = (T) mapValue;
				} else {
					if (objectValue != null) {
						this.valor = (T) objectValue;
					}
				}
			}
		}
		return valor;
	}

	@SuppressWarnings("rawtypes")
	public void setValor(T valor) {
		if (valor instanceof Collection) {
			iterableValue = (Collection) valor;
			mapValue = null;
			objectValue = null;
		} else {
			if (valor instanceof Map) {
				iterableValue = null;
				mapValue = (Map) valor;
				objectValue = null;
			} else {
				if (valor instanceof Serializable) {
					iterableValue = null;
					mapValue = null;
					objectValue = valor;
				} else {
					if (valor != null) {
						throw new IllegalArgumentException("Tipo de dato no soportado: " + valor.getClass());
					}
				}
			}
		}
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
	
	public static class Builder {
		List<Variable<?>> variables = new ArrayList<Variable<?>>();

		public <S extends Serializable> Builder add(String nombre, S valor) {
			Variable<S> var = new Variable<S>();
			var.setNombre(nombre);
			var.setValor(valor);
			variables.add(var);
			return this;
		}

		public List<Variable<?>> build() {
			return variables;
		}
	}
}
