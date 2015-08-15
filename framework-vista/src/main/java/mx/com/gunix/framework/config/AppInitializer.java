package mx.com.gunix.framework.config;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;
import org.vaadin.spring.config.VaadinConfig;
import org.vaadin.spring.servlet.SpringAwareVaadinServlet;

public class AppInitializer extends AbstractSecurityWebApplicationInitializer {
	static boolean isVaadin = !"SPRING_MVC".equals(System.getenv("VIEW_ENGINE"));

	@Override
	protected void afterSpringSecurityFilterChain(ServletContext servletContext) {		
		if (isVaadin) {
			DelegatingFilterProxy httprf = new DelegatingFilterProxy("httpResponseFilter");
			FilterRegistration.Dynamic httprfReg = servletContext.addFilter("httpResponseFilter", httprf);
			httprfReg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
			
			RequestContextListener rcl = new RequestContextListener();
			servletContext.addListener(rcl);

			SpringAwareVaadinServlet savs = new SpringAwareVaadinServlet();
			ServletRegistration.Dynamic savsReg = servletContext.addServlet(SpringAwareVaadinServlet.class.getName(), savs);
			savsReg.setAsyncSupported(true);
			savsReg.setLoadOnStartup(1);
			savsReg.setInitParameter("widgetset", "mx.com.gunix.framework.ui.vaadin.GunixWidgetset");
			savsReg.setInitParameter("productionMode", "true");
			savsReg.addMapping("/*");
		}else {
			DispatcherServlet ds = new DispatcherServlet();
			ServletRegistration.Dynamic dsReg = servletContext.addServlet(DispatcherServlet.class.getName(), ds);
			dsReg.setLoadOnStartup(1);
			dsReg.setInitParameter("contextClass", "org.springframework.web.context.support.AnnotationConfigWebApplicationContext");
			dsReg.addMapping("/");
		}
	}

	public AppInitializer() {
		super(doLoadClientServiceClass());
	}

	private static Class<?>[] doLoadClientServiceClass() {
		List<Class<?>> configClasses = new ArrayList<Class<?>>();
		configClasses.add(FrameworkClientServiceConfig.class);
		
		if (isVaadin) {
			configClasses.add(VaadinConfig.class);
			configClasses.add(VaadinSecurityConfig.class);
		}else {
			configClasses.add(SpringMVCConfig.class);
			configClasses.add(SpringMVCSecurityConfig.class);
		}

		configClasses.add(AspectJConfig.class);
		try {
			configClasses.add(AppInitializer.class.getClassLoader().loadClass("mx.com.gunix.config.ClientServiceConfig"));
		} catch (ClassNotFoundException e) {
		}
		return configClasses.toArray(new Class<?>[] {});
	}
}
