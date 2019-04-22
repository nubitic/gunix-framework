package mx.com.gunix.framework.activiti;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.asyncexecutor.multitenant.TenantAwareExecuteAsyncRunnable;
import org.activiti.engine.impl.cfg.multitenant.TenantInfoHolder;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import mx.com.gunix.framework.activiti.persistence.entity.GunixObjectVariableType;
import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.security.UserDetails;
import mx.com.gunix.framework.service.ActivitiService;
import mx.com.gunix.framework.service.ActivitiServiceImp;

public class ExecuteAsyncSecuredRunnable extends TenantAwareExecuteAsyncRunnable {
	private static Logger log = LoggerFactory.getLogger(ExecuteAsyncSecuredRunnable.class);
	private RuntimeService rs;
	private RepositoryService repos;

	public ExecuteAsyncSecuredRunnable(JobEntity job, CommandExecutor commandExecutor, TenantInfoHolder tenantInfoHolder, String tenantId, RuntimeService rs, RepositoryService repos) {
		super(job, commandExecutor, tenantInfoHolder, tenantId);
		this.rs = rs;
		this.repos = repos;
	}

	@Override
	public void run() {
		try {
			SecurityContext ctx = SecurityContextHolder.createEmptyContext();
			UserDetails usuario = (UserDetails) rs.getVariable(job.getProcessInstanceId(), ActivitiServiceImp.CURRENT_AUTHENTICATION_USUARIO_VAR);			

			UsernamePasswordAuthenticationToken unTk = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

			ctx.setAuthentication(unTk);
			SecurityContextHolder.setContext(ctx);
			
			Instancia instancia = new Instancia();
			instancia.setId(job.getProcessInstanceId());
			instancia.setProcessDefinitionId(job.getProcessDefinitionId());
			instancia.setVolatil(ActivitiService.VOLATIL.equals(repos.getProcessDefinition(job.getProcessDefinitionId()).getCategory()));
			GunixObjectVariableType.setCurrentInstancia(instancia);
			super.run();
		} catch(Exception e) {
			log.error("Failed to execute job " + job.getId(), e);
			throw new RuntimeException(e);
		}
		finally {
			SecurityContextHolder.clearContext();
			GunixObjectVariableType.removeCurrentInstancia();
		}
	}

}
