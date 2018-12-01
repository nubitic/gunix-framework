package mx.com.gunix.framework.service;

import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.stereotype.Component;

import mx.com.gunix.framework.config.ProcessEngineConfigurationCustomizator;

@Component
public class PostgreSQLProcessEngineConfigurationCustomizator implements ProcessEngineConfigurationCustomizator {

	@Override
	public void doConfigure(SpringProcessEngineConfiguration speConf) {
		speConf.setDatabaseSchema("activiti");
	}

}
