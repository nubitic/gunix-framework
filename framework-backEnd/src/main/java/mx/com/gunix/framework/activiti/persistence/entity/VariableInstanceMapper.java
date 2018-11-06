package mx.com.gunix.framework.activiti.persistence.entity;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Options.FlushCachePolicy;
import org.apache.ibatis.annotations.Param;

public interface VariableInstanceMapper {

	@Options(flushCache=FlushCachePolicy.TRUE)
	public List<Map<String,Object>> findGunixObjectByNameAndExecutionIdAndRevision(@Param("executionId") String executionId, @Param("varName") String varName, @Param("revision") Integer revision);

	@Options(flushCache=FlushCachePolicy.TRUE)
	public List<Map<String,Object>> findGunixObjectByNameAndExecutionId(@Param("executionId") String executionId, @Param("varName") String varName);
	
	@Options(flushCache=FlushCachePolicy.TRUE)
	public List<Map<String,Object>> findHistoricGunixObjectByNameAndExecutionId(@Param("executionId") String executionId, @Param("varName") String varName);
	
	@Options(flushCache=FlushCachePolicy.TRUE)
	public List<Map<String,Object>> findHistoricGunixObjectByNameAndExecutionIdAndRevision(@Param("executionId") String executionId, @Param("varName") String varName);
	
	public void delete(@Param("executionId") String executionId,@Param("varName") String varName, @Param("revision") int revision);
}
