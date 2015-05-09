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
import org.vaadin.spring.config.VaadinConfig;
import org.vaadin.spring.servlet.SpringAwareVaadinServlet;

public class AppInitializer extends AbstractSecurityWebApplicationInitializer {
	@Override
	protected void afterSpringSecurityFilterChain(ServletContext servletContext) {
		RequestContextListener rcl = new RequestContextListener(); 
		servletContext.addListener(rcl);
		
		DelegatingFilterProxy httprf = new DelegatingFilterProxy("httpResponseFilter");
		FilterRegistration.Dynamic httprfReg = servletContext.addFilter("httpResponseFilter",httprf);
		httprfReg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");		
		
		DelegatingFilterProxy hiddenhttpmf = new DelegatingFilterProxy("hiddenHttpMethodFilter");
		FilterRegistration.Dynamic hiddenhttpmfReg =servletContext.addFilter("hiddenHttpMethodFilter",hiddenhttpmf);
		hiddenhttpmfReg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
		
		SpringAwareVaadinServlet savs = new SpringAwareVaadinServlet();
		ServletRegistration.Dynamic savsReg = servletContext.addServlet(SpringAwareVaadinServlet.class.getName(), savs);
		savsReg.setAsyncSupported(true);
		savsReg.setLoadOnStartup(1);
		savsReg.setInitParameter("widgetset", "mx.com.gunix.framework.ui.vaadin.GunixWidgetset");
		savsReg.addMapping("/*");
	}

	public AppInitializer(){
		super(doLoadClientServiceClass());
	}

	private static Class<?>[] doLoadClientServiceClass() {
		List<Class<?>> configClasses = new ArrayList<Class<?>>();
		configClasses.add(FrameworkClientServiceConfig.class);
		configClasses.add(VaadinConfig.class);
		configClasses.add(VaadinSecurityConfig.class);
		configClasses.add(AspectJConfig.class);
		try {
			configClasses.add(AppInitializer.class.getClassLoader().loadClass("mx.com.gunix.config.ClientServiceConfig"));
		} catch (ClassNotFoundException e) {}
		return configClasses.toArray(new Class<?>[]{});
	}
}
