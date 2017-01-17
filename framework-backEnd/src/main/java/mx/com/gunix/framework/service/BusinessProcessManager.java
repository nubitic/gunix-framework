package mx.com.gunix.framework.service;

import java.io.Serializable;
import java.util.List;

import mx.com.gunix.framework.processes.domain.Filtro;
import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.processes.domain.Tarea;
import mx.com.gunix.framework.processes.domain.Variable;

public interface BusinessProcessManager {
	public static final Integer NO_LIMIT = -999;

	public Serializable getVar(Instancia instancia, String varName);

	public void setVar(Instancia instancia, String varName, Serializable varValue);

	public Instancia getInstancia(String processInstanceId);

	public Instancia completaTarea(Tarea tarea);

	public Instancia iniciaProceso(String processKey, List<Variable<?>> variables, String comentario);

	public List<Instancia> getPendientes(String processKey, List<Filtro<?>> filtros, Integer maxResults, String... projectionVars);

	public List<Instancia> consulta(String processKey, List<Filtro<?>> filtros, Integer maxResults, String... projectionVars);
	
	public List<Instancia> getPendientes(String processKey, List<Filtro<?>> filtros, String... projectionVars);

	public List<Instancia> consulta(String processKey, List<Filtro<?>> filtros, String... projectionVars);

	public List<String> getEstadosProceso(String processKey);
}
