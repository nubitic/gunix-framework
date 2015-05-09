package mx.com.gunix.framework.service;

import java.util.Map;
import java.util.Optional;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;

public class GunixActivitServiceSupport {
	protected final void actualizaVariable(Object var) {
		ExecutionEntity ee = Context.getExecutionContext().getExecution();
		ee.setVariable(getVarNameToUpdate(ee, var), var);
	}

	private String getVarNameToUpdate(ExecutionEntity ee, Object var) {
		String varName=null;
		Map<String, VariableInstanceEntity> varMap = ee.getVariableInstances(); 
		if (varMap != null && !varMap.isEmpty()) {
			Optional<VariableInstanceEntity> ovie = varMap.values()
															.stream()
															.filter(ivie-> ivie.getCachedValue()==var)
															.findFirst();
			if(ovie.isPresent()){
				varName=ovie.get().getName();
			}
		}
		
		if(varName==null){
			if(ee.getParent()!=null){
				varName=getVarNameToUpdate(ee.getParent(),var);
			}else{
				throw new IllegalArgumentException("No se encontró la variable a actualizar en el contexto de la ejecución actual, ¿será que es nueva y mas bien necesitas 'agregarla' en vez de actualizarla?");
			}
		}
		return varName;
	}

	protected final void agregaVariable(String nombre, Object valor) {
		Context.getExecutionContext().getExecution().setVariable(nombre, valor);
	}
}
