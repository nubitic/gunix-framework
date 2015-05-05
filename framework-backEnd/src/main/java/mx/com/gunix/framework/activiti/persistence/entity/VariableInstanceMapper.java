package mx.com.gunix.framework.activiti.persistence.entity;

import java.util.List;
import java.util.Map;

import mx.com.gunix.framework.activiti.GunixObjectVariableType;

import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

public interface VariableInstanceMapper {	
	@Select("SELECT"+
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
			"	  ACT_RU_VARIABLE"+
			"	WHERE"+
			"	  TASK_ID_ = #{taskId}"+
			"	AND name_ NOT LIKE '%.%' and type_ <> '"+GunixObjectVariableType.GUNIX_OBJECT+"'"+
			"	UNION ALL"+
			"	SELECT"+
			"	  id_ ,"+
			"	  rev_ ,"+
			"	  '"+GunixObjectVariableType.GUNIX_OBJECT+"'::text type_ ,"+
			"	  substring(name_ FROM 1 FOR position('.' IN name_)-1) name_ ,"+
			"	  execution_id_ ,"+
			"	  proc_inst_id_ ,"+
			"	  task_id_ ,"+
			"	  bytearray_id_ ,"+
			"	  double_ ,"+
			"	  long_ ,"+
			"	  text_ ,"+
			"	  text2_"+
			"	FROM"+
			"	  ACT_RU_VARIABLE"+
			"	WHERE"+
			"	  TASK_ID_ = #{taskId}"+
			"	AND name_ LIKE '%.%' and type_ <> '"+GunixObjectVariableType.GUNIX_OBJECT+"'"+
			"	AND substring(name_ FROM position('.' IN name_)+1) ='class'")
	@ResultMap("variableInstanceResultMap")
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
			"	  double_ ,"+
			"	  long_ ,"+
			"	  text_ ,"+
			"	  text2_"+
			"	FROM"+
			"	  ACT_RU_VARIABLE"+
			"	WHERE"+
			"	  EXECUTION_ID_ = #{executionId} and TASK_ID_ is null and type_ <> '"+GunixObjectVariableType.GUNIX_OBJECT+"'"+
			"	AND name_ NOT LIKE '%.%'"+
			"	UNION ALL"+
			"	SELECT"+
			"	  id_ ,"+
			"	  rev_ ,"+
			"	  '"+GunixObjectVariableType.GUNIX_OBJECT+"'::text type_ ,"+
			"	  substring(name_ FROM 1 FOR position('.' IN name_)-1) name_ ,"+
			"	  execution_id_ ,"+
			"	  proc_inst_id_ ,"+
			"	  task_id_ ,"+
			"	  bytearray_id_ ,"+
			"	  double_ ,"+
			"	  long_ ,"+
			"	  text_ ,"+
			"	  text2_"+
			"	FROM"+
			"	  ACT_RU_VARIABLE"+
			"	WHERE"+
			"	  EXECUTION_ID_ = #{executionId} and TASK_ID_ is null"+
			"	AND name_ LIKE '%.%' and type_ <> '"+GunixObjectVariableType.GUNIX_OBJECT+"'"+
			"	AND substring(name_ FROM position('.' IN name_)+1) ='class'")
	@ResultMap("variableInstanceResultMap")
	public List<VariableInstanceEntity>  findVariableInstancesByExecutionId(String executionId);

	
	@Select("SELECT"+
			"	  name_ as key,"+
			"	  COALESCE(double_::text,long_::text ,text_||COALESCE(text2_,'')) as value"+
			"	FROM"+
			"	  ACT_RU_VARIABLE"+
			"	WHERE"+
			"	  EXECUTION_ID_ = #{executionId} and TASK_ID_ is null"+
			"	AND name_ LIKE #{varName} and type_ <> '"+GunixObjectVariableType.GUNIX_OBJECT+"'")
	@ResultType(Map.class)
	public List<Map<String,String>> findGunixObjectByNameAndExecutionId(@Param("executionId") String executionId, @Param("varName") String varName);

	@Select("SELECT"+
			"	  name_ as key,"+
			"	  COALESCE(double_::text,long_::text ,text_||COALESCE(text2_,'')) as value"+
			"	FROM"+
			"	  ACT_RU_VARIABLE"+
			"	WHERE"+
			"	  TASK_ID_ = #{taskId}"+
			"	AND name_ LIKE #{varName} and type_ <> '"+GunixObjectVariableType.GUNIX_OBJECT+"'")
	@ResultType(Map.class)
	public List<Map<String,String>> findGunixObjectByNameAndTaskId(@Param("taskId") String taskId, @Param("varName") String varName);
}
