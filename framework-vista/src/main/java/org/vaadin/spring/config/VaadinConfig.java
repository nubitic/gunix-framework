package org.vaadin.spring.config;

import mx.com.gunix.framework.ui.vaadin.spring.SpringViewProvider;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

public class VaadinConfig extends VaadinConfiguration {
	private ApplicationContext applicationContext;
	private BeanDefinitionRegistry beanDefinitionRegistry;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		super.setApplicationContext(applicationContext);
		this.applicationContext = applicationContext;
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		super.postProcessBeanDefinitionRegistry(registry);
		this.beanDefinitionRegistry = registry;
	}

	@Bean(name = "overridedViewProvider")
	org.vaadin.spring.navigator.SpringViewProvider viewProvider() {
		return null;
	}

	@Bean(name = "viewProvider")
	SpringViewProvider springViewProvider() {
		return new SpringViewProvider(applicationContext, beanDefinitionRegistry);
	}

}
