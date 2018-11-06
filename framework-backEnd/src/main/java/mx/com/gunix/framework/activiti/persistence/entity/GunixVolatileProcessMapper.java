package mx.com.gunix.framework.activiti.persistence.entity;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface GunixVolatileProcessMapper {
	
	public List<String> obtainVolatileProcessDefinitionIds();
	
	public List<String> obtainProcessInstanceIdsByProcessDefinitionId(@Param("processDefinitionId") String processDefinitionId, @Param("date") Date date);
	
	public void deleteFromActHiActinstByProcessInstanceId(@Param("processInstanceId") String processInstanceId);
	
	public void deleteFromActHiAttachmenttByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	public void deleteFromActHiCommentByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	public void deleteFromActHiDetailByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	public void deleteFromActHiIdentityLinkByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	public void deleteFromActHiTaskInstByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	public void deleteFromActRuEventSubcrByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	public void deleteFromActTaskIdByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	public void deleteFromActRuTaskByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	public void deleteFromActRuJobByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	public void deleteFromActRuVariableByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	public void deleteFromActHiVarinstByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	public void deleteFromActRuExecutionByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

	public void deleteFromActHiProcInstByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

}
