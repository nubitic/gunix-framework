package mx.com.gunix.framework.activiti;

import static mx.com.gunix.framework.service.ActivitiService.VOLATIL;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.cmd.NeedsActiveExecutionCmd;
import org.activiti.engine.impl.cmd.StartProcessInstanceCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.variable.NullType;
import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.engine.impl.variable.VariableTypes;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import mx.com.gunix.framework.activiti.persistence.entity.VariableInstanceMapper;
import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.processes.domain.Tarea;

public class GunixObjectVariableType extends NullType implements VariableType {
	public static final String GUNIX_OBJECT = "gunix-serializable";
	private static final Field executionIdField;
	private static final Field processDefinitionKeyField;
	static {
		try {
			executionIdField = NeedsActiveExecutionCmd.class.getDeclaredField("executionId");
			executionIdField.setAccessible(true);

			processDefinitionKeyField = StartProcessInstanceCmd.class.getDeclaredField("processDefinitionKey");
			processDefinitionKeyField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	private static final ThreadLocal<Tarea> currentTarea = new ThreadLocal<Tarea>();
	private static final ThreadLocal<Stack<Map<Serializable,String>>> currentVar = ThreadLocal.withInitial(() -> {
		return new Stack<Map<Serializable, String>>();
	});

	@Autowired
	@Lazy
	VariableInstanceMapper vim;

	@Autowired
	@Lazy
	RuntimeService rs;

	public static void setCurrentTarea(Tarea tarea) {
		currentTarea.set(tarea);
	}

	public static void setCurrentVar(String pid, Serializable valor) {
		Map<Serializable, String> pidVar = new HashMap<Serializable, String>();
		pidVar.put(valor, pid);
		currentVar.get().push(pidVar);
	}

	public static void removeCurrentVar() {
		currentVar.get().pop();
	}

	@Override
	public String getTypeName() {
		return GUNIX_OBJECT;
	}

	@Override
	public boolean isCachable() {
		return true;
	}

	@Override
	public boolean isAbleToStore(Object value) {
		return !isVolatil(value) && ((value != null && !BeanUtils.isSimpleProperty(value.getClass()) && (value instanceof Serializable)) || super.isAbleToStore(value));
	}

	private boolean isVolatil(Object value) {
		boolean isVolatil = true;
		if (currentVar.get().isEmpty() || !currentVar.get().peek().keySet().stream().filter(valor -> valor == value).findFirst().isPresent()) {
			ProcessInstance pi = ProcessInstanceCreatedEvntListener.getLastCreated();
			CommandContext cmdCtx = Context.getCommandContext();
			if (cmdCtx != null) {
				if (pi == null) {
					if (cmdCtx.getCommand() instanceof NeedsActiveExecutionCmd) {
						try {
							ExecutionEntity execution = cmdCtx.getExecutionEntityManager().findExecutionById((String) executionIdField.get(cmdCtx.getCommand()));
							isVolatil = VOLATIL.equals(((ProcessDefinition) execution.getProcessDefinition()).getCategory());
						} catch (IllegalArgumentException | IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					} else {
						if (cmdCtx.getCommand() instanceof StartProcessInstanceCmd) {
							try {
								isVolatil = VOLATIL.equals(cmdCtx.getProcessEngineConfiguration().getDeploymentManager().findDeployedLatestProcessDefinitionByKey((String) processDefinitionKeyField.get(cmdCtx.getCommand())).getCategory());
							} catch (IllegalArgumentException | IllegalAccessException e) {
								throw new RuntimeException(e);
							}
						}
					}
				} else {
					isVolatil = VOLATIL.equals(cmdCtx.getProcessEngineConfiguration().getDeploymentManager().findDeployedProcessDefinitionById(pi.getProcessDefinitionId()).getCategory());
				}
			}
		} else {
			isVolatil = false;
		}
		return isVolatil;
	}

	@Override
	public void setValue(Object value, ValueFields valueFields) {
		VariableInstanceEntity vie = (VariableInstanceEntity) valueFields;
		ProcessInstance pi = ProcessInstanceCreatedEvntListener.getLastCreated();
		boolean isExecutionContextActive = Context.isExecutionContextActive() && pi == null && (currentVar.get().isEmpty() || !currentVar.get().peek().containsKey(value));
		String executionId = isExecutionContextActive ? Context.getExecutionContext().getProcessInstance().getId() : pi == null ? currentVar.get().peek().get(value) : null ;
		boolean isUpdateValue = vie.getRevision() > 0;

		Tarea tarea = null;

		if (!isExecutionContextActive) {
			tarea = currentTarea.get();
			if (tarea == null || executionId != null) {
				tarea = new Tarea();
				tarea.setInstancia(new Instancia());
				tarea.getInstancia().setId(pi != null ? pi.getId() : executionId);
			}
		}

		if (isUpdateValue) {
			if (!isExecutionContextActive) {
				deleteValue(vie.getName(), pi != null ? pi.getId() : tarea.getInstancia().getId(), null);
			} else {
				deleteValue(vie.getName(), executionId, null);
			}
		}

		if (value != null) {
			Map<String, Object> variablesMap = new TreeMap<String, Object>();
			variablesMap.putAll(GunixVariableSerializer.serialize(vie.getName(), value));

			vie.setTextValue(value.getClass().getName());

			if (isExecutionContextActive) {
				rs.setVariables(executionId, variablesMap);
			} else {
				rs.setVariables(pi != null ? pi.getId() : tarea.getInstancia().getId(), variablesMap);
			}
		}
	}

	@Override
	public Object getValue(ValueFields vie) {
		List<Map<String, Object>> vars = null;

		if (vie instanceof HistoricVariableInstanceEntity) {
			if (vie.getTaskId() == null) {
				vars = vim.findHistoricGunixObjectByNameAndExecutionId(vie.getExecutionId(), vie.getName());
			}
		} else {
			if (vie.getTaskId() == null) {
				vars = vim.findGunixObjectByNameAndExecutionId(vie.getExecutionId(), vie.getName());
			} else {
				vars = vim.findGunixObjectByNameAndTaskId(vie.getTaskId(), vie.getName());
			}
		}

		return vars == null || vars.isEmpty() ? super.getValue(vie) : GunixVariableSerializer.deserialize(vie.getName(), vars, getClass().getClassLoader());
	}

	public void deleteValue(String name, String executionId, String taskId) {
		List<Map<String, Object>> vars = null;

		if (taskId == null) {
			vars = vim.findGunixObjectByNameAndExecutionId(executionId, name);
		} else {
			vars = vim.findGunixObjectByNameAndTaskId(taskId, name);
		}
		
		/*ExecutionEntity ee = null;
		Boolean isExecutionActive = Context.isExecutionContextActive() && (ee = Context.getExecutionContext().getExecution()) instanceof VariableScope;
		VariableScope vs = isExecutionActive ? (VariableScope) ee : null;
		VariableTypes vTypes = isExecutionActive?Context.getProcessEngineConfiguration().getVariableTypes():null;*/
		vars.stream().forEach(map -> {
			/*if (isExecutionActive) {
				Object value = GunixVariableSerializer.getValue(map);
				VariableInstanceEntity variableInstance = VariableInstanceEntity.create((String) map.get("key"), vTypes.findVariableType(value), value);
				variableInstance.setId((String) map.get("id_"));
				variableInstance.setExecutionId(((ExecutionEntity)vs).getId());
				variableInstance.setRevision((int) map.get("rev_"));
				variableInstance.delete();
				variableInstance.setValue(null);

			    // Record historic variable deletion
			    Context.getCommandContext().getHistoryManager()
			    	.recordVariableRemoved(variableInstance);

			    // Record historic detail
			    Context.getCommandContext().getHistoryManager()
			      .recordHistoricDetailVariableCreate(variableInstance, (ExecutionEntity) vs,  true);
			} else {*/
				vim.delete((String) map.get("id_"), (int) map.get("rev_"));
			//}
		});
	}
}
