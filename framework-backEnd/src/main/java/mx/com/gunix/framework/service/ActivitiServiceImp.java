package mx.com.gunix.framework.service;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.GunixVariableHistoricProcessInstanceQuery;
import org.activiti.engine.impl.GunixVariableProcessInstanceQuery;
import org.activiti.engine.impl.bpmn.behavior.CancelEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ErrorEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.NoneEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.TerminateEndEventActivityBehavior;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.com.gunix.framework.activiti.GunixVariableSerializer;
import mx.com.gunix.framework.activiti.persistence.entity.GunixObjectVariableType;
import mx.com.gunix.framework.activiti.persistence.entity.VariableInstanceMapper;
import mx.com.gunix.framework.processes.domain.Filtro;
import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.processes.domain.ProgressUpdate;
import mx.com.gunix.framework.processes.domain.Tarea;
import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.security.UserDetails;
import mx.com.gunix.framework.security.domain.Usuario;

@Service
@Transactional(rollbackFor = Exception.class)
public class ActivitiServiceImp implements ActivitiService, BusinessProcessManager {
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
	
	@Autowired
	@Lazy
	VariableInstanceMapper vim;
	
	@Autowired
	ProcessEngineConfiguration processEngineConfig;
	
	public static final String CURRENT_AUTHENTICATION_USUARIO_VAR = "CURRENT_AUTHENTICATION_USUARIO_VAR";
	private final int MAX_UPDATES_PER_FETCH = 100;
	private static final String ID_APLICACION_VAR = "ID_APLICACION";

	private static final String PROCESS_CREATION_COMMENT = "PROCESS_CREATION_COMMENT";
	private static final String TASK_COMMENT = "TASK_COMMENT";
	
	private final String ID_APLICACION = System.getenv(ID_APLICACION_VAR);

	@Override
	public Instancia completaTarea(Tarea tarea) {
		try {
			GunixObjectVariableType.setCurrentInstancia(tarea.getInstancia());
			String taskId = tarea.getId();
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String usuario = ((Usuario) auth.getPrincipal()).getIdUsuario();
			is.setAuthenticatedUserId(usuario);
			ts.claim(taskId, usuario);
			Optional.ofNullable(tarea.getComentario()).ifPresent(comment -> {
				ts.addComment(taskId, tarea.getInstancia().getId(), TASK_COMMENT, comment);
			});
			Map<String, Object>[] variablesMaps = toMap(tarea.getVariables());
			Map<String, Object> variablesProcesoMap = variablesMaps[Variable.Scope.PROCESO.ordinal()];

			GunixObjectVariableType.setCurrentTarea(tarea);

			ts.complete(taskId,variablesProcesoMap);
			tarea.getInstancia().setTareaActual(getCurrentTask(tarea.getInstancia().getId(), null));

			if (tarea.getInstancia().getTareaActual() == null) {
				tarea.getInstancia().setTareaActual(Tarea.DEFAULT_END_TASK);
			} else {
				tarea.getInstancia().getTareaActual().setInstancia(tarea.getInstancia());
			}

			if (tarea.getInstancia().getTareaActual().isTerminal()) {
				ts.complete(tarea.getInstancia().getTareaActual().getId());
			}

			return tarea.getInstancia();
		} finally {
			is.setAuthenticatedUserId(null);
			GunixObjectVariableType.removeCurrentInstancia();
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object>[] toMap(List<Variable<?>> variables) {
		Map<String, Object> variablesProcesoMap = new HashMap<String, Object>();
		Optional.ofNullable(variables).ifPresent(vars -> {
			vars.stream().forEach(variable -> {
				if (variable != null) {
					switch (variable.getScope()) {
					case PROCESO:
						variablesProcesoMap.put(variable.getNombre(), variable.getValor());
						break;
					}
				}
			});
		});
		Map<String, Object>[] variablesMaps = new Map[2];
		variablesMaps[Variable.Scope.PROCESO.ordinal()] = variablesProcesoMap;
		return variablesMaps;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Instancia iniciaProceso(String processKey, List<Variable<?>> variables, String comentario) {
		Instancia instancia = null;
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			UserDetails usuario = ((UserDetails) auth.getPrincipal());
			is.setAuthenticatedUserId(usuario.getIdUsuario());
			
			ProcessInstance pi = rs.startProcessInstanceByKey(processKey, toMap(variables)[Variable.Scope.PROCESO.ordinal()]);
			if (pi != null) {
				UserDetails usuarioSimpl = new UserDetails(usuario);
				usuarioSimpl.setAuthorities((List<GrantedAuthority>) usuario.getAuthorities());
				usuarioSimpl.setSelectedAuthority(usuario.getSelectedAuthority());
				usuarioSimpl.setAplicaciones(null);
				rs.setVariable(pi.getProcessInstanceId(), CURRENT_AUTHENTICATION_USUARIO_VAR, usuarioSimpl);
				if (ID_APLICACION != null) {
					rs.setVariable(pi.getProcessInstanceId(), ID_APLICACION_VAR, ID_APLICACION);
				}
				instancia = new Instancia();
				instancia.setId(pi.getId());
				instancia.setProcessDefinitionId(pi.getProcessDefinitionId());
				instancia.setComentario(comentario);
				instancia.setProcessKey(pi.getBusinessKey());
				instancia.setUsuario(usuario.getIdUsuario());
				instancia.setVariables(Collections.unmodifiableList(variables));
				instancia.setVolatil(VOLATIL.equals(repos.getProcessDefinition(pi.getProcessDefinitionId()).getCategory()));

				if (!pi.isEnded()) {
					Optional.ofNullable(comentario).ifPresent(comment -> {
						ts.addComment(null, pi.getProcessInstanceId(), PROCESS_CREATION_COMMENT, comment);
					});
					Tarea currTask = getCurrentTask(pi.getProcessInstanceId(), pi);
					currTask.setInstancia(instancia);
					instancia.setTareaActual(currTask);
				} else {
					instancia.setTareaActual(Tarea.DEFAULT_END_TASK);
				}
			} else {
				if (Context.getCommandContext() != null && Context.getCommandContext().getException() != null) {
					throw new RuntimeException("Error al intentar crear una nueva instancia del proceso " + processKey, Context.getCommandContext().getException());
				}
			}
		} finally {
			is.setAuthenticatedUserId(null);
			GunixObjectVariableType.removeCurrentInstancia();
		}
		return instancia;
	}
	
	@Override
	public Serializable getVar(Instancia instancia, String varName) {
		try {
			GunixObjectVariableType.setCurrentInstancia(instancia);
			if (instancia.isVolatil() && instancia.getTareaActual() == null) {
				throw new IllegalArgumentException("Para procesos volatiles la tarea actual no debe ser null");
			}
			Serializable ser = null;
			if (((instancia.isVolatil() && !instancia.getTareaActual().isTerminal()) || !instancia.isVolatil()) && instancia.getTermino() == null) {
				ser = rs.getVariable(instancia.getId(), varName, Serializable.class);
				if (!instancia.isVolatil() && ser == null) {
					List<Map<String, Object>> vars = vim.findGunixObjectByNameAndExecutionId(instancia.getId(), varName);
					if (vars != null && !vars.isEmpty()) {
						ser = (Serializable) GunixVariableSerializer.deserialize(varName, vars, getClass().getClassLoader());
					}
				}
			} else {
				List<HistoricVariableInstance> hvis = hs.createHistoricVariableInstanceQuery().processInstanceId(instancia.getId()).excludeTaskVariables().variableName(varName).list();
				if (hvis != null && !hvis.isEmpty()) {
					ser = (Serializable) hvis.get(0).getValue();
				} else {
					if (!instancia.isVolatil()) {
						List<Map<String, Object>> vars = vim.findHistoricGunixObjectByNameAndExecutionId(instancia.getId(), varName);
						if (vars != null && !vars.isEmpty()) {
							ser = (Serializable) GunixVariableSerializer.deserialize(varName, vars, getClass().getClassLoader());
						}
					}
				}
			}
			return ser;
		} finally {
			GunixObjectVariableType.removeCurrentInstancia();
		}
	}

	private Tarea getCurrentTask(String piid, ProcessInstance pi) {
		Tarea tarea = null;
		Task task =null;
		
		if (validaTasksPrecargadas(pi)) {
			task = ((ExecutionEntity) pi).getTasks().get(0);
		} else {
			task = ts.createTaskQuery().active().processInstanceId(piid).singleResult();
		}
		
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
			if (oTrans != null) {
				if (oTrans.isEmpty()) {
					tarea.setTerminal(true);
				} else {
					if (oTrans.size() == 1) {
						ActivityImpl pva = (ActivityImpl) oTrans.get(0).getDestination();
						if ((pva.getActivityBehavior() instanceof TerminateEndEventActivityBehavior) || (pva.getActivityBehavior() instanceof ErrorEndEventActivityBehavior) || (pva.getActivityBehavior() instanceof NoneEndEventActivityBehavior) || (pva.getActivityBehavior() instanceof CancelEndEventActivityBehavior)) {
							tarea.setTerminal(true);
						} else {
							addTransitions(tarea, oTrans);
						}
					} else {
						addTransitions(tarea, oTrans);
					}
				}
			} else {
				tarea.setTerminal(true);
			}
		}
		return tarea;
	}

	private boolean validaTasksPrecargadas(ProcessInstance pi) {
		try {
			return (pi != null && ((ExecutionEntity) pi).getTasks() != null);
		} catch (NullPointerException ignorar) {
			return false;
		}
	}

	private void addTransitions(Tarea tarea, List<PvmTransition> oTrans) {
		tarea.setTransiciones(new ArrayList<String>());
		oTrans.forEach(trans -> {
			tarea.getTransiciones().add(new StringBuilder(trans.getId() != null ? trans.getId().trim() : "").append(" ").append(trans.getDestination().getId() != null ? trans.getDestination().getId().trim() : "").toString());
		});
	}

	// A las 00:00 todos los días
	@Scheduled(cron = "0 0 0 * * *")
	public void eliminaTodasLasInstanciasVolatilesTerminadasOIniciadasHaceMasDe35Minutos() {
		if(Boolean.valueOf(System.getenv("ACTIVITI_MASTER"))) {
			doDelete();
			doDelete();// <---- Para eliminar lo que la primera no pudo debido a problemas de concurrencia.
		}
	}

	private void doDelete() {
		List<ProcessDefinition> pDefsVolatiles = repos.createProcessDefinitionQuery().processDefinitionCategory(VOLATIL).latestVersion().list();
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
									try{
										rs.deleteProcessInstance(pi.getId(), "");
									}catch(ActivitiObjectNotFoundException | ActivitiOptimisticLockingException ignorar){}
								});
					}
					hpis = hs.createHistoricProcessInstanceQuery().processDefinitionKey(pd.getKey()).finished().variableValueEquals(ID_APLICACION_VAR, ID_APLICACION).list();
				} else {
					hpis.forEach(hpi -> {
						try{
							rs.deleteProcessInstance(hpi.getId(), "");
						}catch(ActivitiObjectNotFoundException | ActivitiOptimisticLockingException ignorar){}
					});
					hpis = hs.createHistoricProcessInstanceQuery().processDefinitionKey(pd.getKey()).finished().list();
				}
				hpis.parallelStream().forEach(hpi -> {
					try{
						hs.deleteHistoricProcessInstance(hpi.getId());
					}catch(ActivitiObjectNotFoundException | ActivitiOptimisticLockingException ignorar){}
				});
			});
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

	@Override
	public Instancia getInstancia(String processInstanceId) {
		boolean isHistoric = false;

		HistoricProcessInstance hpi = hs.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		isHistoric = hpi.getEndTime() != null;
		
		
		Instancia instancia = new Instancia();
		instancia.setId(processInstanceId);
		instancia.setProcessDefinitionId(hpi.getProcessDefinitionId());
		instancia.setProcessKey(hpi.getProcessDefinitionKey());
		instancia.setInicio(hpi.getStartTime());
		instancia.setTermino(hpi.getEndTime());
		
		List<Comment> processCreationComments = ts.getProcessInstanceComments(processInstanceId, PROCESS_CREATION_COMMENT);
		if (processCreationComments != null && !processCreationComments.isEmpty()) {
			instancia.setComentario(processCreationComments.get(0).getFullMessage());
		}
		
		instancia.setProcessKey(hpi.getBusinessKey());
		
		if (isHistoric) {
			instancia.setUsuario(((UserDetails)hs.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).variableName(CURRENT_AUTHENTICATION_USUARIO_VAR).singleResult().getValue()).getIdUsuario());
		} else {
			instancia.setUsuario(((UserDetails)rs.getVariable(processInstanceId, CURRENT_AUTHENTICATION_USUARIO_VAR)).getIdUsuario());
		}
		
		instancia.setVolatil(VOLATIL.equals(repos.getProcessDefinition(hpi.getProcessDefinitionId()).getCategory()));
		
		List<HistoricTaskInstance> tareasCompletadas = hs.createHistoricTaskInstanceQuery()
																	.processInstanceId(processInstanceId)
																	.finished()
																	.orderByTaskCreateTime().desc()
																	.list();
		if (tareasCompletadas != null) {
			instancia.setTareas(new ArrayList<Tarea>());
			tareasCompletadas.forEach(hti -> {
				Tarea hT = new Tarea();
				hT.setInicio(hti.getCreateTime());
				hT.setTermino(hti.getEndTime());
				hT.setInstancia(instancia);
				hT.setNombre(hti.getName());
				hT.setUsuario(hti.getOwner() == null ? hti.getAssignee() : hti.getOwner());
				List<Comment> taskComments = ts.getTaskComments(hti.getId(), TASK_COMMENT);
				if (taskComments != null && !taskComments.isEmpty()) {
					hT.setComentario(taskComments.get(0).getFullMessage());
				}
				instancia.getTareas().add(hT);
			});
		}
		
		instancia.setTareaActual(getCurrentTask(processInstanceId, null));
		if(instancia.getTareaActual()!=null){
			instancia.getTareaActual().setInstancia(instancia);
		}
		return instancia;
	}
	
	@Override
	public List<Instancia> getPendientes(String processKey, List<Filtro<?>> filtros, String... projectionVars) {
		return doConsulta(processKey, filtros, true, null, BusinessProcessManager.NO_LIMIT, projectionVars);
	}

	@Override
	public List<Instancia> consulta(String processKey, List<Filtro<?>> filtros, String... projectionVars) {
		return doConsulta(processKey, filtros, false, null, BusinessProcessManager.NO_LIMIT, projectionVars);
	}

	@Override
	public List<Instancia> getPendientes(String processKey, List<Filtro<?>> filtros, Integer maxResults, String... projectionVars) {
		return doConsulta(processKey, filtros, true, null, maxResults, projectionVars);
	}

	@Override
	public List<Instancia> consulta(String processKey, List<Filtro<?>> filtros, Integer maxResults, String... projectionVars) {
		return doConsulta(processKey, filtros, false, null, maxResults, projectionVars);
	}
	
	@Override
	public List<Instancia> consulta2(String processKey, List<Filtro<?>> filtros, Integer registroInicial, Integer tamañoPagina, String... projectionVars) {
		return doConsulta(processKey, filtros, false, registroInicial, tamañoPagina, projectionVars);
	}

	@SuppressWarnings("unchecked")
	private List<Instancia> doConsulta(String processKey, List<Filtro<?>> filtros, boolean esParaPendientes, Integer registroInicial, Integer maxResults, String... projectionVars) {
		try{
			Instancia instancia = new Instancia();
			instancia.setVolatil(ActivitiService.VOLATIL.equals(repos.createProcessDefinitionQuery().processDefinitionKey(processKey).latestVersion().singleResult().getCategory()));
			GunixObjectVariableType.setCurrentInstancia(instancia);
			List<Task> tareas = null;
			List<Instancia> pidsEncontrados = new ArrayList<Instancia>();
			Filtro<String> filtroEstatus = null;
			Filtro<Boolean> filtroEnded = null;
			boolean restringirPorFechasInicioTermino = false;
			if (filtros != null && !filtros.isEmpty()) {
				filtroEstatus = (Filtro<String>) filtros.stream().filter(f -> f.getNombre() == Filtro.FILTRO_ESTATUS).findFirst().orElse(null);
				filtroEnded = (Filtro<Boolean>) filtros.stream().filter(f -> f.getNombre() == Filtro.FILTRO_ENDED).findFirst().orElse(null);
			}
			
			if ((filtroEstatus != null && filtroEnded != null) || (esParaPendientes && (filtroEstatus != null || filtroEnded != null))) {
				throw new IllegalArgumentException("Combinación de filtros mutuamente excluyentes: a) Por estatus y terminados ó b) Pendientes con estatus específico o terminados");
			} else {
				if (filtroEstatus != null) {
					filtros.remove(filtroEstatus);
				}
				if (filtroEnded != null) {
					filtros.remove(filtroEnded);
				}
			}
			
			/* Si se requieren instancias que se encuentren en un estatus específico entonces el comportamiento debe ser muy parecido a cuando se desean
			 * saber los pendientes para un determinado rol, solo que ahora se deben filtrar por aquellas que se encuentran en un estatus específico.
			 * */
			if (filtroEstatus != null) {
				esParaPendientes = true;
			}
			
			//Primero se busca las tareas 
			if (esParaPendientes) {
				TaskQuery tq = ts.createTaskQuery();
				tq.processDefinitionKey(processKey);
				if (filtroEstatus != null) {
					tq.taskName(filtroEstatus.getValor());
				} else {
					tq.taskCandidateGroup(((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSelectedAuthority());
				}
				tq.orderByProcessInstanceId().asc();
				tareas = tq.list();
	
				if (Context.getCommandContext() != null && tareas!=null && !tareas.isEmpty()) {
					List<PersistentObject> pos = Context.getCommandContext().getDbSqlSession().getUpdatedObjects();
					AtomicReference<List<Task>> tareasHolder = new AtomicReference<List<Task>>(tareas);
					if (pos != null && !pos.isEmpty()) {
						pos.forEach(po -> {
							Task task = tareasHolder.get()
											.stream()
											.filter(tarea -> po.getId().equals(tarea.getId()))
											.findFirst()
											.orElse(null);
							if (task != null && po instanceof HistoricTaskInstance) {
								//La tarea fue completada dentro de la ejecución actual y no ha sido registrada en la bd por eso aún la vemos en la consulta, procedemos a descartarla manualmente
								tareasHolder.get().remove(task);
							}
						});
					}
				}
			}
			
			CommandExecutor commandExecutor = ((ProcessEngineConfigurationImpl) processEngineConfig).getCommandExecutor();
			
			if ((esParaPendientes && !tareas.isEmpty()) || !esParaPendientes) {
				Set<String> pidsEncontradosSet = null;
				List<ProcessInstance> pids = null;
				
				if (filtroEnded == null) {
					GunixVariableProcessInstanceQuery piq = new GunixVariableProcessInstanceQuery(commandExecutor);
					piq.processDefinitionKey(processKey);
					restringirPorFechasInicioTermino = processFilters(filtros, piq);
					
					/*Si hay tareas pendientes la consulta se cerrara a unicamente las instancias de las tareas encontradas*/
					if (tareas != null && !tareas.isEmpty()) {
						piq.processInstanceIds(tareas.stream().map(task -> {
							return task.getProcessInstanceId();
						}).collect(Collectors.toCollection(() -> {
							return new HashSet<String>();
						})));
					}
					
					piq.orderByProcessInstanceId().asc();
					if (maxResults == BusinessProcessManager.NO_LIMIT) {
						pids = commandExecutor.execute(new Command<List<ProcessInstance>>() {
							@Override
							public List<ProcessInstance> execute(CommandContext commandContext) {
								piq.inicializaVariables();
								return commandContext.getDbSqlSession().selectList("selectProcessInstancesByQueryCriteriaGunix", piq);
							}
						});
					} else {
						pids = commandExecutor.execute(new Command<List<ProcessInstance>>() {
							@Override
							public List<ProcessInstance> execute(CommandContext commandContext) {
								piq.inicializaVariables();
								piq.setFirstResult(registroInicial != null ? registroInicial : 0);
								piq.setMaxResults(maxResults);
								return commandContext.getDbSqlSession().selectList("selectProcessInstancesByQueryCriteriaGunix", piq);
							}
						});
					}
					
					pidsEncontradosSet = pids.stream().map(pid -> {
						return pid.getProcessInstanceId();
					}).collect(Collectors.toCollection(() -> {
						return new HashSet<String>();
					}));
				} else {
					pidsEncontradosSet = new HashSet<String>();
				}
				
				if ((esParaPendientes && pidsEncontradosSet != null && !pidsEncontradosSet.isEmpty()) || !esParaPendientes) {
					/*Historia del proceso encontrado y que sigue en ejecución*/
					GunixVariableHistoricProcessInstanceQuery hpiq = new GunixVariableHistoricProcessInstanceQuery(commandExecutor);
					hpiq.processDefinitionKey(processKey);
					if ((esParaPendientes || maxResults != BusinessProcessManager.NO_LIMIT) && pidsEncontradosSet != null && !pidsEncontradosSet.isEmpty()) {
						hpiq.processInstanceIds(pidsEncontradosSet);
					}
					
					//Cuando la consulta no se cierra por las tareas de un perfil determinado entonces se incluyen las instancias historicas que cumplan con los filtros indicados 
					if((!esParaPendientes || restringirPorFechasInicioTermino) && filtros != null && !filtros.isEmpty()){
						restringirPorFechasInicioTermino = processFilters(filtros, hpiq);
					}
					
					if (filtroEnded != null) {
						if (filtroEnded.getValor()) {
							hpiq.finished();
						} else {
							hpiq.unfinished();
						}
					}
					
					hpiq.orderByProcessInstanceId().asc();

					List<HistoricProcessInstance> hpis = null;
					if (maxResults == BusinessProcessManager.NO_LIMIT) {
						hpis = commandExecutor.execute(new Command<List<HistoricProcessInstance>>() {
									@Override
									public List<HistoricProcessInstance> execute(CommandContext commandContext) {
										hpiq.inicializaVariables();
										return commandContext.getDbSqlSession().selectList("selectHistoricProcessInstancesByQueryCriteriaGunix", hpiq);
									}
								});
					} else {
						hpis = commandExecutor.execute(new Command<List<HistoricProcessInstance>>() {
							@Override
							public List<HistoricProcessInstance> execute(CommandContext commandContext) {
								hpiq.inicializaVariables();
								hpiq.setFirstResult(registroInicial != null ? registroInicial : 0);
								hpiq.setMaxResults(maxResults);
								return commandContext.getDbSqlSession().selectList("selectHistoricProcessInstancesByQueryCriteriaGunix", hpiq);
							}
						});
					}
					
					/* Si se restringió la busqueda por fechas de inicio o termino entonces deberemos de limitar la busqueda a esos registros, por lo que limpiamos los que ya habiamos encontrado en ejecución
					 * y de esa forma solo incluir los que resultaron de aplicar los filtros de fecha
					 */
					if (restringirPorFechasInicioTermino) {
						pidsEncontradosSet.clear();
					}

					/*
					 * Se consolidan los que siguen en ejecución como los historicos encontrados
					 */
					pidsEncontradosSet.addAll(hpis.stream().map(hpid -> {
						return hpid.getId();
					}).collect(Collectors.toCollection(() -> {
						return new HashSet<String>();
					})));
					
					if(pidsEncontradosSet!=null && !pidsEncontradosSet.isEmpty()){
						/*Tareas históricas*/
						HistoricTaskInstanceQuery htiq = hs.createHistoricTaskInstanceQuery();
						htiq.finished();
						htiq.processInstanceIdIn(new ArrayList<String>(pidsEncontradosSet));
						htiq.orderByProcessInstanceId().asc();
						htiq.orderByTaskCreateTime().desc();
						List<HistoricTaskInstance> hTasks = htiq.list();
						
						AtomicReference<List<Task>> tareasHolder = new AtomicReference<List<Task>>();
						AtomicReference<List<HistoricProcessInstance>> historicosHolder = new AtomicReference<List<HistoricProcessInstance>>();
						historicosHolder.set(hpis);
						
						if (!esParaPendientes && pids != null && !pids.isEmpty()) {
							TaskQuery tq = ts.createTaskQuery();
							tareasHolder.set(tq.processDefinitionKey(processKey)
												.active()
												.processInstanceIdIn(pids
																		.stream()
																		.map(pid->{
																			return pid.getId();
																			})
																		.collect(Collectors.toList()))
												.orderByProcessInstanceId().asc()
												.list());
						} else {
							tareasHolder.set(tareas);
						}
						
						
						//Primero procesamos los procesos que siguen en ejecución
						if (pids != null) {
							boolean finalRestringirPorFechasInicioTermino = restringirPorFechasInicioTermino;
							pidsEncontrados.addAll(
								pids
								.parallelStream()
								.flatMap(pid -> {
											Instancia inst = new Instancia();
											HistoricProcessInstance hpi = getHpi(pid.getId(), historicosHolder.get());
											Stream.Builder<Instancia> instanciaStream = Stream.builder();
											if (hpi != null) {
												inst.setId(pid.getId());
												inst.setProcessDefinitionId(pid.getProcessDefinitionId());
												inst.setInicio(hpi.getStartTime());
												inst.setProcessKey(processKey);
												inst.setTermino(hpi.getEndTime());
												inst.setUsuario(hpi.getStartUserId());
												inst.setVolatil(false);
												List<Comment> processComments = ts.getProcessInstanceComments(pid.getId(), PROCESS_CREATION_COMMENT);
												if (processComments != null && !processComments.isEmpty()) {
													inst.setComentario(processComments.get(0).getFullMessage());
												}
												if (tareasHolder.get() != null) {
													inst.setTareaActual(getTarea(inst, tareasHolder.get()));
												}
												
												inst.setTareas(getTareas(inst,hTasks));
												inst.setVariables(getVariables(inst, projectionVars));
												instanciaStream.add(inst);
											} else {
												if (!finalRestringirPorFechasInicioTermino) {
													throw new IllegalStateException("No fue posible encontrar la historia del process instance con id " + pid.getId());
												}
											}
											return instanciaStream.build();
										}
									)
								.collect(Collectors.toList()));
						}
						
						/*Recorremos los históricos que quedan*/
						if (hpis != null && !hpis.isEmpty()) {
							
							pidsEncontrados.addAll(hpis.parallelStream()
								.flatMap(hpid -> {
									Stream.Builder<Instancia> instanciaStream = Stream.builder();
									if (pidsEncontrados.parallelStream().filter(inst -> inst.getId().equals(hpid.getId())).findFirst().orElse(null) == null) {
										Instancia inst = new Instancia();
										inst.setId(hpid.getId());
										inst.setProcessDefinitionId(hpid.getProcessDefinitionId());
										inst.setProcessKey(hpid.getProcessDefinitionKey());
										inst.setInicio(hpid.getStartTime());
										inst.setProcessKey(processKey);
										inst.setTermino(hpid.getEndTime());
										inst.setUsuario(hpid.getStartUserId());
										inst.setVolatil(false);
										List<Comment> processComments = ts.getProcessInstanceComments(hpid.getId(), PROCESS_CREATION_COMMENT);
										
										if (processComments != null && !processComments.isEmpty()) {
											inst.setComentario(processComments.get(0).getFullMessage());
										}
	
										inst.setTareas(getTareas(inst, hTasks));
										inst.setVariables(getVariables(inst, projectionVars));
										instanciaStream.add(inst);
									}
									return instanciaStream.build();
							}).collect(Collectors.toList()));
							
						}
						
						Collections.sort(pidsEncontrados, Comparator.comparing(Instancia::getInicio).reversed());
					}
				}
			}
		return pidsEncontrados;
		}finally{
			GunixObjectVariableType.removeCurrentInstancia();
		}
	}
	
	private List<Variable<?>> getVariables(Instancia inst, String[] projectionVars) {
		Variable.Builder varBldr = new Variable.Builder();
		if (projectionVars != null) {
			for (String var : projectionVars) {
				varBldr.add(var, getVar(inst, var));
			}
		}
		return varBldr.build();
	}

	private List<Tarea> getTareas(Instancia inst, List<HistoricTaskInstance> hTasks) {
		Iterator<HistoricTaskInstance> hTasksIt = hTasks.iterator();
		List<Tarea> tareas = new ArrayList<Tarea>();
		boolean found = false;
		HistoricTaskInstance currTask = null;
		while (hTasksIt.hasNext()) {
			if ((currTask = hTasksIt.next()).getProcessInstanceId().equals(inst.getId())) {
				if (!found) {
					// Solo obtenemos la primer tarea histórica para simplificar la consulta, si se requieren todas se deberá utilizar el método getInstancia por cada instancia de la que se requiera
					// su detalle
					Tarea hT = new Tarea();
					hT.setInicio(currTask.getCreateTime());
					hT.setTermino(currTask.getEndTime());
					hT.setNombre(currTask.getName());
					hT.setUsuario(currTask.getOwner() == null ? currTask.getAssignee() : currTask.getOwner());
					hT.setInstancia(inst);
					List<Comment> taskComments = ts.getTaskComments(currTask.getId(), TASK_COMMENT);
					if (taskComments != null && !taskComments.isEmpty()) {
						hT.setComentario(taskComments.get(0).getFullMessage());
					}
					found = true;
					tareas.add(hT);
				}
			} else {
				if (found) {// Ya no hay más tareas para la instancia
					break;
				}
			}
		}
		return tareas;
	}

	private Tarea getTarea(Instancia inst, List<Task> tareas) {
		Iterator<Task> tareasIt = tareas.iterator();
		Task task = null;
		Tarea tarea = null;
		while (tareasIt.hasNext()) {
			if ((task = tareasIt.next()).getProcessInstanceId().equals(inst.getId())) {
				tarea = new Tarea();
				tarea.setExecutionId(task.getExecutionId());
				tarea.setId(task.getId());
				tarea.setInicio(task.getCreateTime());
				tarea.setNombre(task.getName());
				tarea.setInstancia(inst);
				break;
			}
		}
		return tarea;
	}

	private HistoricProcessInstance getHpi(String pid, List<HistoricProcessInstance> hpis) {
		Iterator<HistoricProcessInstance> hpisIt = hpis.iterator();
		HistoricProcessInstance hpi = null;
		while (hpisIt.hasNext()) {
			if ((hpi = hpisIt.next()).getId().equals(pid)) {
				break;
			} else {
				hpi = null;
			}
		}
		return hpi;
	}

	private boolean processFilters(List<Filtro<?>> filtros, GunixVariableHistoricProcessInstanceQuery hpiq) {
		boolean restringirPorFechasInicioTermino = false;
		if (filtros != null && !filtros.isEmpty()) {
			// Primero verificamos las fechas de inicio
			List<Date> inicioTerminoBetween = processRegistroTerminoFilters(filtros, true);
			if (!inicioTerminoBetween.isEmpty()) {
				restringirPorFechasInicioTermino = true;
				if (inicioTerminoBetween.get(0) != null) {
					hpiq.startedAfter(inicioTerminoBetween.get(0));
				}
				if (inicioTerminoBetween.get(1) != null) {
					hpiq.startedBefore(inicioTerminoBetween.get(1));
				}
			}

			// Ahora verificamos las fechas de termino
			inicioTerminoBetween = processRegistroTerminoFilters(filtros, false);
			if (!inicioTerminoBetween.isEmpty()) {
				restringirPorFechasInicioTermino = true;
				if (inicioTerminoBetween.get(0) != null) {
					hpiq.finishedAfter(inicioTerminoBetween.get(0));
				}
				if (inicioTerminoBetween.get(1) != null) {
					hpiq.finishedBefore(inicioTerminoBetween.get(1));
				}
			}
			
			
			filtros
				.stream()
				.filter(filtro->(filtro.getScope()==Variable.Scope.PROCESO))
				.forEach(filtro->{
					Map<String, Object> fvm = new TreeMap<String, Object>();
					fvm.putAll(GunixVariableSerializer.serialize(filtro.getNombre(), filtro.getValor(), false));
					fvm.forEach((varName, varValue) -> {
						switch (filtro.getlOp()) {
						case IGUAL:
							if (varName == Filtro.FILTRO_GLOBAL) {
								hpiq.variableValueEquals(varValue);
							} else {
								if(varName.indexOf("%")==-1){
									hpiq.variableValueEquals(varName, varValue);
								}else{
									hpiq.variableNameLikeValueEquals(varName, varValue);
								}
							}
							break;
						case MAYOR_QUE:
							hpiq.variableValueGreaterThan(varName, varValue);
							break;
						case MENOR_QUE:
							hpiq.variableValueLessThanOrEqual(varName, varValue);
							break;
						case DIFERENTE:
							hpiq.variableValueNotEquals(varName, varValue);
							break;
						case LIKE:
							if(varName.indexOf("%")==-1){
								hpiq.variableValueLike(varName, (String) varValue);
							}else{
								hpiq.variableNameLikeValueLike(varName, (String) varValue);
							}
							break;
						}
					});
				});

		}
		return restringirPorFechasInicioTermino;
	}
	
	private List<Date> processRegistroTerminoFilters(List<Filtro<?>> filtros, boolean isInicio){
		List<Date> registradoFinalizadoBetween = new ArrayList<Date>();
		List<Filtro<?>> registroFinBetweenFilters = filtros.stream().filter(f -> f.getNombre() == (isInicio ? Filtro.FILTRO_INICIO_BETWEEN : Filtro.FILTRO_FIN_BETWEEN)).collect(Collectors.toList());
		if (registroFinBetweenFilters != null && !registroFinBetweenFilters.isEmpty()) {
			registroFinBetweenFilters.forEach(f -> {
				registradoFinalizadoBetween.add((Date) f.getValor());
				filtros.remove(f);
			});
		}
		return registradoFinalizadoBetween;
	}
	
	private boolean processFilters(List<Filtro<?>> filtros, GunixVariableProcessInstanceQuery piq) {
		AtomicBoolean restringirPorFechasInicioTermino = new AtomicBoolean();
		if (filtros != null && !filtros.isEmpty()) {
			filtros
				.stream()
				.filter(filtro->(filtro.getScope()==Variable.Scope.PROCESO))
				.forEach(filtro->{
					if(filtro.getNombre() != Filtro.FILTRO_FIN_BETWEEN && filtro.getNombre() != Filtro.FILTRO_INICIO_BETWEEN){
						Map<String, Object> fvm = new TreeMap<String, Object>();
						fvm.putAll(GunixVariableSerializer.serialize(filtro.getNombre(), filtro.getValor(), false));
						fvm.forEach((varName, varValue) -> {
							switch (filtro.getlOp()) {
							case IGUAL:
								if (varName == Filtro.FILTRO_GLOBAL) {
									piq.variableValueEquals(varValue);
								} else {
									if(varName.indexOf("%")==-1){
										piq.variableValueEquals(varName, varValue);
									}else{
										piq.variableNameLikeValueEquals(varName, varValue);
									}
								}
								break;
							case MAYOR_QUE:
								piq.variableValueGreaterThan(varName, varValue);
								break;
							case MENOR_QUE:
								piq.variableValueLessThanOrEqual(varName, varValue);
								break;
							case DIFERENTE:
								piq.variableValueNotEquals(varName, varValue);
								break;
							case LIKE:
								if(varName.indexOf("%")==-1){
									piq.variableValueLike(varName, (String) varValue);
								}else{
									piq.variableNameLikeValueLike(varName, (String) varValue);
								}
								break;
							}
						});
					}else{
						restringirPorFechasInicioTermino.set(true);
					}
				});

		}
		return restringirPorFechasInicioTermino.get();
	}

	@Override
	public void setVar(Instancia instancia, String varName, Serializable varValue) {
		try {
			GunixObjectVariableType.setCurrentInstancia(instancia);
			rs.setVariable(instancia.getId(), varName, varValue);
		} finally {
			GunixObjectVariableType.removeCurrentInstancia();
		}
	}

	@Override
	public List<String> getEstadosProceso(String processKey) {
		return repos.getBpmnModel(repos.createProcessDefinitionQuery()
											.processDefinitionKey(processKey)
											.latestVersion()
											.singleResult()
											.getId())
					.getProcessById(processKey)
					.findFlowElementsOfType(UserTask.class)
					.stream()
						.map(userTask -> {
								return userTask.getName();
							})
						.collect(Collectors.toList());
	}
}
