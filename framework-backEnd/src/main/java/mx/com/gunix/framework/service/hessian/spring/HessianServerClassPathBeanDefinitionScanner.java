package mx.com.gunix.framework.service.hessian.spring;

import java.lang.annotation.Annotation;

import java.util.Map;

import mx.com.gunix.framework.service.hessian.HessianServiceExporter;
import mx.com.gunix.framework.spring.ClassPathBeanDefinitionScanner;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.util.StringUtils;

/**
 * hessian
 * 
 * @author rocca.peng@hunteron.com
 * @Description
 * @Date 2015
 */
public class HessianServerClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {
	private Map<String, String> implClassContextName;

	public HessianServerClassPathBeanDefinitionScanner(Map<String, String> implClassContextName, BeanDefinitionRegistry registry, Class<? extends Annotation> annotationClass, Class<?> markerInterface) {
		super(registry, annotationClass, markerInterface);
		this.implClassContextName = implClassContextName;
	}

	@Override
	protected void completaDefinicion(GenericBeanDefinition definition) {
		// the mapper interface is the original class of the bean
		// but, the actual class of the bean is
		// HessianServiceExporter
		definition.getPropertyValues().add("serviceInterface", definition.getBeanClassName());
		String beanNameRef = implClassContextName.get(definition.getBeanClassName());
		definition.getPropertyValues().add("service", new RuntimeBeanReference(beanNameRef));
		definition.setBeanClass(HessianServiceExporter.class);
	}

	@Override
	protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
		String implBeanName = implClassContextName.get(beanDefinition.getBeanClassName());
		if (!StringUtils.isEmpty(implBeanName) && super.checkCandidate(beanName, beanDefinition)) {
			return true;
		} else {
			logger.warn("Skipping HessianServiceExporter with name '" + beanName + "' and '" + beanDefinition.getBeanClassName() + "' serviceInterface " + ". Bean already defined with the same name!");
			return false;
		}
	}
}
