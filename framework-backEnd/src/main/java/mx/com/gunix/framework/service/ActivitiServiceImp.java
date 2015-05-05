package mx.com.gunix.framework.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import mx.com.gunix.framework.activiti.GunixObjectVariableType;
import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.processes.domain.Tarea;
import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.security.domain.Usuario;

import org.activiti.engine.FormService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ActivitiServiceImp implements ActivitiService {
	@Autowired
	TaskService ts;

	@Autowired
	RuntimeService rs;

	@Autowired
	IdentityService is;

	@Autowired
	FormService fs;

	@Override
	public Instancia completaTarea(Tarea tarea) {
		String taskId = tarea.getId();
		ts.claim(taskId, ((Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getIdUsuario());
		Optional.ofNullable(tarea.getComentario()).ifPresent(comment -> {
			ts.addComment(taskId, tarea.getInstancia().getId(), comment);
		});
		Map<String, Object>[] variablesMaps = toMap(tarea.getVariables());
		Map<String, Object> variablesProcesoMap = variablesMaps[Variable.Scope.PROCESO.ordinal()];
		Map<String, Object> variablesTareaMap = variablesMaps[Variable.Scope.TAREA.ordinal()];
		
		GunixObjectVariableType.setCurrentTarea(tarea);
				
		if (!variablesProcesoMap.isEmpty()) {
			rs.setVariables(tarea.getInstancia().getId(), variablesProcesoMap);
		}
		if (!variablesTareaMap.isEmpty()) {
			rs.setVariablesLocal(tarea.getExecutionId(), variablesProcesoMap);
		}
		ts.complete(taskId, variablesMaps[Variable.Scope.TAREA.ordinal()]);
		tarea.getInstancia().setTareaActual(getCurrentTask(tarea.getInstancia().getId()));
		if(tarea.getInstancia().getTareaActual()==null){
			tarea.getInstancia().setTareaActual(Tarea.DEFAULT_END_TASK);
		}else{
			tarea.getInstancia().getTareaActual().setInstancia(tarea.getInstancia());	
		}
		return tarea.getInstancia();
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object>[] toMap(List<Variable<?>> variables) {
		Map<String, Object> variablesProcesoMap = new HashMap<String, Object>();
		Map<String, Object> variablesTareaMap = new HashMap<String, Object>();
		Optional.ofNullable(variables).ifPresent(vars -> {
			vars.stream().forEach(variable -> {
				if(variable!=null && variable.getValor()!=null){					
					switch (variable.getScope()) {
						case PROCESO:
							variablesProcesoMap.put(variable.getNombre(), variable.getValor());
							break;
						case TAREA:
							variablesTareaMap.put(variable.getNombre(), variable.getValor());
							break;
					}
				}
			});
		});
		Map<String, Object>[] variablesMaps = new Map[2];
		variablesMaps[Variable.Scope.PROCESO.ordinal()] = variablesProcesoMap;
		variablesMaps[Variable.Scope.TAREA.ordinal()] = variablesTareaMap;
		return variablesMaps;
	}

	@Override
	public Instancia iniciaProceso(String processKey, List<Variable<?>> variables, String comentario) {
		Instancia instancia = null;
		try {
			String usuario = ((Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getIdUsuario();
			is.setAuthenticatedUserId(usuario);
			ProcessInstance pi = rs.startProcessInstanceByKey(processKey, toMap(variables)[Variable.Scope.PROCESO.ordinal()]);
			Optional.ofNullable(comentario).ifPresent(comment -> {
				ts.addComment(null, pi.getProcessInstanceId(), comment);
			});

			instancia = new Instancia();
			instancia.setId(pi.getId());
			instancia.setComentario(comentario);
			instancia.setProcessKey(pi.getBusinessKey());
			instancia.setUsuario(usuario);
			instancia.setVariables(Collections.unmodifiableList(variables));

			Tarea currTask = getCurrentTask(pi.getId());
			currTask.setInstancia(instancia);
			instancia.setTareaActual(currTask);
		} finally {
			is.setAuthenticatedUserId(null);
		}
		return instancia;
	}

	private Tarea getCurrentTask(String piid) {
		Tarea tarea = null;
		Task task = ts.createTaskQuery().active().processInstanceId(piid).singleResult();
		if (task != null) {
			tarea = new Tarea();
			tarea.setId(task.getId());
			tarea.setExecutionId(task.getExecutionId());
			tarea.setInicio(task.getCreateTime());
			TaskFormData fd = fs.getTaskFormData(tarea.getId());
			if (fd != null) {
				tarea.setVista(fd.getFormKey());
			}
		}
		return tarea;
	}
}
