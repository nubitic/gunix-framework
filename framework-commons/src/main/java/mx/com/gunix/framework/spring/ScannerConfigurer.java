package mx.com.gunix.framework.spring;

import static org.springframework.util.Assert.notNull;

import java.lang.annotation.Annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

/**
 * hessian
 * 
 * @author rocca.peng@hunteron.com
 * @Description
 * @Date 2015
 */
public abstract class ScannerConfigurer implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware{
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected Class<? extends Annotation> annotationClass;
	protected Class<?> markerInterface;
	protected String beanName;
	protected String basePackage;
	protected ApplicationContext applicationContext;
	private BeanNameGenerator nameGenerator;
	private boolean includeAnnotationConfig = true;

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
	}

	public void afterPropertiesSet() throws Exception {
		notNull(this.basePackage, "Property 'basePackage' is required " + beanName);
		if(this.annotationClass==null && this.markerInterface==null){
			throw new IllegalArgumentException("At least one of 'annotationClass' or 'markerInterface' must be indicated");
		}
		doAfterPropertiesSet();
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		ClassPathBeanDefinitionScanner scanner = getScanner(registry);
		scanner.setResourceLoader(this.applicationContext);
		scanner.setBeanNameGenerator(this.nameGenerator);
		scanner.setIncludeAnnotationConfig(this.includeAnnotationConfig);
		scanner.registerFilters();

		String[] basePackages = StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
		scanner.scan(basePackages);
	}

	public BeanNameGenerator getNameGenerator() {
		return nameGenerator;
	}

	public void setNameGenerator(BeanNameGenerator nameGenerator) {
		this.nameGenerator = nameGenerator;
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

	public void setIncludeAnnotationConfig(boolean includeAnnotationConfig) {
		this.includeAnnotationConfig = includeAnnotationConfig;
	}
	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public Class<? extends Annotation> getAnnotationClass() {
		return annotationClass;
	}

	public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
		this.annotationClass = annotationClass;
	}

	public Class<?> getMarkerInterface() {
		return markerInterface;
	}

	public void setMarkerInterface(Class<?> markerInterface) {
		this.markerInterface = markerInterface;
	}

	protected abstract ClassPathBeanDefinitionScanner getScanner(BeanDefinitionRegistry registry);

	protected abstract void doAfterPropertiesSet();
}
