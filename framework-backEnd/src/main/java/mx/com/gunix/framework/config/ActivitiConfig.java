package mx.com.gunix.framework.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.jobexecutor.DefaultJobExecutor;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.autodeployment.ResourceParentFolderAutoDeploymentStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ActivitiConfig {
	
	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	
	@Bean
	public JobExecutor jobExecutor(){
		DefaultJobExecutor je = new DefaultJobExecutor();
		je.setQueueSize(10);
		je.setCorePoolSize(8);
		je.setMaxPoolSize(20);
		je.setMaxJobsPerAcquisition(10);
		je.setWaitTimeInMillis(7000);
		je.setLockTimeInMillis(7200000);
		return je;
	}

	@Bean
	public IdGenerator idGenerator(){
		return new StrongUuidGenerator();
	}
	
	@Bean
	public SpringProcessEngineConfiguration springProcessEngineConfiguration(DataSource dataSource, PlatformTransactionManager transactionManager) throws IOException {
		SpringProcessEngineConfiguration speConf = new SpringProcessEngineConfiguration();
		speConf.setDataSource(dataSource);
		speConf.setDatabaseType(ProcessEngineConfigurationImpl.DATABASE_TYPE_POSTGRES);
		speConf.setTransactionManager(transactionManager);
		speConf.setDatabaseSchemaUpdate("false");
		speConf.setJobExecutorActivate(true);
		speConf.setDbIdentityUsed(false);
		speConf.setBatchSizeTasks(1000);
		speConf.setJobExecutor(jobExecutor());
		speConf.setIdGenerator(idGenerator());
		speConf.setDeploymentMode(ResourceParentFolderAutoDeploymentStrategy.DEPLOYMENT_MODE);
		speConf.setDeploymentResources(resourcePatternResolver.getResources("classpath*:/mx/com/gunix/procesos/*.bpmn"));
		
		return speConf;
	}

	@Bean
	public ProcessEngineFactoryBean processEngineFactoryBean(SpringProcessEngineConfiguration speConf){
		ProcessEngineFactoryBean pefbean = new ProcessEngineFactoryBean();
		pefbean.setProcessEngineConfiguration(speConf);
		return pefbean;
 
	}
 
	@Bean
	public RepositoryService repositoryService(ProcessEngineFactoryBean pefb) throws Exception{
		return pefb.getObject().getRepositoryService();
	}
 
	@Bean
	public RuntimeService runtimeService(ProcessEngineFactoryBean pefb) throws Exception {
		return pefb.getObject().getRuntimeService();
	}
 
	@Bean
	public HistoryService historyService(ProcessEngineFactoryBean pefb) throws Exception {
		return pefb.getObject().getHistoryService();
	}
 
	@Bean
	public ManagementService managementService(ProcessEngineFactoryBean pefb) throws Exception {
		return pefb.getObject().getManagementService();
	}
 
	@Bean
	public IdentityService identityService(ProcessEngineFactoryBean pefb) throws Exception {
		return pefb.getObject().getIdentityService();
	}
 
	@Bean
	public FormService formService(ProcessEngineFactoryBean pefb) throws Exception {
		return pefb.getObject().getFormService();
	}
 
	@Bean
	public TaskService taskService(ProcessEngineFactoryBean pefb) throws Exception {
		return pefb.getObject().getTaskService();
	}
}