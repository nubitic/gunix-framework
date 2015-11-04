package mx.com.gunix.framework.security.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import mx.com.gunix.framework.domain.HashCodeByTimeStampAware;
import mx.com.gunix.framework.domain.Identificador;
import mx.com.gunix.framework.domain.validation.GunixValidationGroups.BeanValidations;
import mx.com.gunix.framework.security.domain.validation.ValidaFuncion;

@ValidaFuncion(groups = BeanValidations.class)
public class Funcion extends HashCodeByTimeStampAware implements Serializable {
	public static enum Horario {
		LD24("Lunes a Domingo 24 Horas"), LV24("Lunes a Viernes 24 Horas"), LV9_18("Lunes a Viernes de 09 a 18"), PERSONALIZADO("Personalizado...");
		private final String label;

		private Horario(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}
	}

	public static enum Acceso {
		COMPLETO, PUNTUAL;
	}

	private static final long serialVersionUID = 1L;

	@NotNull
	@Size(min = 1, max = 30)
	@Identificador
	private String idFuncion;

	@NotNull(groups = BeanValidations.class)
	@Identificador
	private Modulo modulo;

	@NotNull
	@Size(min = 1, max = 20)
	private String titulo;

	@NotNull
	@Size(min = 1, max = 100)
	private String descripcion;

	@Size(min = 0, max = 255)
	private String processKey;

	@NotNull
	private float orden;

	@NotNull
	private Horario horario;

	private Acceso acceso;

	public Acceso getAcceso() {
		return acceso;
	}

	public void setAcceso(Acceso acceso) {
		this.acceso = acceso;
	}

	public Horario getHorario() {
		return horario;
	}

	public void setHorario(Horario horario) {
		this.horario = horario;
	}

	private Funcion padre;
	private List<Funcion> hijas;
	private List<Parametro> parametros;

	public String getIdFuncion() {
		return idFuncion;
	}

	public void setIdFuncion(String idFuncion) {
		this.idFuncion = idFuncion;
	}

	public Modulo getModulo() {
		return modulo;
	}

	public void setModulo(Modulo modulo) {
		this.modulo = modulo;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getProcessKey() {
		return processKey;
	}

	public void setProcessKey(String processKey) {
		this.processKey = processKey;
	}

	public float getOrden() {
		return orden;
	}

	public void setOrden(float orden) {
		this.orden = orden;
	}

	public Funcion getPadre() {
		return padre;
	}

	public void setPadre(Funcion padre) {
		this.padre = padre;
	}

	public List<Funcion> getHijas() {
		return hijas;
	}

	public void setHijas(List<Funcion> hijas) {
		this.hijas = hijas;
	}

	public List<Parametro> getParametros() {
		return parametros;
	}

	public void setParametros(List<Parametro> parametros) {
		this.parametros = parametros;
	}

	@Override
	public int doHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idFuncion == null) ? 0 : idFuncion.hashCode());
		result = prime * result + ((modulo == null) ? 0 : modulo.hashCode());
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
		Funcion other = (Funcion) obj;
		if (idFuncion == null) {
			if (other.idFuncion != null)
				return false;
		} else if (!idFuncion.equals(other.idFuncion))
			return false;
		if (modulo == null) {
			if (other.modulo != null)
				return false;
		} else if (!modulo.equals(other.modulo))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Funcion [idFuncion=" + idFuncion + ", modulo=" + modulo + ", titulo=" + titulo + ", processKey=" + processKey + "]";
	}

	public static List<Funcion> jerarquizaFunciones(Modulo modulo, List<Funcion> funciones) {
		Objects.requireNonNull(funciones);
		List<Funcion> funcionesReacomodadas = new ArrayList<Funcion>();
		funciones.stream().forEach(funcion -> {
			funcion.setModulo(modulo);
			if (funcion.getPadre() != null) {
				Optional<Funcion> padre = funciones.stream().filter(posiblePadre -> (posiblePadre.getIdFuncion().equals(funcion.getPadre().getIdFuncion()))).findFirst();
				padre.ifPresent(p -> {
					funcion.setPadre(p);
					List<Funcion> hijas = p.getHijas();
					if (hijas == null) {
						p.setHijas((hijas = new ArrayList<Funcion>()));
					}
					hijas.add(funcion);
					funcionesReacomodadas.add(funcion);
				});
			}
		});

		return funciones.stream().filter(funcion -> (!funcionesReacomodadas.contains(funcion))).collect(Collectors.toList());
	}
}
