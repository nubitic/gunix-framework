package mx.com.gunix.framework.config;

import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.js.ajax.AjaxUrlBasedViewResolver;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.ResourceBundleThemeSource;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.support.ControllerClassNameHandlerMapping;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;

import com.hunteron.core.Context;

import mx.com.gunix.framework.security.domain.Funcion;
import mx.com.gunix.framework.ui.springmvc.MainController;
import mx.com.gunix.framework.ui.springmvc.tiles3.AjaxTilesView;
import mx.com.gunix.framework.util.GunixFile;
import mx.com.gunix.framework.util.Utils;

@EnableWebMvc
@Configuration
@ComponentScan({"mx.com.gunix.ui","mx.com.gunix.framework.ui"})
public class SpringMVCConfig extends WebMvcConfigurerAdapter {
	
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		registry
			.addResourceHandler(VaadinSecurityConfig.STATIC_RESOURCES_PATTERN)
				.addResourceLocations(VaadinSecurityConfig.STATIC_RESOURCES_LOCATION,
										"classpath:/META-INF/resources" + VaadinSecurityConfig.STATIC_RESOURCES_LOCATION,
										"classpath:/VAADIN/widgetsets/mx.com.gunix.framework.ui.vaadin.GunixWidgetset/styles/");
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new HandlerMethodArgumentResolver() {
			@Override
			public boolean supportsParameter(MethodParameter parameter) {
				return parameter.getParameterType().equals(GunixFile.class);
			}

			@Override
			public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
				return webRequest.getAttribute(MainController.GUNIX_FILE_UPLOAD_ATTR, NativeWebRequest.SCOPE_REQUEST);
			}
		});
	}

	@Bean
	public InternalResourceViewResolver getInternalResourceViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/mx/com/gunix/ui/springmvc/");
		resolver.setSuffix(".jsp");
		resolver.setOrder(1);
		return resolver;
	}
	
	@Bean
	public AjaxUrlBasedViewResolver getAjaxUrlBasedViewResolver() {
		AjaxUrlBasedViewResolver resolver = new AjaxUrlBasedViewResolver();
		resolver.setViewClass(AjaxTilesView.class);
		resolver.setOrder(0);
		return resolver;
	}

	@Bean
	public ThemeSource themeSource() {
		ResourceBundleThemeSource source = new ResourceBundleThemeSource();
		source.setBasenamePrefix("/mx/com/gunix/framework/ui/springmvc/themes/");
		return source;
	}

	@Bean
	public ThemeResolver themeResolver() {
		FixedThemeResolver resolver = new FixedThemeResolver();
		resolver.setDefaultThemeName("gunix");
		return resolver;
	}
	
	@Bean
	public TilesConfigurer tilesConfigurer() {
		TilesConfigurer tc = new TilesConfigurer();
		tc.setDefinitions("/WEB-INF/mx/com/gunix/ui/springmvc/framework/framework-tiles-defs-gunix.xml",
						  "/WEB-INF/mx/com/gunix/ui/springmvc/**/tiles-defs-*.xml", 
						  "classpath*:/META-INF/resources/WEB-INF/mx/com/gunix/ui/springmvc/**/tiles-defs-*.xml");	
		return tc;
	}
	
	@Bean
	public ControllerClassNameHandlerMapping controllerClassNameHandlerMapping() {
		ControllerClassNameHandlerMapping ccnhm = new ControllerClassNameHandlerMapping();
		ccnhm.setCaseSensitive(true);
		ccnhm.setUseTrailingSlashMatch(true);
		ccnhm.setOrder(4);
		return ccnhm;
	}
	
	@Override
	public Validator getValidator() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        return validator;
	}
	
	
	@Bean
	public MessageSource messageSource() {
		return Utils.buildMessageSource(getClass().getClassLoader());
	}

	@Bean
	public CommonsMultipartResolver multipartResolver() {
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		resolver.setDefaultEncoding("UTF-8");
		return resolver;
	}
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName(Funcion.ViewEngine.SPRINGMVC.name().equals(Context.VIEW_ENGINE.get())?Context.VIEW_INDEX_TILE_DEF.get():"forward:/WEB-INF/mx/com/gunix/ui/springmvc/framework/home.jsp");
	}
}
