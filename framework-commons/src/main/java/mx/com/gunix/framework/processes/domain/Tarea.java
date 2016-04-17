package mx.com.gunix.framework.processes.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Tarea implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final Tarea DEFAULT_END_TASK = new Tarea();
	public static final String DEFAULT_END_TASK_VIEW = "DEFAULT_END_TASK_VIEW";

	static {
		DEFAULT_END_TASK.setVista(DEFAULT_END_TASK_VIEW);
	}

	private String id;
	private String executionId;
	private List<String> rolesCandidatos;
	private List<Variable<?>> variables;
	private String comentario;
	private String usuario;
	private Date inicio;
	private Date termino;
	private String vista;
	private Instancia instancia;
	private boolean terminal;
	private List<String> transiciones;
	private String nombre;

	public boolean isTerminal() {
		return terminal;
	}

	public void setTerminal(boolean terminal) {
		this.terminal = terminal;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getRolesCandidatos() {
		return rolesCandidatos;
	}

	public void setRolesCandidatos(List<String> rolesCandidatos) {
		this.rolesCandidatos = rolesCandidatos;
	}

	public List<Variable<?>> getVariables() {
		return variables;
	}

	public void setVariables(List<Variable<?>> variables) {
		this.variables = variables;
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

	public String getVista() {
		return vista;
	}

	public void setVista(String vista) {
		this.vista = vista;
	}

	public Instancia getInstancia() {
		return instancia;
	}

	public void setInstancia(Instancia instancia) {
		this.instancia = instancia;
	}

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public List<String> getTransiciones() {
		return transiciones;
	}

	public void setTransiciones(List<String> transiciones) {
		this.transiciones = transiciones;
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
		result = prime * result + ((executionId == null) ? 0 : executionId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((instancia == null) ? 0 : instancia.hashCode());
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
		Tarea other = (Tarea) obj;
		if (executionId == null) {
			if (other.executionId != null)
				return false;
		} else if (!executionId.equals(other.executionId))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (instancia == null) {
			if (other.instancia != null)
				return false;
		} else if (!instancia.equals(other.instancia))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Tarea [id=" + id + ", usuario=" + usuario + ", inicio=" + inicio + ", vista=" + vista + ", instancia=" + instancia + "]";
	}
}
