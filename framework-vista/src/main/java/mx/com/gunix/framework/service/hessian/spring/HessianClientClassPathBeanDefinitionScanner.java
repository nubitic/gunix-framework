package mx.com.gunix.framework.service.hessian.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import mx.com.gunix.framework.spring.ClassPathBeanDefinitionScanner;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;
import org.springframework.security.core.userdetails.UserDetails;

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
		definition.getPropertyValues().add("serviceInterface", appendUsuario(definition.getBeanClassName()));
		definition.setBeanClass(HessianProxyFactoryBean.class);
	}
	
	private Class<?> appendUsuario(String beanClassName) {
		try {
			Class<?> serviceInterface = getClass().getClassLoader().loadClass(beanClassName);
			
			DynamicType.Builder<?> builder = new ByteBuddy().makeInterface(serviceInterface);

			for(Method m:serviceInterface.getMethods()){
				Class<?>[] args = new Class<?>[m.getParameterCount()+1];
				System.arraycopy(m.getParameterTypes(), 0, args, 0, m.getParameterCount());
				args[args.length-1]=UserDetails.class;
				builder = builder.defineMethod(m.getName(), m.getReturnType(), Arrays.asList(args), Visibility.PUBLIC)
								 .withoutCode();
			}
			
			return builder.make()
				.load(getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
				.getLoaded();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} 
	}
}
