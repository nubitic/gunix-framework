package com.hunteron.api;

import static org.springframework.util.Assert.notNull;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import com.hunteron.core.Context;

/**
 * hessian 接口客户端自动扫描注入
 * 
 * @author rocca.peng@hunteron.com
 * @Description
 * @Date 2015年2月8日 上午10:29:58
 */
public class HessianClientScannerConfigurer implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private String beanName;
	private String basePackage;

	private Resource[] locations;

	private boolean includeAnnotationConfig = true;

	private ApplicationContext applicationContext;

	// 实现了该接口
	private Class<?> markerInterface;
	// 配置了该注解
	private Class<? extends Annotation> annotationClass;

	private BeanNameGenerator nameGenerator;

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		notNull(this.basePackage, "Property 'basePackage' is required " + beanName);
		Properties props = new Properties();
		loadProperties(props);
		for (Entry<Object, Object> resource : props.entrySet()) {
			System.setProperty((String) resource.getKey(), (String) resource.getValue());
		}
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		HessianClassPathScanner scan = new HessianClassPathScanner(registry);
		scan.setResourceLoader(this.applicationContext);
		scan.setBeanNameGenerator(this.nameGenerator);
		// 引入注解配置
		scan.setIncludeAnnotationConfig(this.includeAnnotationConfig);
		scan.registerFilters();

		String[] basePackages = StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
		scan.scan(basePackages);
	}
	
	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

	public void setIncludeAnnotationConfig(boolean includeAnnotationConfig) {
		this.includeAnnotationConfig = includeAnnotationConfig;
	}

	public Class<?> getMarkerInterface() {
		return markerInterface;
	}

	public void setMarkerInterface(Class<?> markerInterface) {
		this.markerInterface = markerInterface;
	}

	public Class<? extends Annotation> getAnnotationClass() {
		return annotationClass;
	}

	public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
		this.annotationClass = annotationClass;
	}

	public BeanNameGenerator getNameGenerator() {
		return nameGenerator;
	}

	public void setNameGenerator(BeanNameGenerator nameGenerator) {
		this.nameGenerator = nameGenerator;
	}

	public void setLocations(Resource... locations) {
		this.locations = locations;
	}

	protected void loadProperties(Properties props) throws IOException {
		if (this.locations != null) {
			for (Resource location : this.locations) {
				if (logger.isInfoEnabled()) {
					logger.info("Loading properties file from " + location);
				}
				try {
					PropertiesLoaderUtils.fillProperties(props, location);
				} catch (IOException ex) {
					if (logger.isWarnEnabled()) {
						logger.warn("Could not load properties from " + location + ": " + ex.getMessage());
					}
				}
			}
		}
	}

	private class HessianClassPathScanner extends ClassPathBeanDefinitionScanner {
		protected final Logger logger = LoggerFactory.getLogger(getClass());

		public HessianClassPathScanner(BeanDefinitionRegistry registry) {
			super(registry, false);
		}

		@Override
		public Set<BeanDefinitionHolder> doScan(String... basePackages) {
			Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
			if (beanDefinitions.isEmpty()) {
				logger.warn("No hessian was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
			} else {
				for (BeanDefinitionHolder holder : beanDefinitions) {
					GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();

					if (logger.isDebugEnabled()) {
						logger.debug("Creating HessianFactoryBean with name '" + holder.getBeanName() + "' and '" + definition.getBeanClassName() + "' hessianInterface");
					}

					AnnotationMetadata metadata = ((ScannedGenericBeanDefinition) definition).getMetadata();
					Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(annotationClass.getName());
					Context host = (Context) annotationAttributes.get("host");
					String uri = (String) annotationAttributes.get("value");
					
					definition.getPropertyValues().add("serviceUrl", host.getRemoteUrl() + uri);
					definition.getPropertyValues().add("serviceInterface", appendUsuario(definition.getBeanClassName()));
					definition.setBeanClass(HessianProxyFactoryBean.class);
				}
			}
			return beanDefinitions;

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

		@Override
		protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
			return (beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
			if (super.checkCandidate(beanName, beanDefinition)) {
				return true;
			} else {
				logger.warn("Skipping HessianFactoryBean with name '" + beanName + "' and '" + beanDefinition.getBeanClassName() + "' hessianInterface" + ". Bean already defined with the same name!");
				return false;
			}
		}

		public void registerFilters() {
			boolean acceptAllInterfaces = true;

			// if specified, use the given annotation and / or marker interface
			if (HessianClientScannerConfigurer.this.annotationClass != null) {
				addIncludeFilter(new AnnotationTypeFilter(HessianClientScannerConfigurer.this.annotationClass));
				acceptAllInterfaces = false;
			}

			// override AssignableTypeFilter to ignore matches on the actual
			// marker interface
			if (HessianClientScannerConfigurer.this.markerInterface != null) {
				addIncludeFilter(new AssignableTypeFilter(HessianClientScannerConfigurer.this.markerInterface) {
					@Override
					protected boolean matchClassName(String className) {
						return false;
					}
				});
				acceptAllInterfaces = false;
			}

			if (acceptAllInterfaces) {
				// default include filter that accepts all classes
				addIncludeFilter(new TypeFilter() {
					public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
						return true;
					}
				});
			}

			// exclude package-info.java
			addExcludeFilter(new TypeFilter() {
				public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
					String className = metadataReader.getClassMetadata().getClassName();
					return className.endsWith("package-info");
				}
			});
		}
	}
}