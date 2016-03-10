package mx.com.gunix.framework.activiti;

import java.util.ArrayDeque;
import java.util.Deque;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.runtime.ProcessInstance;

public class ProcessInstanceCreatedEvntListener implements ActivitiEventListener {
	protected static ThreadLocal<Deque<ProcessInstance>> lastProcessInstanceCreatedThreadLocal = ThreadLocal.withInitial(() -> {
		return new ArrayDeque<ProcessInstance>();
	});

	@Override
	public void onEvent(ActivitiEvent event) {
		if (event instanceof ActivitiEntityEvent) {
			ActivitiEntityEvent entityEvnt = (ActivitiEntityEvent) event;
			if (entityEvnt.getEntity() instanceof ProcessInstance) {
				lastProcessInstanceCreatedThreadLocal.get().offerFirst((ProcessInstance) entityEvnt.getEntity());
			}
		}
	}

	@Override
	public boolean isFailOnException() {
		// TODO Auto-generated method stub
		return false;
	}

	public static ProcessInstance getLastCreated() {
		return lastProcessInstanceCreatedThreadLocal.get().peek();
	}
	
	public static void consumeLastCreated(){
		lastProcessInstanceCreatedThreadLocal.get().pollFirst();
	}
}
