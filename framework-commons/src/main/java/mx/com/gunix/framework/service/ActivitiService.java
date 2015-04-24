package mx.com.gunix.framework.service;

import java.util.Map;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.AuthenticatedVoter;

import com.hunteron.core.Hessian;

@Hessian("/activitiService")
@Secured(AuthenticatedVoter.IS_AUTHENTICATED_FULLY)
public interface ActivitiService {
	public Map<String,Object> completeTask(String processInstaceId,Map<String,Object> variables, String comentario);
	public String iniciaProceso(String processKey, Map<String,Object> variables, String comentario);
}
