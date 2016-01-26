package mx.com.gunix.framework.activiti;

import java.util.ArrayList;
import java.util.List;

import mx.com.gunix.framework.service.ActivitiServiceImp;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.asyncexecutor.ExecuteAsyncRunnable;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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
			String usuario = (String) rs.getVariable(job.getProcessInstanceId(), ActivitiServiceImp.CURRENT_AUTHENTICATION_USUARIO_VAR);
			String roles = (String) rs.getVariable(job.getProcessInstanceId(), ActivitiServiceImp.CURRENT_AUTHENTICATION_ROLES_VAR);
			List<GrantedAuthority> gas = new ArrayList<GrantedAuthority>();

			if (roles != null && !roles.equals("")) {
				String[] rolesArr = roles.split(",");
				for (String rol : rolesArr) {
					gas.add(new SimpleGrantedAuthority(rol));
				}
			}

			UsernamePasswordAuthenticationToken unTk = new UsernamePasswordAuthenticationToken(usuario, null, gas);

			ctx.setAuthentication(unTk);
			SecurityContextHolder.setContext(ctx);
			super.run();
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

}
