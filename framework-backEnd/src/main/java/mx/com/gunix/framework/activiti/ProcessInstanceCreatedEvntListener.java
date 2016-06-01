package mx.com.gunix.framework.activiti;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.runtime.ProcessInstance;

import mx.com.gunix.framework.activiti.persistence.entity.GunixObjectVariableType;
import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.service.ActivitiService;

public class ProcessInstanceCreatedEvntListener implements ActivitiEventListener {

	@Override
	public void onEvent(ActivitiEvent event) {
		if (event instanceof ActivitiEntityEvent) {
			ActivitiEntityEvent entityEvnt = (ActivitiEntityEvent) event;
			if (entityEvnt.getEntity() instanceof ProcessInstance) {
				ProcessInstance pi = (ProcessInstance) entityEvnt.getEntity();
				Instancia pIns = new Instancia();
				pIns.setId(pi.getId());
				pIns.setVolatil(ActivitiService.VOLATIL.equals(Context.getProcessEngineConfiguration().getRepositoryService().getProcessDefinition(pi.getProcessDefinitionId()).getCategory()));
				GunixObjectVariableType.setCurrentInstancia(pIns);
			}
		}
	}

	@Override
	public boolean isFailOnException() {
		// TODO Auto-generated method stub
		return false;
	}
}
