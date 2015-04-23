package mx.com.gunix.framework.service;

import java.util.Map;

import com.hunteron.core.Hessian;

@Hessian("/activitiService")
public interface ActivitiService {
	public Map<String,Object> completeTask(String processInstaceId,Map<String,Object> variables, String comentario, String usuario);
	public String iniciaProceso(String processKey, String usuario,Map<String,Object> variables, String comentario);
}
