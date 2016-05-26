package mx.com.gunix.framework.activiti;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.asyncexecutor.ExecuteAsyncRunnable;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import mx.com.gunix.framework.security.UserDetails;
import mx.com.gunix.framework.service.ActivitiServiceImp;

public class ExecuteAsyncSecuredRunnable extends ExecuteAsyncRunnable {
	private RuntimeService rs;

	public ExecuteAsyncSecuredRunnable(JobEntity job, CommandExecutor commandExecutor, RuntimeService rs) {
		super(job, commandExecutor);
		this.rs = rs;
	}

	@Override
	public void run() {
		try {
			SecurityContext ctx = SecurityContextHolder.createEmptyContext();
			UserDetails usuario = (UserDetails) rs.getVariable(job.getProcessInstanceId(), ActivitiServiceImp.CURRENT_AUTHENTICATION_USUARIO_VAR);			

			UsernamePasswordAuthenticationToken unTk = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

			ctx.setAuthentication(unTk);
			SecurityContextHolder.setContext(ctx);
			super.run();
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

}
