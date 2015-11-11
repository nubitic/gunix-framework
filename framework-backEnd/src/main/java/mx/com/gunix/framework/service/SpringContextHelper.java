package mx.com.gunix.framework.service;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Tomado de: https://objectpartners.com/2010/08/23/gaining-access-to-the-spring-context-in-non-spring-managed-classes/
 * */
@Component
public class SpringContextHelper implements ApplicationContextAware {
	private static ApplicationContext context;

	@SuppressWarnings("static-access")
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

	public static Object getBean(String beanName) {
		return context.getBean(beanName);
	}
}
