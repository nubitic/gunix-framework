package mx.com.gunix.framework.activiti.persistence.entity;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

public interface VariableInstanceMapper {
	public static String DOUBLE_KEY = "double_";
	public static String LONG_KEY = "long_";
	public static String TEXT_KEY = "text_";
	
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
			"	  EXECUTION_ID_ = #{executionId} and TASK_ID_ is null and REV_ >= #{revision}"+
			"	AND type_ <> '"+GunixObjectVariableType.GUNIX_OBJECT+"' and (name_ LIKE '${varName}%' or name_ LIKE '${varName}[%' or name_ LIKE '${varName}(%')" + 
			" order by name_ ")
	@ResultType(Map.class)
	@Options(flushCache=true)
	public List<Map<String,Object>> findGunixObjectByNameAndExecutionIdAndRevision(@Param("executionId") String executionId, @Param("varName") String varName, @Param("revision") Integer revision);

	@Select("SELECT"+
			"	  id_ ,"+
			"	  rev_ ,"+
			"	  name_ as key,"+
			"	  " + DOUBLE_KEY + " ,"+
			"	  " + LONG_KEY + " ,"+
			"	  text_||COALESCE(text2_,'')   as " + TEXT_KEY + 
			"	FROM"+
			"	  ACTIVITI.ACT_RU_VARIABLE "+
			"	WHERE"+
			"	  EXECUTION_ID_ = #{executionId} and TASK_ID_ is null"+
			"   AND rev_ >= (select rev_ from ACTIVITI.ACT_RU_VARIABLE v2 where v2.EXECUTION_ID_ = #{executionId} and v2.TASK_ID_ is null and v2.type_='"+GunixObjectVariableType.GUNIX_OBJECT+"' and strpos(#{varName},v2.name_)=1)" +
			"	AND type_ <> '"+GunixObjectVariableType.GUNIX_OBJECT+"' and (name_ LIKE '${varName}%' or name_ LIKE '${varName}[%' or name_ LIKE '${varName}(%')" + 
			" order by name_ ")
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
			"	  ACTIVITI.act_hi_varinst"+
			"	WHERE"+
			"	  EXECUTION_ID_ = #{executionId} and TASK_ID_ is null"+
			"   AND rev_ >= (select rev_ from ACTIVITI.act_hi_varinst v2 where v2.EXECUTION_ID_ = #{executionId} and v2.TASK_ID_ is null and v2.var_type_='"+GunixObjectVariableType.GUNIX_OBJECT+"' and strpos(#{varName},v2.name_)=1)" +
			"	AND var_type_ <> '"+GunixObjectVariableType.GUNIX_OBJECT+"' and (name_ LIKE '${varName}%' or name_ LIKE '${varName}[%'  or name_ LIKE '${varName}(%')" + 
			" order by name_ ")
	@ResultType(Map.class)
	@Options(flushCache=true)
	public List<Map<String,Object>> findHistoricGunixObjectByNameAndExecutionId(@Param("executionId") String executionId, @Param("varName") String varName);
	
	@Select("SELECT"+
			"	  id_ ,"+
			"	  rev_ ,"+
			"	  name_ as key,"+
			"	  " + DOUBLE_KEY + " ,"+
			"	  " + LONG_KEY + " ,"+
			"	  text_||COALESCE(text2_,'')   as " + TEXT_KEY + 
			"	FROM"+
			"	  ACTIVITI.act_hi_varinst"+
			"	WHERE"+
			"	  EXECUTION_ID_ = #{executionId} and TASK_ID_ is null"+
			"	AND var_type_ <> '"+GunixObjectVariableType.GUNIX_OBJECT+"' and (name_ LIKE '${varName}%' or name_ LIKE '${varName}[%' or name_ LIKE '${varName}(%')" + 
			" order by name_ ")
	@ResultType(Map.class)
	@Options(flushCache=true)
	public List<Map<String,Object>> findHistoricGunixObjectByNameAndExecutionIdAndRevision(@Param("executionId") String executionId, @Param("varName") String varName);
	
	@Delete("delete from ACTIVITI.ACT_RU_VARIABLE where ID_ = #{id,jdbcType=VARCHAR} and REV_ = #{revision}")
	public void delete(@Param("id") String id, @Param("revision") int revision);
}
