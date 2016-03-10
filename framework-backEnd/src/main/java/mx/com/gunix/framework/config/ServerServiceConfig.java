package mx.com.gunix.framework.config;

import mx.com.gunix.framework.service.ActivitiService;
import mx.com.gunix.framework.service.ActivitiServiceImp;
import mx.com.gunix.framework.service.UsuarioService;
import mx.com.gunix.framework.service.UsuarioServiceImp;
import mx.com.gunix.framework.service.hessian.spring.HessianServerScannerConfigurer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.hunteron.core.Hessian;

@Configuration
@ComponentScan({ "mx.com.gunix.service", "mx.com.gunix.framework.service" ,"org.openl.rules.activiti.spring"})
@EnableWebMvc
public class ServerServiceConfig extends WebMvcConfigurerAdapter {
	@Bean
	public UsuarioService usuarioService() {
		return new UsuarioServiceImp();
	}

	@Bean
	@Primary
	public ActivitiService activitiService() {
		return new ActivitiServiceImp();
	}

	@Bean
	@DependsOn("validator")
	public MethodValidationPostProcessor methodValidationPostProcessor(javax.validation.Validator validator) {
		MethodValidationPostProcessor mvpp = new MethodValidationPostProcessor();
		mvpp.setValidator(validator);
		return mvpp;
	}

	@Bean
	public HessianServerScannerConfigurer hessianServerScannerConfigurer() {
		HessianServerScannerConfigurer hssc = new HessianServerScannerConfigurer();
		hssc.setAnnotationClass(Hessian.class);
		hssc.setBasePackage("mx.com.gunix.**.service");
		return hssc;
	}

	@Override
	public Validator getValidator() { 
		return (Validator) validator();
	}

	@Bean
	public javax.validation.Validator validator() {
		return (javax.validation.Validator) new LocalValidatorFactoryBean();
	}
}
