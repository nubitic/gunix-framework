package mx.com.gunix.framework.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return doConfigureClasses();
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[0];
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}

	private Class<?>[] doConfigureClasses() {
		List<Class<?>> configClasses = new ArrayList<Class<?>>();

		if (Boolean.valueOf(System.getenv("STANDALONE_APP")) && Boolean.valueOf(System.getenv("MONGO_DB_NAME"))) {
			configClasses.addAll(Arrays.asList(new Class<?>[] { MethodSecurityConfig.class, ServerServiceConfig.class, ActivitiConfig.class, AspectJConfig.class, AdminAppServicesConfig.class, MongoDBConfig.class }));
		} else {
			if (Boolean.valueOf(System.getenv("STANDALONE_APP"))) {
				configClasses.addAll(Arrays.asList(new Class<?>[] { MethodSecurityConfig.class, ServerServiceConfig.class, ActivitiConfig.class, AspectJConfig.class, AdminAppServicesConfig.class }));
			} else {
				if (Boolean.valueOf(System.getenv("MONGO_DB_NAME"))) {
					configClasses.addAll(Arrays.asList(new Class<?>[] { MethodSecurityConfig.class, ServerServiceConfig.class, ActivitiConfig.class, AspectJConfig.class, MongoDBConfig.class }));
				} else {
					configClasses.addAll(Arrays.asList(new Class<?>[] { MethodSecurityConfig.class, ServerServiceConfig.class, ActivitiConfig.class, AspectJConfig.class }));
				}
			}
		}
		try {
			configClasses.add(AppInitializer.class.getClassLoader().loadClass("mx.com.gunix.config.AppSpringConfig"));
		} catch (ClassNotFoundException ignorar) {

		}
		return configClasses.toArray(new Class<?>[] {});
	}
}
