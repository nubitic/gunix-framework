package mx.com.gunix.framework.config;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.MissingResourceException;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;
import org.vaadin.spring.config.VaadinConfig;
import org.vaadin.spring.servlet.SpringAwareUIProvider;
import org.vaadin.spring.servlet.SpringAwareVaadinServlet;

import com.hunteron.core.Context;
import com.vaadin.server.Constants;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.UICreateEvent;

import mx.com.gunix.framework.mail.config.MailConfig;

public class AppInitializer extends AbstractSecurityWebApplicationInitializer {

	@Override
	protected void afterSpringSecurityFilterChain(ServletContext servletContext) {
		DelegatingFilterProxy httprf = new DelegatingFilterProxy("httpResponseFilter");
		FilterRegistration.Dynamic httprfReg = servletContext.addFilter("httpResponseFilter", httprf);
		httprfReg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");

		RequestContextListener rcl = new RequestContextListener();
		servletContext.addListener(rcl);

		SpringAwareVaadinServlet savs = new SpringAwareVaadinServlet() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void servletInitialized() throws ServletException {
				getService().addSessionInitListener(new SessionInitListener() {
					private static final long serialVersionUID = 1L;

					@Override
					public void sessionInit(SessionInitEvent sessionInitEvent) throws ServiceException {
						WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
						SpringAwareUIProvider uiProvider = new SpringAwareUIProvider(webApplicationContext){
							private static final long serialVersionUID = 1L;

							@Override
							public String getTheme(UICreateEvent event) {
								return Context.VIEW_THEME.get();
							}
							
						};
						sessionInitEvent.getSession().addUIProvider(uiProvider);
					}
				});
			}
		};
		ServletRegistration.Dynamic savsReg = servletContext.addServlet(SpringAwareVaadinServlet.class.getName(), savs);
		savsReg.setAsyncSupported(true);
		savsReg.setLoadOnStartup(2);
		savsReg.setInitParameter("widgetset", "mx.com.gunix.framework.ui.vaadin.GunixWidgetset");
		savsReg.setInitParameter("productionMode", "true".equals(Context.VIEW_VAADIN_ENABLE_DEBUG_MODE.get()) ? "false" : "true");
		if(!"".equals(Context.VIEW_VAADIN_STATIC_RESOURCES.get())){
			savsReg.setInitParameter(Constants.PARAMETER_VAADIN_RESOURCES, Context.VIEW_VAADIN_STATIC_RESOURCES.get());
		}
		savsReg.addMapping("/VAADIN/*", "/" + VaadinSecurityConfig.VAADIN_LOCATION + "*");

		DispatcherServlet ds = new DispatcherServlet();
		ServletRegistration.Dynamic dsReg = servletContext.addServlet(DispatcherServlet.class.getName(), ds);
		dsReg.setLoadOnStartup(3);
		dsReg.setInitParameter("contextClass", "org.springframework.web.context.support.AnnotationConfigWebApplicationContext");
		dsReg.addMapping("/");
	}

	public AppInitializer() {
		super(doLoadClientServiceClass());
	}

	private static Class<?>[] doLoadClientServiceClass() {
		List<Class<?>> configClasses = new ArrayList<Class<?>>();
		configClasses.add(FrameworkClientServiceConfig.class);

		configClasses.add(VaadinConfig.class);
		configClasses.add(VaadinSecurityConfig.class);
		configClasses.add(SpringMVCConfig.class);

		configClasses.add(AspectJConfig.class);
		configClasses.add(MailConfig.class);

		if (Boolean.parseBoolean(Context.SOPORTE_DOCUMENTAL_ENABLED.get())) {
			try {
				configClasses.add(AppInitializer.class.getClassLoader().loadClass("mx.com.gunix.framework.documents.config.SoporteDocumentalConfig"));
			} catch (ClassNotFoundException e) {
				throw new MissingResourceException("No fue posible cargar la clase porque no existe","mx.com.gunix.framework.documents.config.SoporteDocumentalConfig","");
			}
		}
		try {
			configClasses.add(AppInitializer.class.getClassLoader().loadClass("mx.com.gunix.config.ClientServiceConfig"));
		} catch (ClassNotFoundException e) {
		}
		return configClasses.toArray(new Class<?>[] {});
	}
}
