package mx.com.gunix.framework.service;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.processes.domain.ProgressUpdate;
import mx.com.gunix.framework.processes.domain.Tarea;
import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.security.domain.Usuario;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.bpmn.behavior.CancelEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ErrorEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.NoneEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.TerminateEndEventActivityBehavior;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
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

	@Autowired
	HistoryService hs;

	@Autowired
	RepositoryService repos;
	
	@Autowired
	RedisTemplate<String, ProgressUpdate> redisProgressUpdateTemplate;
	
	public static final String CURRENT_AUTHENTICATION_USUARIO_VAR = "CURRENT_AUTHENTICATION_USUARIO_VAR";
	public static final String CURRENT_AUTHENTICATION_ROLES_VAR = "CURRENT_AUTHENTICATION_ROLES_VAR";
	private final int MAX_UPDATES_PER_FETCH = 100;
	private static final String ID_APLICACION_VAR = "ID_APLICACION";
	private final String ID_APLICACION = System.getenv(ID_APLICACION_VAR);

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

		if (!variablesProcesoMap.isEmpty()) {
			rs.setVariables(tarea.getInstancia().getId(), variablesProcesoMap);
		}

		if (!variablesTareaMap.isEmpty()) {
			ts.setVariablesLocal(tarea.getId(), variablesProcesoMap);
		}

		ts.complete(taskId, variablesMaps[Variable.Scope.TAREA.ordinal()]);
		tarea.getInstancia().setTareaActual(getCurrentTask(tarea.getInstancia().getId()));

		if (tarea.getInstancia().getTareaActual() == null) {
			tarea.getInstancia().setTareaActual(Tarea.DEFAULT_END_TASK);
		} else {
			tarea.getInstancia().getTareaActual().setInstancia(tarea.getInstancia());
		}

		if (tarea.getInstancia().getTareaActual().isTerminal()) {
			ts.complete(tarea.getInstancia().getTareaActual().getId());
		}

		return tarea.getInstancia();
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object>[] toMap(List<Variable<?>> variables) {
		Map<String, Object> variablesProcesoMap = new HashMap<String, Object>();
		Map<String, Object> variablesTareaMap = new HashMap<String, Object>();
		Optional.ofNullable(variables).ifPresent(vars -> {
			vars.stream().forEach(variable -> {
				if (variable != null) {
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
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String usuario = ((Usuario) auth.getPrincipal()).getIdUsuario();
			is.setAuthenticatedUserId(usuario);
			
			ProcessInstance pi = rs.startProcessInstanceByKey(processKey, toMap(variables)[Variable.Scope.PROCESO.ordinal()]);
			rs.setVariable(pi.getProcessInstanceId(), CURRENT_AUTHENTICATION_USUARIO_VAR, usuario);
			if (auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
				StringBuilder roles = new StringBuilder();
				auth.getAuthorities().forEach(ga -> {
					roles.append(ga.toString()).append(",");
				});
				roles.deleteCharAt(roles.length() - 1);
				rs.setVariable(pi.getProcessInstanceId(), CURRENT_AUTHENTICATION_ROLES_VAR, roles.toString());
			}
			if (ID_APLICACION != null) {
				rs.setVariable(pi.getProcessInstanceId(), ID_APLICACION_VAR, ID_APLICACION);
			}
			instancia = new Instancia();
			instancia.setId(pi.getId());
			instancia.setComentario(comentario);
			instancia.setProcessKey(pi.getBusinessKey());
			instancia.setUsuario(usuario);
			instancia.setVariables(Collections.unmodifiableList(variables));
			
			if (!pi.isEnded()) {
				Optional.ofNullable(comentario).ifPresent(comment -> {
					ts.addComment(null, pi.getProcessInstanceId(), comment);
				});
				Tarea currTask = getCurrentTask(pi.getId());
				currTask.setInstancia(instancia);
				instancia.setTareaActual(currTask);
			} else {
				instancia.setTareaActual(Tarea.DEFAULT_END_TASK);
			}
		} finally {
			is.setAuthenticatedUserId(null);
		}
		return instancia;
	}
	
	@Override
	public Serializable getVar(Instancia instancia, String varName) {
		if (instancia.getTareaActual() == null) {
			throw new IllegalArgumentException("La tarea actual no debe ser null");
		}
		Serializable ser = null;
		if (!instancia.getTareaActual().isTerminal()) {
			ser = rs.getVariable(instancia.getId(), varName, Serializable.class);
			ser = (ser == null ? getVar(instancia.getTareaActual(), varName) : ser);
		} else {
			List<HistoricVariableInstance> hvis = hs.createHistoricVariableInstanceQuery().processInstanceId(instancia.getId()).excludeTaskVariables().variableName(varName).list();
			if (hvis != null && !hvis.isEmpty()) {
				ser = (Serializable) hvis.get(0).getValue();
			} else {
				ser = (ser == null ? getHistVar(instancia.getTareaActual(), varName) : ser);
			}
		}
		return ser;
	}

	private Serializable getHistVar(Tarea tarea, String varName) {
		Serializable ser = null;
		List<HistoricVariableInstance> hvis = hs.createHistoricVariableInstanceQuery().processInstanceId(tarea.getInstancia().getId()).taskId(tarea.getId()).variableName(varName).list();
		if (hvis != null && !hvis.isEmpty()) {
			ser = (Serializable) hvis.get(0).getValue();
		}
		return ser;
	}

	private Serializable getVar(Tarea tarea, String varName) {
		return ts.getVariableLocal(tarea.getId(), varName, Serializable.class);
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
			TaskEntity te = (TaskEntity) task;
			ProcessDefinitionImpl pdfimp = (ProcessDefinitionImpl) repos.getProcessDefinition(te.getProcessDefinitionId());
			List<PvmTransition> oTrans = pdfimp.findActivity(te.getTaskDefinitionKey()).getOutgoingTransitions();
			if (oTrans.size() >= 1) {
				if (oTrans.isEmpty()) {
					tarea.setTerminal(true);
				} else {
					ActivityImpl pva = (ActivityImpl) oTrans.get(0).getDestination();
					if ((pva.getActivityBehavior() instanceof TerminateEndEventActivityBehavior) || (pva.getActivityBehavior() instanceof ErrorEndEventActivityBehavior)
							|| (pva.getActivityBehavior() instanceof NoneEndEventActivityBehavior) || (pva.getActivityBehavior() instanceof CancelEndEventActivityBehavior)) {
						tarea.setTerminal(true);
					}
				}
			}
		}
		return tarea;
	}

	// A las 00:00 todos los días
	@Scheduled(cron = "0 0 0 * * *")
	public void eliminaTodasLasInstanciasVolatilesTerminadasOIniciadasHaceMasDe35Minutos() {
		if(Boolean.valueOf(System.getenv("ACTIVITI_MASTER"))) {
			List<ProcessDefinition> pDefsVolatiles = repos.createProcessDefinitionQuery().processDefinitionCategory("VOLATIL").latestVersion().list();
			Date hace35Minutos = Date.from(Instant.now().minus(35, ChronoUnit.MINUTES));
			pDefsVolatiles.parallelStream().forEach(
					pd -> {
						List<HistoricProcessInstance> hpis = hs.createHistoricProcessInstanceQuery().processDefinitionKey(pd.getKey()).startedBefore(hace35Minutos).list();
						
						if (ID_APLICACION != null) {
							if (hpis != null && !hpis.isEmpty()) {
								rs.createProcessInstanceQuery()
										.processInstanceIds(hpis.stream().map(hpi -> hpi.getId()).collect(Collectors.toSet()))
										.variableValueEquals(ID_APLICACION_VAR, ID_APLICACION)
										.list()
										.forEach(pi -> {
											rs.deleteProcessInstance(pi.getId(), "");
										});
							}
							hpis = hs.createHistoricProcessInstanceQuery().processDefinitionKey(pd.getKey()).finished().variableValueEquals(ID_APLICACION_VAR, ID_APLICACION).list();
						} else {
							hpis.forEach(hpi -> {
								rs.deleteProcessInstance(hpi.getId(), "");
							});
							hpis = hs.createHistoricProcessInstanceQuery().processDefinitionKey(pd.getKey()).finished().list();
						}
						hpis.parallelStream().forEach(hpi -> {
							hs.deleteHistoricProcessInstance(hpi.getId());
						});
					});
		}
	}

	@Override
	public List<ProgressUpdate> getRecentProgressUpdates(String processId) {
		List<ProgressUpdate> pu = new ArrayList<ProgressUpdate>();

		ProgressUpdate cpud = null;
		int updatesFetched = 0;
		while ((cpud = redisProgressUpdateTemplate.boundListOps(processId).rightPop()) != null && updatesFetched < MAX_UPDATES_PER_FETCH) {
			pu.add(cpud);
			updatesFetched++;
		}
		return pu;
	}

	public void addProgressUpdate(String processId, ProgressUpdate pu) {
		BoundListOperations<String, ProgressUpdate> blops = redisProgressUpdateTemplate.boundListOps(processId);
		blops.expire(2, TimeUnit.MINUTES);
		blops.leftPush(pu);
	}
}
