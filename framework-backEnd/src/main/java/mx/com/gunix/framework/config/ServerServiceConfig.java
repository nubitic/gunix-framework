package mx.com.gunix.framework.config;

import mx.com.gunix.framework.service.ActivitiService;
import mx.com.gunix.framework.service.ActivitiServiceImp;
import mx.com.gunix.framework.service.UsuarioService;
import mx.com.gunix.framework.service.UsuarioServiceImp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.hunteron.api.HessianServerScannerConfigurer;
import com.hunteron.core.Hessian;

@Configuration
@ComponentScan({ "mx.com.gunix.service", "mx.com.gunix.framework.service" })
@EnableWebMvc
public class ServerServiceConfig extends WebMvcConfigurerAdapter {
	@Bean
	public UsuarioService usuarioService() {
		return new UsuarioServiceImp();
	}
	
	@Bean
	public ActivitiService activitiService() {
		return new ActivitiServiceImp();
	}
	
	@Bean
	public HessianServerScannerConfigurer hessianServerScannerConfigurer(){
		HessianServerScannerConfigurer hssc = new HessianServerScannerConfigurer();
		hssc.setAnnotationClass(Hessian.class);
		hssc.setBasePackage("mx.com.gunix.**.service");
		return hssc;
	}
}
