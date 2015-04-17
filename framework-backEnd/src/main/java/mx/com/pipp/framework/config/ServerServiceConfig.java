package mx.com.pipp.framework.config;

import mx.com.pipp.framework.service.ActivitiServiceImp;
import mx.com.pipp.framework.service.UsuarioServiceImp;
import mx.com.pipp.service.ActivitiService;
import mx.com.pipp.service.UsuarioService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.caucho.HessianServiceExporter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@ComponentScan({"mx.com.pipp.service","mx.com.pipp.framework.service"})
@EnableWebMvc
public class ServerServiceConfig extends WebMvcConfigurerAdapter {
	@Bean
	public UsuarioService usuarioService() {
		return new UsuarioServiceImp();
	}
	
	@Bean(name = "/usuarioService")
	public HessianServiceExporter userServiceExport() {
		HessianServiceExporter he = new HessianServiceExporter();
		he.setService(usuarioService());
		he.setServiceInterface(UsuarioService.class);
		return he;
	}
	
	@Bean
	public ActivitiService activitiService() {
		return new ActivitiServiceImp();
	}
	
	@Bean(name = "/activitiService")
	public HessianServiceExporter activitiServiceExport() {
		HessianServiceExporter he = new HessianServiceExporter();
		he.setService(activitiService());
		he.setServiceInterface(ActivitiService.class);
		return he;
	}
}
