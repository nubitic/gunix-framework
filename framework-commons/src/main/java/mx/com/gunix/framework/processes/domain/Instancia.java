package mx.com.gunix.framework.processes.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Instancia implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public Instancia(){
		
	}
	
	private String processKey;
	private String id;
	private List<Variable<?>> variables;
	private String comentario;
	private String usuario;
	private Date inicio;
	private Date termino;
	private List<Tarea> tareas;
	private Tarea tareaActual;
	private boolean volatil = true;

	public String getProcessKey() {
		return processKey;
	}

	public void setProcessKey(String processKey) {
		this.processKey = processKey;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Variable<?>> getVariables() {
		return variables;
	}

	public void setVariables(List<Variable<?>> variables) {
		this.variables = variables;
	}
	
	@SuppressWarnings("unchecked")
	public <S extends Serializable> S findVariable(String nombre, Class<S> expectedType) {
		S s = null;
		if (variables != null) {
			Variable<S> var = (Variable<S>) variables
												.stream()
												.filter(varF -> varF.getNombre().equals(nombre))
												.findFirst()
												.orElse(null);
			if (var != null) {
				s = var.getValor();
			}
		}
		return s;
	}
	
	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public Date getInicio() {
		return inicio;
	}

	public void setInicio(Date inicio) {
		this.inicio = inicio;
	}

	public Date getTermino() {
		return termino;
	}

	public void setTermino(Date termino) {
		this.termino = termino;
	}

	public List<Tarea> getTareas() {
		return tareas;
	}

	public void setTareas(List<Tarea> tareas) {
		this.tareas = tareas;
	}

	public Tarea getTareaActual() {
		return tareaActual;
	}

	public void setTareaActual(Tarea tareaActual) {
		this.tareaActual = tareaActual;
	}

	public boolean isVolatil() {
		return volatil;
	}

	public void setVolatil(boolean volatil) {
		this.volatil = volatil;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((processKey == null) ? 0 : processKey.hashCode());
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
		Instancia other = (Instancia) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (processKey == null) {
			if (other.processKey != null)
				return false;
		} else if (!processKey.equals(other.processKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Instancia [processKey=" + processKey + ", id=" + id + ", usuario=" + usuario + ", inicio=" + inicio + ", termino=" + termino + "]";
	}
}
