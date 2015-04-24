package mx.com.gunix.framework.service.hessian.spring;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import mx.com.gunix.framework.spring.ClassPathBeanDefinitionScanner;
import mx.com.gunix.framework.spring.ScannerConfigurer;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * hessian
 * 
 * @author rocca.peng@hunteron.com
 * @Description
 * @Date 2015
 */
public class HessianClientScannerConfigurer extends ScannerConfigurer {
	private Resource[] locations;

	@Override
	protected void doAfterPropertiesSet() {
		Properties props = new Properties();
		loadProperties(props);
		for (Entry<Object, Object> resource : props.entrySet()) {
			System.setProperty((String) resource.getKey(), (String) resource.getValue());
		}
	}

	private void loadProperties(Properties props) {
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

	@Override
	protected ClassPathBeanDefinitionScanner getScanner(BeanDefinitionRegistry registry) {
		return new HessianClientClassPathBeanDefinitionScanner(registry,annotationClass, markerInterface);
	}

	public void setLocations(Resource... locations) {
		this.locations = locations;
	}
}