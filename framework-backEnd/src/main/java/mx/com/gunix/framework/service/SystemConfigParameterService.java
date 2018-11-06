package mx.com.gunix.framework.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mx.com.gunix.framework.util.SystemConfigParameterMapper;
import mx.com.gunix.framework.util.SystemConfigurationParameter;


@Service("systemConfigParameterService")
public class SystemConfigParameterService extends GunixActivitServiceSupport<SystemConfigurationParameter> {

	
	@Autowired
	SystemConfigParameterMapper scpm;
	
	
	public List <SystemConfigurationParameter> getAllParameters(){
		return scpm.getAllParameters();
	}
	
	public SystemConfigurationParameter getParameterByKey(String key){
		if(scpm.getParameterByKey(key) ==  null){
			throw new RuntimeException("El parámetro " + key + " no fue hallado en la tabla de parámetros");
		}
		return scpm.getParameterByKey(key);
	}
	
	
	
}
