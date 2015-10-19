package mx.com.gunix.framework.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return Boolean.valueOf(System.getenv("STANDALONE_APP")) ? 
					new Class<?>[] { MethodSecurityConfig.class, ServerServiceConfig.class, AdminAppServicesConfig.class, ActivitiConfig.class, DozerConfig.class, AspectJConfig.class } : 
   			   new Class<?>[] { MethodSecurityConfig.class, ServerServiceConfig.class, ActivitiConfig.class, DozerConfig.class, AspectJConfig.class };
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[0];
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}

}
