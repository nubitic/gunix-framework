package mx.com.gunix.framework.activiti.persistence.entity;

import java.util.Date;

import org.apache.ibatis.annotations.Param;

public interface GunixVolatileProcessMapper {
	
	public void deleteProcessInstanceIdByTenantIDltDate(@Param("idAplicacion") String idAplicacion, @Param("date") Date date);

}
