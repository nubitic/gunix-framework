package mx.com.gunix.framework.activiti.persistence.entity;

import java.util.List;
import java.util.Map;

import mx.com.gunix.framework.activiti.GunixObjectVariableType;

import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

public interface VariableInstanceMapper {
	public static String DOUBLE_KEY = "double_";
	public static String LONG_KEY = "long_";
	public static String TEXT_KEY = "text_";
	@Select("SELECT"+
			"	  id_ ,"+
			"	  rev_ ,"+
			"	  type_ ,"+
			"	  name_ ,"+
			"	  execution_id_ ,"+
			"	  proc_inst_id_ ,"+
			"	  task_id_ ,"+
			"	  bytearray_id_ ,"+
			"	  " + DOUBLE_KEY + " ,"+
			"	  " + LONG_KEY + " ,"+
			"	  " + TEXT_KEY + " ,"+
			"	  text2_"+
			"	FROM"+
			"	  ACTIVITI.ACT_RU_VARIABLE"+
			"	WHERE"+
			"	  TASK_ID_ = #{taskId} "+
			"	and type_ <> '"+GunixObjectVariableType.GUNIX_OBJECT+"'"+
			"	AND name_ NOT LIKE '%.%'"+
			"	AND name_ NOT LIKE '%[%'"+
			"	UNION ALL"+
			"	SELECT"+
			"	  id_ ,"+
			"	  rev_ ,"+
			"	  type_ ,"+
			"	  name_ ,"+
			"	  execution_id_ ,"+
			"	  proc_inst_id_ ,"+
			"	  task_id_ ,"+
			"	  bytearray_id_ ,"+
			"	  double_ ,"+
			"	  long_ ,"+
			"	  text_ ,"+
			"	  text2_"+
			"	FROM"+
			"	  ACTIVITI.ACT_RU_VARIABLE"+
			"	WHERE"+
			"	  TASK_ID_ = #{taskId} "+
			"	AND type_ = '"+GunixObjectVariableType.GUNIX_OBJECT+"'")
	@ResultMap("variableInstanceResultMap")
	@Options(flushCache=true)
	public List<VariableInstanceEntity>  findVariableInstancesByTaskId(String taskId);
	
	@Select("SELECT"+
			"	  id_ ,"+
			"	  rev_ ,"+
			"	  type_ ,"+
			"	  name_ ,"+
			"	  execution_id_ ,"+
			"	  proc_inst_id_ ,"+
			"	  task_id_ ,"+
			"	  bytearray_id_ ,"+
			"	  " + DOUBLE_KEY + " ,"+
			"	  " + LONG_KEY + " ,"+
			"	  " + TEXT_KEY + " ,"+
			"	  text2_"+
			"	FROM"+
			"	  ACTIVITI.ACT_RU_VARIABLE"+
			"	WHERE"+
			"	  EXECUTION_ID_ = #{executionId} and TASK_ID_ is null "+
			"	and type_ <> '"+GunixObjectVariableType.GUNIX_OBJECT+"'"+
			"	AND name_ NOT LIKE '%.%'"+
			"	AND name_ NOT LIKE '%[%'"+
			"	UNION ALL"+
			"	SELECT"+
			"	  id_ ,"+
			"	  rev_ ,"+
			"	  type_ ,"+
			"	  name_ ,"+
			"	  execution_id_ ,"+
			"	  proc_inst_id_ ,"+
			"	  task_id_ ,"+
			"	  bytearray_id_ ,"+
			"	  double_ ,"+
			"	  long_ ,"+
			"	  text_ ,"+
			"	  text2_"+
			"	FROM"+
			"	  ACTIVITI.ACT_RU_VARIABLE"+
			"	WHERE"+
			"	  EXECUTION_ID_ = #{executionId} and TASK_ID_ is null"+
			"	AND type_ = '"+GunixObjectVariableType.GUNIX_OBJECT+"'")
	@ResultMap("variableInstanceResultMap")
	@Options(flushCache=true)
	public List<VariableInstanceEntity>  findVariableInstancesByExecutionId(String executionId);

	
	@Select("SELECT"+
			"	  id_ ,"+
			"	  rev_ ,"+
			"	  name_ as key,"+
			"	  " + DOUBLE_KEY + " ,"+
			"	  " + LONG_KEY + " ,"+
			"	  text_||COALESCE(text2_,'')   as " + TEXT_KEY + 
			"	FROM"+
			"	  ACTIVITI.ACT_RU_VARIABLE"+
			"	WHERE"+
			"	  EXECUTION_ID_ = #{executionId} and TASK_ID_ is null"+
			"	AND type_ <> '"+GunixObjectVariableType.GUNIX_OBJECT+"' and (name_ LIKE '${varName}.%' or name_ LIKE '${varName}[%')")
	@ResultType(Map.class)
	@Options(flushCache=true)
	public List<Map<String,Object>> findGunixObjectByNameAndExecutionId(@Param("executionId") String executionId, @Param("varName") String varName);

	@Select("SELECT"+
			"	  id_ ,"+
			"	  rev_ ,"+
			"	  name_ as key,"+
			"	  " + DOUBLE_KEY + " ,"+
			"	  " + LONG_KEY + " ,"+
			"	  text_||COALESCE(text2_,'')   as " + TEXT_KEY +
			"	FROM"+
			"	  ACTIVITI.ACT_RU_VARIABLE"+
			"	WHERE"+
			"	  TASK_ID_ = #{taskId}"+
			"	AND type_ <> '"+GunixObjectVariableType.GUNIX_OBJECT+"' and (name_ LIKE '${varName}.%' or name_ LIKE '${varName}[%')")
	@ResultType(Map.class)
	@Options(flushCache=true)
	public List<Map<String,Object>> findGunixObjectByNameAndTaskId(@Param("taskId") String taskId, @Param("varName") String varName);
	
	
	@Delete("delete from ACTIVITI.ACT_RU_VARIABLE where ID_ = #{id,jdbcType=VARCHAR} and REV_ = #{revision}")
	public void delete(@Param("id") String id, @Param("revision") int revision);
}
