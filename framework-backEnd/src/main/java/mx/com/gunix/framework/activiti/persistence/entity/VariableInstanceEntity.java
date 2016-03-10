package mx.com.gunix.framework.activiti.persistence.entity;

import mx.com.gunix.framework.activiti.GunixObjectVariableType;

public class VariableInstanceEntity extends org.activiti.engine.impl.persistence.entity.VariableInstanceEntity {
	private static final long serialVersionUID = 1L;

	@Override
	public void delete() {
		super.delete();
		if(type instanceof GunixObjectVariableType){
			((GunixObjectVariableType)type).deleteValue(name, executionId, taskId);
		}
	}

}
