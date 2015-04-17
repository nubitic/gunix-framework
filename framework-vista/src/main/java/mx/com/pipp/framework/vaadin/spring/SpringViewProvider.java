package mx.com.pipp.framework.vaadin.spring;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;

import com.vaadin.navigator.View;

public class SpringViewProvider extends org.vaadin.spring.navigator.SpringViewProvider {
	private static final long serialVersionUID = 1L;

	public SpringViewProvider(ApplicationContext applicationContext, BeanDefinitionRegistry beanDefinitionRegistry) {
		super(applicationContext, beanDefinitionRegistry);
	}

	@Override
	public View getView(String viewName) {
		try {
			View view = getTargetObject(super.getView(viewName), View.class);
			return view;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getTargetObject(Object proxy, Class<T> targetClass) throws Exception {
		if (AopUtils.isJdkDynamicProxy(proxy)) {
			return (T) ((Advised) proxy).getTargetSource().getTarget();
		} else {
			return (T) proxy; // expected to be cglib proxy then, which is
								// simply a specialized class
		}
	}
}
