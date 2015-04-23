package mx.com.gunix.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hunteron.api.HessianClientScannerConfigurer;
import com.hunteron.core.Hessian;

@Configuration
public class FrameworkClientServiceConfig {	
	@Bean
	public HessianClientScannerConfigurer hessianClientScannerConfigurer(){
		HessianClientScannerConfigurer hcsc = new HessianClientScannerConfigurer();
		hcsc.setAnnotationClass(Hessian.class);
		hcsc.setBasePackage("mx.com.gunix.**.service");
		return hcsc;
	}
}