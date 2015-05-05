package mx.com.gunix.framework.activiti;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mx.com.gunix.framework.activiti.persistence.entity.VariableInstanceMapper;
import mx.com.gunix.framework.processes.domain.Tarea;
import mx.com.gunix.framework.processes.domain.Variable;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.variable.SerializableType;
import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

public class GunixObjectVariableType implements VariableType {
	public static final String GUNIX_OBJECT = SerializableType.TYPE_NAME;

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
		return value != null && !BeanUtils.isSimpleProperty(value.getClass()) && (value instanceof Serializable);
	}

	@Override
	public void setValue(Object value, ValueFields valueFields) {
		VariableInstanceEntity vie = (VariableInstanceEntity) valueFields;

		Map<String, Object> variablesMap = new TreeMap<String, Object>();
		variablesMap.putAll(Utils.toMap(vie.getName(), value));
		
		Tarea tarea = currentTarea.get();
		
		Variable<?> variable = tarea.getVariables().stream()
											.filter( var -> var.getNombre().equals(vie.getName()) && var.getValor().equals(value))
											.findFirst()
											.get();
		
		switch(variable.getScope()){
			case PROCESO:
				rs.setVariables(tarea.getInstancia().getId(), variablesMap);	
			break;
			case TAREA:
				rs.setVariablesLocal(tarea.getExecutionId(), variablesMap);
			break;
		}
	}

	@Override
	public Object getValue(ValueFields valueFields) {
		VariableInstanceEntity vie = (VariableInstanceEntity) valueFields;
		Map<String, Object> mappedObject = new TreeMap<String, Object>();

		List<Map<String, String>> vars = null;

		if (vie.getTaskId() == null) {
			vars = vim.findGunixObjectByNameAndExecutionId(vie.getExecutionId(), vie.getName() + ".%");
		} else {
			vars = vim.findGunixObjectByNameAndTaskId(vie.getTaskId(), vie.getName() + ".%");
		}

		vars.stream().forEach(map -> {
			mappedObject.put(map.get("key"), map.get("value"));
		});

		return Utils.fromMap(vie.getName(), mappedObject, getClass().getClassLoader());
	}

}
