package mx.com.pipp.framework.config;

import mx.com.pipp.service.ActivitiService;
import mx.com.pipp.service.UsuarioService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;

@Configuration
public class FrameworkClientServiceConfig {
	
	@Bean
	public HessianProxyFactoryBean usuarioService(){
		HessianProxyFactoryBean hessianProxy = new HessianProxyFactoryBean();
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
