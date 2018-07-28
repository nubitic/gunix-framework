package mx.com.gunix.framework.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.hunteron.core.Context;

import mx.com.gunix.framework.documents.config.LogicalDocConfig;
import mx.com.gunix.framework.mail.config.MailConfig;

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
		configClasses.add(MailConfig.class);

		if (Boolean.valueOf(Context.STANDALONE_APP.get())||Boolean.valueOf(Context.ENABLE_ADMIN_APP_SERVICES.get())) {
			configClasses.add(AdminAppServicesConfig.class);
		}
		
		if (Context.MONGO_DB_NAME.get() != null) {
			configClasses.add(MongoDBConfig.class);
		}
		
		if (Boolean.parseBoolean(Context.LOGICALDOC_ENABLED.get())) {
			configClasses.add(LogicalDocConfig.class);
		}

		try {
			configClasses.add(AppInitializer.class.getClassLoader().loadClass("mx.com.gunix.config.AppSpringConfig"));
		} catch (ClassNotFoundException ignorar) {

		}
		return configClasses.toArray(new Class<?>[] {});
	}
}
