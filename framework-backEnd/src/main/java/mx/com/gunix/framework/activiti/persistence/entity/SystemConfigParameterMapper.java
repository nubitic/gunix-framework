package mx.com.gunix.framework.activiti.persistence.entity;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;

import mx.com.gunix.framework.util.SystemConfigurationParameter;

public interface SystemConfigParameterMapper {


	@Results({
		@Result(property = "value", column = "value"),
		@Result(property = "name", column = "name_"),

	})
	@Select("SELECT * FROM gx_system_config_parameter")
	public List<SystemConfigurationParameter> getAllParameters();


	@Results({
		@Result(property = "value", column = "value"),
		@Result(property = "name", column = "name_"),

	})
	@Select("SELECT * FROM gx_system_config_parameter where name_ = #{name}")
	public SystemConfigurationParameter getParameterByKey(@Param("name") String clave );


}
