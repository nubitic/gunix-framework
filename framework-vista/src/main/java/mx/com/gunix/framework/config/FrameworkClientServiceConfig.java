package mx.com.gunix.framework.config;

import mx.com.gunix.service.ActivitiService;
import mx.com.gunix.service.UsuarioService;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;

@Configuration
public class FrameworkClientServiceConfig {
	
	@Bean
	public HessianProxyFactoryBean usuarioService(){
		HessianProxyFactoryBean hessianProxy = new HessianProxyFactoryBean(){

			@Override
			public Object invoke(MethodInvocation invocation) throws Throwable {
				ReflectiveMethodInvocation rmi = (ReflectiveMethodInvocation) invocation;
				Object[] orgArgs = rmi.getArguments();
				Object[] newArgs = new Object[orgArgs.length+1]; 
				System.arraycopy(orgArgs, 0, newArgs, 0, orgArgs.length);
				
				rmi.setArguments(newArgs);
				
				Object result = super.invoke(invocation);
				rmi.setArguments(orgArgs);
				return result;
			}
			
		};
		hessianProxy.setServiceInterface(UsuarioService.class);
		hessianProxy.setServiceUrl("http://localhost:8080/backEnd/usuarioService");
		return hessianProxy;
	}
	
	@Bean
	public HessianProxyFactoryBean activitiService(){
		HessianProxyFactoryBean hessianProxy = new HessianProxyFactoryBean();
		hessianProxy.setServiceInterface(ActivitiService.class);
		hessianProxy.setServiceUrl("http://localhost:8080/backEnd/activitiService");
		return hessianProxy;
	}

}
