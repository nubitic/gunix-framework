package mx.com.gunix.framework.activiti.persistence.entity;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.cmd.NeedsActiveExecutionCmd;
import org.activiti.engine.impl.cmd.StartProcessInstanceCmd;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.variable.NullType;
import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import mx.com.gunix.framework.activiti.GunixVariableSerializer;
import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.processes.domain.Tarea;

public class GunixObjectVariableType extends NullType implements VariableType {
	public static final String GUNIX_OBJECT = "gunix-serializable";
	private static final Field executionIdField;
	private static final Field processDefinitionKeyField;
	private static final Field deleteOperationsField;
	static {
		try {
			executionIdField = NeedsActiveExecutionCmd.class.getDeclaredField("executionId");
			executionIdField.setAccessible(true);

			processDefinitionKeyField = StartProcessInstanceCmd.class.getDeclaredField("processDefinitionKey");
			processDefinitionKeyField.setAccessible(true);
			
			deleteOperationsField = DbSqlSession.class.getDeclaredField("deleteOperations");
			deleteOperationsField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	private static final ThreadLocal<Tarea> currentTarea = new ThreadLocal<Tarea>();
	private static final ThreadLocal<Stack<Instancia>> currentInstancia = ThreadLocal.withInitial(() -> {
		return new Stack<Instancia>();
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

	public static void setCurrentInstancia(Instancia instancia) {
		currentInstancia.get().push(instancia);
	}

	public static void removeCurrentInstancia() {
		currentInstancia.get().pop();
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
		return !currentInstancia.get().peek().isVolatil() && ((value != null && !BeanUtils.isSimpleProperty(value.getClass()) && (value instanceof Serializable)) || super.isAbleToStore(value));
	}

	@Override
	public void setValue(Object value, ValueFields valueFields) {
		if (valueFields instanceof HasRevision) {
			VariableInstanceEntity vie = (VariableInstanceEntity) valueFields;
			String executionId = currentInstancia.get().peek().getId();
			
			if (value != null) {
				if (!value.getClass().isArray() && !(value instanceof Collection)) {
					vim.delete(executionId, vie.getName(), ((HasRevision) vie).getRevision());
				}
				Map<String, Object> variablesMap = new TreeMap<String, Object>();
				variablesMap.putAll(GunixVariableSerializer.serialize(vie.getName(), value, false));

				vie.setTextValue(value.getClass().getName());
				rs.setVariables(executionId, variablesMap);
			}
		}else{
			throw new UnsupportedOperationException("Solo se permiten instancias de HasRevision");
		}
	}

	@Override
	public Object getValue(ValueFields vie) {
		List<Map<String, Object>> vars = null;

		if (vie instanceof HistoricVariableInstanceEntity) {
			if (vie.getTaskId() == null) {
				vars = vim.findHistoricGunixObjectByNameAndExecutionIdAndRevision(vie.getExecutionId(), vie.getName());
			}
		} else {
			if (vie instanceof HasRevision) {
				vars = vim.findGunixObjectByNameAndExecutionIdAndRevision(vie.getExecutionId(), vie.getName(), ((HasRevision) vie).getRevision());
			} else {
				throw new UnsupportedOperationException("Solo se permiten instancias de HasRevision");
			}
		}

		return vars == null || vars.isEmpty() ? super.getValue(vie) : GunixVariableSerializer.deserialize(vie.getName(), vars, getClass().getClassLoader());
	}
}
