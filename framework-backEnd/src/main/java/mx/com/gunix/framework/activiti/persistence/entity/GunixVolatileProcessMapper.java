package mx.com.gunix.framework.activiti.persistence.entity;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface GunixVolatileProcessMapper {
	
	
	@Select("select id_ from activiti.act_re_procdef where category_ = 'VOLATIL'")
	public List<String> obtainVolatileProcessDefinitionIds();
	
	@Select("select id_ from activiti.act_hi_procinst where proc_def_id_ = #{processDefinitionId} and start_time_ < #{date}")
	public List<String> obtainProcessInstanceIdsByProcessDefinitionId(@Param("processDefinitionId") String processDefinitionId, @Param("date") Date date);
		
	
	@Delete("delete from activiti.ACT_HI_ACTINST where PROC_INST_ID_ = #{processInstanceId} ")
	public void deleteFromActHiActinstByProcessInstanceId(@Param("processInstanceId") String processInstanceId);
	
	@Delete("delete from activiti.ACT_HI_ATTACHMENT where PROC_INST_ID_ = #{processInstanceId} ")
	public void deleteFromActHiAttachmenttByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	@Delete("delete from activiti.ACT_HI_COMMENT where PROC_INST_ID_ = #{processInstanceId} ")
	public void deleteFromActHiCommentByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	@Delete("delete from activiti.ACT_HI_DETAIL where PROC_INST_ID_ = #{processInstanceId} ")
	public void deleteFromActHiDetailByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	@Delete("delete from activiti.ACT_HI_IDENTITYLINK where PROC_INST_ID_ = #{processInstanceId} ")
	public void deleteFromActHiIdentityLinkByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	@Delete("delete from activiti.ACT_HI_TASKINST where PROC_INST_ID_ = #{processInstanceId} ")
	public void deleteFromActHiTaskInstByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	@Delete("delete from activiti.ACT_RU_EVENT_SUBSCR where PROC_INST_ID_ = #{processInstanceId} ")
	public void deleteFromActRuEventSubcrByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	@Delete("delete from activiti.ACT_RU_IDENTITYLINK where PROC_INST_ID_ = #{processInstanceId} Or TASK_ID_ in (select ID_ from activiti.ACT_RU_TASK where PROC_INST_ID_ = #{processInstanceId}) ")
	public void deleteFromActTaskIdByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	@Delete("delete from activiti.ACT_RU_TASK where PROC_INST_ID_ = #{processInstanceId} ")
	public void deleteFromActRuTaskByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	@Delete("delete from activiti.ACT_RU_JOB where PROCESS_INSTANCE_ID_ = #{processInstanceId} ")
	public void deleteFromActRuJobByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	@Delete("delete from activiti.ACT_RU_VARIABLE where PROC_INST_ID_ = #{processInstanceId} ")
	public void deleteFromActRuVariableByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	@Delete("delete from activiti.ACT_HI_VARINST where PROC_INST_ID_ = #{processInstanceId} ")
	public void deleteFromActHiVarinstByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	@Delete("delete from activiti.ACT_RU_EXECUTION where proc_inst_id_ = #{processInstanceId} ")
	public void deleteFromActRuExecutionByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	@Delete("delete from activiti.ACT_HI_PROCINST where id_ = #{processInstanceId}")
	public void deleteFromActHiProcInstByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	

}
