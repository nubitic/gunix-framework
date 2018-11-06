package mx.com.gunix.framework.util;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import mx.com.gunix.framework.util.SystemConfigurationParameter;

public interface SystemConfigParameterMapper {

	public List<SystemConfigurationParameter> getAllParameters();
	public SystemConfigurationParameter getParameterByKey(@Param("name") String clave );
}
