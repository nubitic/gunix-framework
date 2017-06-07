package mx.com.gunix.framework.processes.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public final class Filtro<T extends Serializable> extends Variable<T> {
	private static final long serialVersionUID = 1L;
	public static final String FILTRO_ESTATUS = "FILTRO_ESTATUS_";
	public static final String FILTRO_ENDED ="FILTRO_ENDED_";
	public static final String FILTRO_INICIO_BETWEEN ="FILTRO_INICIO_BETWEEN_";
	public static final String FILTRO_FIN_BETWEEN ="FILTRO_FIN_BETWEEN_";
	public static final String FILTRO_GLOBAL ="FILTRO_GLOBAL_";

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
		result = prime * result + ((lOp == null) ? 0 : lOp.ordinal());
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

		public <S extends Serializable> Builder addGlobal(S valor) {
			Filtro<S> filtro = new Filtro<S>();
			filtro.setNombre(FILTRO_GLOBAL);
			filtro.setValor(valor);
			filtro.setlOp(Operador.IGUAL);
			filtros.add(filtro);
			return this;
		}
		public <S extends Serializable> Builder estatusEs(String estatus){
			Filtro<String> filtro = new Filtro<String>();
			filtro.setNombre(FILTRO_ESTATUS);
			filtro.setValor(estatus);
			filtro.setlOp(Operador.IGUAL);
			filtros.add(filtro);
			return this;
		}
		
		public <S extends Serializable> Builder terminados() {
			Filtro<Boolean> filtro = new Filtro<Boolean>();
			filtro.setNombre(FILTRO_ENDED);
			filtro.setValor(Boolean.TRUE);
			filtro.setlOp(Operador.IGUAL);
			filtros.add(filtro);
			return this;
		}
		
		public <S extends Serializable> Builder enProceso() {
			Filtro<Boolean> filtro = new Filtro<Boolean>();
			filtro.setNombre(FILTRO_ENDED);
			filtro.setValor(Boolean.FALSE);
			filtro.setlOp(Operador.IGUAL);
			filtros.add(filtro);
			return this;
		}
		/**
		 * Establece un filtro para buscar instancias registradas entre la fecha inicial dada y la fecha final, si no se proporciona una fecha final entonces el filtro 
		 * buscará todas aquellas instancias registradas posteriores a la fecha inicial, por el contrario, si no se establece una fecha inicial entonces el filtro
		 * buscará todas aquellas instancias registradas hasta la fecha final.
		 * @param fechaInicial Se ajustan las horas, minutos, segundos y milisegundos a cero
		 * @param fechaFinal Se ajustan las horas, minutos, segundos y milisegundos a 23, 59, 59 y 9999 respectivamente
		 * @return El Builder para continuar agregando filtros
		 */
		public <S extends Serializable> Builder registradoEntre(Date fechaInicial, Date fechaFinal){
			if (fechaInicial == null && fechaFinal == null) {
				throw new IllegalArgumentException("Al menos una de las fechas debe ser diferente a null");
			}
			
			Filtro<Date> filtro = new Filtro<Date>();
			filtro.setNombre(FILTRO_INICIO_BETWEEN);
			filtro.setValor(ajustaHoraMinutoSegundo(fechaInicial, true));
			filtro.setlOp(Operador.MAYOR_QUE);
			filtros.add(filtro);
			
			filtro = new Filtro<Date>();
			filtro.setNombre(FILTRO_INICIO_BETWEEN);
			filtro.setValor(ajustaHoraMinutoSegundo(fechaFinal, false));
			filtro.setlOp(Operador.MENOR_QUE);
			filtros.add(filtro);
			return this;
		}
		/**
		 * Establece un filtro para buscar instancias finalizadas entre la fecha inicial dada y la fecha final, si no se proporciona una fecha final entonces el filtro 
		 * buscará todas aquellas instancias finalizadas posteriormente a la fecha inicial, por el contrario, si no se establece una fecha inicial entonces el filtro
		 * buscará todas aquellas instancias finalizadas hasta la fecha final.
		 * @param fechaInicial Se ajustan las horas, minutos, segundos y milisegundos a cero
		 * @param fechaFinal Se ajustan las horas, minutos, segundos y milisegundos a 23, 59, 59 y 9999 respectivamente
		 * @return El Builder para continuar agregando filtros
		 */
		public <S extends Serializable> Builder finalizadoEntre(Date fechaInicial, Date fechaFinal){
			if (fechaInicial == null && fechaFinal == null) {
				throw new IllegalArgumentException("Al menos una de las fechas debe ser diferente a null");
			}
			
			Filtro<Date> filtro = new Filtro<Date>();
			filtro.setNombre(FILTRO_FIN_BETWEEN);
			filtro.setValor(ajustaHoraMinutoSegundo(fechaInicial, true));
			filtro.setlOp(Operador.MAYOR_QUE);
			filtros.add(filtro);
			
			filtro = new Filtro<Date>();
			filtro.setNombre(FILTRO_FIN_BETWEEN);
			filtro.setValor(ajustaHoraMinutoSegundo(fechaFinal, false));
			filtro.setlOp(Operador.MENOR_QUE);
			filtros.add(filtro);
			return this;
		}
		
		private Date ajustaHoraMinutoSegundo(Date date, boolean isACero) {
			if (date != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				cal.set(Calendar.HOUR_OF_DAY, isACero ? 0 : 23);
				cal.set(Calendar.MINUTE, isACero ? 0 : 59);
				cal.set(Calendar.SECOND, isACero ? 0 : 59);
				cal.set(Calendar.MILLISECOND, isACero ? 0 : 9999);
				return cal.getTime();
			} else {
				return null;
			}
		}

		public List<Filtro<?>> build() {
			return filtros;
		}
	}
}
