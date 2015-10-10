package mx.com.gunix.framework.josso.spring;

import java.lang.reflect.Field;

import org.josso.gateway.assertion.service.store.db.DataSourceAssertionStore;
import org.josso.gateway.identity.service.store.db.DataSourceIdentityStore;
import org.josso.gateway.session.service.store.db.DataSourceSessionStore;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

public class DataSourceStoreBeansPostProcessor implements BeanPostProcessor, ApplicationContextAware {
	private ApplicationContext ac;

	@Override
	public void setApplicationContext(ApplicationContext ac) throws BeansException {
		this.ac = ac;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Field dataSourceField = null;
		Field dsJndiNameField = null;
		if (beanName.equals("josso-identity-store") && bean instanceof DataSourceIdentityStore) {
			ReflectionUtils.makeAccessible((dataSourceField = ReflectionUtils.findField(DataSourceIdentityStore.class, "_datasource")));
			ReflectionUtils.makeAccessible((dsJndiNameField = ReflectionUtils.findField(DataSourceIdentityStore.class, "_dsJndiName")));
			ReflectionUtils.setField(dataSourceField, bean, ac.getBean((String) ReflectionUtils.getField(dsJndiNameField, bean)));
		} else {
			if (beanName.equals("josso-session-store") && bean instanceof DataSourceSessionStore) {
				ReflectionUtils.makeAccessible((dataSourceField = ReflectionUtils.findField(DataSourceSessionStore.class, "_datasource")));
				ReflectionUtils.makeAccessible((dsJndiNameField = ReflectionUtils.findField(DataSourceSessionStore.class, "_dsJndiName")));
				ReflectionUtils.setField(dataSourceField, bean, ac.getBean((String) ReflectionUtils.getField(dsJndiNameField, bean)));
			} else {
				if (beanName.equals("josso-assertion-store") && bean instanceof DataSourceAssertionStore) {
					ReflectionUtils.makeAccessible((dataSourceField = ReflectionUtils.findField(DataSourceAssertionStore.class, "_datasource")));
					ReflectionUtils.makeAccessible((dsJndiNameField = ReflectionUtils.findField(DataSourceAssertionStore.class, "_dsJndiName")));
					ReflectionUtils.setField(dataSourceField, bean, ac.getBean((String) ReflectionUtils.getField(dsJndiNameField, bean)));
				}	
			}			
		}
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
