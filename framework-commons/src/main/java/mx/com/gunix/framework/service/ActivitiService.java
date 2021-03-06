package mx.com.gunix.framework.service;

import java.io.Serializable;
import java.util.List;

import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.processes.domain.ProgressUpdate;
import mx.com.gunix.framework.processes.domain.Tarea;
import mx.com.gunix.framework.processes.domain.Variable;

import com.hunteron.core.Hessian;

@Hessian("/activitiService")
public interface ActivitiService {
	static final String VOLATIL = "VOLATIL";

	public List<ProgressUpdate> getRecentProgressUpdates(String processId);

	public void addProgressUpdate(String processId, ProgressUpdate pu);

	public Serializable getVar(Instancia instancia, String varName);

	public Instancia getInstancia(String processInstanceId);

	public Instancia completaTarea(Tarea tarea);

	public Instancia iniciaProceso(String processKey, List<Variable<?>> variables, String comentario);
}
