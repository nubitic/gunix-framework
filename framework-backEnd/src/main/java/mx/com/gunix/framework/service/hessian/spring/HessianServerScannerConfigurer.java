package mx.com.gunix.framework.service.hessian.spring;

import static org.springframework.util.Assert.notNull;

import java.util.HashMap;
import java.util.Map;

import mx.com.gunix.framework.spring.ClassPathBeanDefinitionScanner;
import mx.com.gunix.framework.spring.ScannerConfigurer;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * hessian
 * 
 * @author rocca.peng@hunteron.com
 * @Description
 * @Date 2015
 */
public class HessianServerScannerConfigurer extends ScannerConfigurer {
	private Map<String, String> implClassContextName = new HashMap<String, String>();

	@Override
	protected void doAfterPropertiesSet() {
		notNull(this.annotationClass, "Property 'annotationClass' is required " + beanName);
		ConfigurableWebApplicationContext context = (ConfigurableWebApplicationContext) applicationContext;
		BeanFactory parentBeanFactory = context.getParentBeanFactory();
		context = parentBeanFactory != null ? (ConfigurableWebApplicationContext) parentBeanFactory : context;

		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();

		beanFactory.getBeanNamesIterator().forEachRemaining(beanName -> {
			try {
				BeanDefinition bd = beanFactory.getBeanDefinition(beanName);

				if (bd.isSingleton()) {
					Class<?>[] actualInterfaces = getActualInterfaces(bd);
					for (Class<?> actualInterface : actualInterfaces) {
						implClassContextName.put(actualInterface.getName(), beanName);
					}
				}
			} catch (NoSuchBeanDefinitionException ignorar) {
			}
		});

		setNameGenerator(new AnnotationBeanNameGenerator() {
			@Override
			protected String buildDefaultBeanName(BeanDefinition definition) {
				AnnotationMetadata metadata = ((ScannedGenericBeanDefinition) definition).getMetadata();
				Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(annotationClass.getName());
				String uri = (String) annotationAttributes.get("value");
				return uri;
			}
		});
	}

	private Class<?>[] getActualInterfaces(BeanDefinition bd) {
		if (bd.getBeanClassName() != null) {
			try {
				Class<?> beanClass = getClass().getClassLoader().loadClass(bd.getBeanClassName());
				return beanClass.getInterfaces();
			} catch (Exception e) {
				logger.error(bd + " find Actual Interface error", e);
			}
		}
		return new Class[0];
	}

	@Override
	protected ClassPathBeanDefinitionScanner getScanner(BeanDefinitionRegistry registry) {
		return new HessianServerClassPathBeanDefinitionScanner(implClassContextName, registry, annotationClass, markerInterface);
	}

}