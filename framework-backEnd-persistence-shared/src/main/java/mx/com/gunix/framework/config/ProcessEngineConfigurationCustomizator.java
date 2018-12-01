package mx.com.gunix.framework.config;

import org.activiti.spring.SpringProcessEngineConfiguration;

public interface ProcessEngineConfigurationCustomizator {
	public void doConfigure(SpringProcessEngineConfiguration speConf);
}
