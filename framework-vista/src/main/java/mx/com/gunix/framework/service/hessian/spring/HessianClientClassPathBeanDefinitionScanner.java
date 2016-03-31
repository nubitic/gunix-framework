package mx.com.gunix.framework.service.hessian.spring;

import java.lang.annotation.Annotation;
import java.util.Map;

import mx.com.gunix.framework.service.hessian.ByteBuddyUtils;
import mx.com.gunix.framework.spring.ClassPathBeanDefinitionScanner;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;

import com.hunteron.core.Context;

public class HessianClientClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

	public HessianClientClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, Class<? extends Annotation> annotationClass, Class<?> markerInterface) {
		super(registry, annotationClass, markerInterface);
	}

	@Override
	protected void completaDefinicion(GenericBeanDefinition definition) {
		AnnotationMetadata metadata = ((ScannedGenericBeanDefinition) definition).getMetadata();
		Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(annotationClass.getName());
		Context host = (Context) annotationAttributes.get("host");
		String uri = (String) annotationAttributes.get("value");
		
		definition.getPropertyValues().add("serviceUrl", host.getRemoteUrl() + uri);
		definition.getPropertyValues().add("serviceInterface", ByteBuddyUtils.appendUsuario(getClass().getClassLoader(), definition.getBeanClassName()));
		definition.setBeanClass(HessianProxyFactoryBean.class);
	}
}
