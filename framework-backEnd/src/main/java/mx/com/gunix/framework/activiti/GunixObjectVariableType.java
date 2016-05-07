package mx.com.gunix.framework.activiti;

import static mx.com.gunix.framework.service.ActivitiService.VOLATIL;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mx.com.gunix.framework.activiti.persistence.entity.VariableInstanceMapper;
import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.processes.domain.Tarea;
import mx.com.gunix.framework.processes.domain.Variable;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.cmd.NeedsActiveExecutionCmd;
import org.activiti.engine.impl.cmd.StartProcessInstanceCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.variable.NullType;
import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

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

	@Autowired
	@Lazy
	VariableInstanceMapper vim;

	@Autowired
	@Lazy
	RuntimeService rs;

	public static void setCurrentTarea(Tarea tarea) {
		currentTarea.set(tarea);
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
		return !isVolatil() && ((value != null && !BeanUtils.isSimpleProperty(value.getClass()) && (value instanceof Serializable)) || super.isAbleToStore(value));
	}

	private boolean isVolatil() {
		boolean isVolatil = true;
		ProcessInstance pi = ProcessInstanceCreatedEvntListener.getLastCreated();
		CommandContext cmdCtx = Context.getCommandContext();
		if(cmdCtx != null){
			if(pi == null){
					if (cmdCtx.getCommand() instanceof NeedsActiveExecutionCmd) {
						try {
							ExecutionEntity execution = cmdCtx.getExecutionEntityManager().findExecutionById((String) executionIdField.get(cmdCtx.getCommand()));
							isVolatil = VOLATIL.equals(((ProcessDefinition) execution.getProcessDefinition()).getCategory());
						} catch (IllegalArgumentException | IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					}else{
						if (cmdCtx.getCommand() instanceof StartProcessInstanceCmd) {
							try {
								isVolatil = VOLATIL.equals(
										cmdCtx
									      .getProcessEngineConfiguration()
									      .getDeploymentManager()
									      .findDeployedLatestProcessDefinitionByKey((String) processDefinitionKeyField.get(cmdCtx.getCommand())).getCategory());
							} catch (IllegalArgumentException | IllegalAccessException e) {
								throw new RuntimeException(e);
							}
						}
					}	
			} else {
				isVolatil = VOLATIL.equals(
						cmdCtx
					      .getProcessEngineConfiguration()
					      .getDeploymentManager()
					      .findDeployedProcessDefinitionById(pi.getProcessDefinitionId()).getCategory());
			}
		}
		return isVolatil;
	}

	@Override
	public void setValue(Object value, ValueFields valueFields) {
		VariableInstanceEntity vie = (VariableInstanceEntity) valueFields;
		ProcessInstance pi = ProcessInstanceCreatedEvntListener.getLastCreated();
		boolean isExecutionContextActive = Context.isExecutionContextActive() && pi == null;
		String executionId = isExecutionContextActive ? Context.getExecutionContext().getProcessInstance().getId() : null;
		boolean isUpdateValue = vie.getRevision() > 0;

		Tarea tarea = null;
		Variable<?> variable = null;

		if (!isExecutionContextActive) {
			tarea = currentTarea.get();
			if (tarea != null && (pi == null || pi.getId().equals(tarea.getInstancia().getId()))) {
				variable = tarea.getVariables()
									.stream()
									.filter(var -> 
											var.getNombre().equals(vie.getName()) && (var.getValor() == value || (var.getValor() != null && var.getValor().equals(value)))
											)
									.findFirst()
									.get();
			} else {
				if (pi != null) {
					variable = new Variable<String>(); //El scope por default es PROCESO
					tarea = new Tarea();
					tarea.setInstancia(new Instancia());
					tarea.getInstancia().setId(pi.getId());
				}
			}
		}

		if (isUpdateValue) {
			if (!isExecutionContextActive) {
				switch (variable.getScope()) {
				case PROCESO:
					deleteValue(vie.getName(), pi != null ? pi.getId() : tarea.getInstancia().getId(), null);
					break;
				}
			} else {
				deleteValue(vie.getName(), executionId, null);
			}
		}

		if (value != null) {
			Map<String, Object> variablesMap = new TreeMap<String, Object>();
			variablesMap.putAll(Utils.toMap(vie.getName(), value));

			vie.setTextValue(value.getClass().getName());

			if (isExecutionContextActive) {
				rs.setVariables(executionId, variablesMap);
			} else {
				switch (variable.getScope()) {
				case PROCESO:
					rs.setVariables(pi != null ? pi.getId() : tarea.getInstancia().getId(), variablesMap);
					break;
				}
			}
		}
	}

	@Override
	public Object getValue(ValueFields valueFields) {
		VariableInstanceEntity vie = (VariableInstanceEntity) valueFields;
		TreeMap<String, Object> mappedObject = new TreeMap<String, Object>();

		List<Map<String, Object>> vars = null;

		if (vie.getTaskId() == null) {
			vars = vim.findGunixObjectByNameAndExecutionId(vie.getExecutionId(), vie.getName());
		} else {
			vars = vim.findGunixObjectByNameAndTaskId(vie.getTaskId(), vie.getName());
		}

		vars.stream().forEach(map -> {
			mappedObject.put((String) map.get("key"), getValue(map));
		});

		return mappedObject.isEmpty() ? super.getValue(valueFields) : Utils.fromMap(vie.getName(), mappedObject, getClass().getClassLoader());
	}

	private Object getValue(Map<String, Object> map) {
		Object ans = null;
		ans = map.get(VariableInstanceMapper.LONG_KEY);
		if (ans == null) {
			ans = map.get(VariableInstanceMapper.DOUBLE_KEY);
		}
		if (ans == null) {
			ans = map.get(VariableInstanceMapper.TEXT_KEY);
		}
		return ans;
	}

	public void deleteValue(String name, String executionId, String taskId) {
		List<Map<String, Object>> vars = null;

		if (taskId == null) {
			vars = vim.findGunixObjectByNameAndExecutionId(executionId, name);
		} else {
			vars = vim.findGunixObjectByNameAndTaskId(taskId, name);
		}
		vars.stream().forEach(map -> {
			vim.delete((String) map.get("id_"), (int) map.get("rev_"));
		});
	}
}
