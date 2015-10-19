package mx.com.gunix.framework.config;

import mx.com.gunix.framework.ui.springmvc.tiles3.AjaxTilesView;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.js.ajax.AjaxUrlBasedViewResolver;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.ResourceBundleThemeSource;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.support.ControllerClassNameHandlerMapping;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;

@EnableWebMvc
@Configuration
@ComponentScan({"mx.com.gunix.ui"})
public class SpringMVCConfig extends WebMvcConfigurerAdapter {
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		registry
			.addResourceHandler(SpringMVCSecurityConfig.STATIC_RESOURCES_PATTERN)
				.addResourceLocations(SpringMVCSecurityConfig.STATIC_RESOURCES_LOCATION,
									  "classpath:/META-INF/resources"+SpringMVCSecurityConfig.STATIC_RESOURCES_LOCATION,
									  "classpath:/VAADIN/widgetsets/mx.com.gunix.framework.ui.vaadin.GunixWidgetset/styles/");
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
		tc.setDefinitions("/WEB-INF/mx/com/gunix/ui/springmvc/framework/tiles-defs.xml");
		return tc;
	}
	
	@Bean
	public HandlerMapping controllerClassNameHandlerMapping() {
		ControllerClassNameHandlerMapping ccnhm = new ControllerClassNameHandlerMapping();
		ccnhm.setOrder(4);
		return ccnhm;
	}
	
	@Override
	public Validator getValidator() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        return validator;
	}
}