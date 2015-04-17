package mx.com.pipp.framework.service;

import java.util.Map;
import java.util.Optional;

import mx.com.pipp.service.ActivitiService;

import org.activiti.engine.FormService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ActivitiServiceImp implements ActivitiService {
	@Autowired
	TaskService ts;
	
	@Autowired
	RuntimeService rs;
	
	@Autowired
	IdentityService is;
	
	@Autowired
	FormService fs;
	
	@Override
	public Map<String, Object> completeTask(String processInstaceId, Map<String,Object> variables, String comentario, String usuario) {
		String taskId = ts.createTaskQuery().active().processInstanceId(processInstaceId).singleResult().getId();
		ts.claim(taskId, usuario);
		Optional.ofNullable(comentario).ifPresent(comment->{
			ts.addComment(taskId, processInstaceId, comment);
		});
		ts.complete(taskId, variables);
		return variables;
	}

	@Override
	public String iniciaProceso(String processKey, String usuario, Map<String, Object> variables, String comentario) {
		is.setAuthenticatedUserId(usuario);
		ProcessInstance pi = rs.startProcessInstanceByKey(processKey, variables);
		Task task = ts.createTaskQuery().active().processInstanceId(pi.getProcessInstanceId()).singleResult();
		Optional.ofNullable(comentario).ifPresent(comment->{
			ts.addComment(null, pi.getProcessInstanceId(), comment);
		});
		TaskFormData fd =fs.getTaskFormData(task.getId());
		is.setAuthenticatedUserId(null);
		return fd.getFormKey();
	}
}
