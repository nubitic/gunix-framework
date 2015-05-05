package mx.com.gunix.framework.activiti.persistence.entity;

import java.util.List;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

public class VariableInstanceEntityManager extends org.activiti.engine.impl.persistence.entity.VariableInstanceEntityManager implements SessionFactory {

	@Autowired
	@Lazy
	VariableInstanceMapper vim;

	@Override
	public List<VariableInstanceEntity> findVariableInstancesByTaskId(String taskId) {
		return vim.findVariableInstancesByTaskId(taskId);
	}

	@Override
	public List<VariableInstanceEntity> findVariableInstancesByExecutionId(String executionId) {
		return vim.findVariableInstancesByExecutionId(executionId);
	}

	@Override
	public Class<?> getSessionType() {
		return org.activiti.engine.impl.persistence.entity.VariableInstanceEntityManager.class;
	}

	@Override
	public Session openSession() {
		return this;
	}

}
