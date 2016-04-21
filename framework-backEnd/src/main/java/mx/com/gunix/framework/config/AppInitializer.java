package mx.com.gunix.framework.config;

import java.util.ArrayList;
import java.util.List;

import mx.com.gunix.framework.documents.config.LogicalDocConfig;

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

		configClasses.add(MethodSecurityConfig.class);
		configClasses.add(ServerServiceConfig.class);
		configClasses.add(ActivitiConfig.class);
		configClasses.add(AspectJConfig.class);
		configClasses.add(REDISConfig.class);

		if (Boolean.valueOf(System.getenv("STANDALONE_APP"))) {
			configClasses.add(AdminAppServicesConfig.class);
		}
		
		if (System.getenv("MONGO_DB_NAME") != null) {
			configClasses.add(MongoDBConfig.class);
		}
		
		if (Boolean.parseBoolean(System.getenv("LOGICALDOC_ENABLED"))) {
			configClasses.add(LogicalDocConfig.class);
		}

		try {
			configClasses.add(AppInitializer.class.getClassLoader().loadClass("mx.com.gunix.config.AppSpringConfig"));
		} catch (ClassNotFoundException ignorar) {

		}
		return configClasses.toArray(new Class<?>[] {});
	}
}
