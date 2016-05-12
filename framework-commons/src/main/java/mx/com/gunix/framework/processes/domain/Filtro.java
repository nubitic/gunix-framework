package mx.com.gunix.framework.processes.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Filtro<T extends Serializable> extends Variable<T> {
	private static final long serialVersionUID = 1L;

	public static enum Operador {
		IGUAL, MAYOR_QUE, MENOR_QUE, DIFERENTE, LIKE
	}

	private Operador lOp;

	public Operador getlOp() {
		return lOp;
	}

	public void setlOp(Operador lOp) {
		this.lOp = lOp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((lOp == null) ? 0 : lOp.hashCode());
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
		Filtro<?> other = (Filtro<?>) obj;
		if (lOp != other.lOp)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Filtro [" + getNombre() + " " + lOp + " " + getValor() + "]";
	}
	public static class Builder {
		List<Filtro<?>> filtros = new ArrayList<Filtro<?>>();

		public <S extends Serializable> Builder add(String nombre, Operador op, S valor) {
			Filtro<S> filtro = new Filtro<S>();
			filtro.setNombre(nombre);
			filtro.setValor(valor);
			filtro.setlOp(op);
			filtros.add(filtro);
			return this;
		}

		public List<Filtro<?>> build() {
			return filtros;
		}
	}
}
