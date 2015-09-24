package org.vaadin.spring.config;

import mx.com.gunix.framework.ui.vaadin.spring.SpringViewProvider;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.vaadin.spring.internal.BeanStore;
import org.vaadin.spring.navigator.internal.ViewCache;

import com.vaadin.navigator.View;

public class VaadinConfig extends VaadinConfiguration {
	private ApplicationContext applicationContext;
	private BeanDefinitionRegistry beanDefinitionRegistry;

	private BeanStore noCachingBeanStore = new BeanStore(null) {
		private static final long serialVersionUID = 1L;

		@Override
		public Object get(String s, ObjectFactory<?> objectFactory) {
			return super.create(s, objectFactory);
		}
	};

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

	@Bean
	@org.vaadin.spring.annotation.VaadinUIScope
	ViewCache viewCache() {
		return new ViewCache() {
			private static final long serialVersionUID = 1L;

			@Override
			public void creatingView(String viewName) {

			}

			@Override
			public void viewCreated(String viewName, View viewInstance) {

			}

			@Override
			public BeanStore getCurrentViewBeanStore() throws IllegalStateException {
				return noCachingBeanStore;
			}

		};
	}
}
