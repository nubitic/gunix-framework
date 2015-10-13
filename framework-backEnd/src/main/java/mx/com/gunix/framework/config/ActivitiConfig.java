package mx.com.gunix.framework.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.sql.DataSource;

import mx.com.gunix.framework.activiti.FloatType;
import mx.com.gunix.framework.activiti.GunixObjectVariableType;
import mx.com.gunix.framework.activiti.persistence.entity.VariableInstanceEntityManager;
import mx.com.gunix.framework.scheduling.concurrent.ManagedAwareThreadFactory;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.asyncexecutor.ManagedAsyncJobExecutor;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.autodeployment.ResourceParentFolderAutoDeploymentStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableScheduling
public class ActivitiConfig {

	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	@Bean
	public IdGenerator idGenerator() {
		return new StrongUuidGenerator();
	}

	@Bean
	public SpringProcessEngineConfiguration springProcessEngineConfiguration(DataSource dataSource, PlatformTransactionManager transactionManager) throws IOException {
		SpringProcessEngineConfiguration speConf = new SpringProcessEngineConfiguration();
		speConf.setDataSource(dataSource);
		speConf.setDatabaseType(ProcessEngineConfigurationImpl.DATABASE_TYPE_POSTGRES);
		speConf.setTransactionManager(transactionManager);
		speConf.setTablePrefixIsSchema(true);
		speConf.setDatabaseTablePrefix("activiti.");
		speConf.setDatabaseSchema("activiti");
		speConf.setDatabaseSchemaUpdate("false");
		speConf.setDbIdentityUsed(false);
		speConf.setBatchSizeTasks(1000);
		speConf.setEnableSafeBpmnXml(true);

		if (Boolean.valueOf(System.getenv("ACTIVITI_MASTER"))) {
			speConf.setAsyncExecutor(asyncExecutor());
			speConf.setAsyncExecutorEnabled(true);
			speConf.setAsyncExecutorActivate(true);
		}

		speConf.setIdGenerator(idGenerator());
		speConf.setDeploymentMode(ResourceParentFolderAutoDeploymentStrategy.DEPLOYMENT_MODE);
		speConf.setDeploymentResources(resourcePatternResolver.getResources("classpath*:/mx/com/gunix/procesos/**/*.bpmn"));

		VariableInstanceEntityManager vim = variableInstanceEntityManager();

		List<SessionFactory> vimList = new ArrayList<SessionFactory>();
		vimList.add(vim);
		speConf.setCustomSessionFactories(vimList);

		List<VariableType> varTypes = new ArrayList<VariableType>();
		varTypes.add(gunixObjectVariableType());
		varTypes.add(new FloatType());
		speConf.setCustomPreVariableTypes(varTypes);// Se establece primero en
													// la lista que se usa para
													// el guardado
		speConf.setCustomPostVariableTypes(varTypes);// Se establece como el
														// último en asignarse
														// en el Mapa que se usa
														// para la recuperación

		return speConf;
	}

	@Bean
	public AsyncExecutor asyncExecutor() {
		ManagedAsyncJobExecutor majex = new ManagedAsyncJobExecutor();
		majex.setCorePoolSize(5);
		majex.setMaxPoolSize(100);
		majex.setKeepAliveTime(5000);
		majex.setQueueSize(2);
		majex.setMaxTimerJobsPerAcquisition(75);
		majex.setMaxAsyncJobsDuePerAcquisition(75);
		majex.setDefaultTimerJobAcquireWaitTimeInMillis(60000);
		majex.setDefaultAsyncJobAcquireWaitTimeInMillis(2000);
		majex.setTimerLockTimeInMillis(2147483647);
		majex.setAsyncJobLockTimeInMillis(2147483647);
		majex.setThreadFactory(threadFactory());
		return majex;
	}

	@Bean
	public ManagedThreadFactory threadFactory() {
		return new ManagedAwareThreadFactory();
	}

	@Bean
	public GunixObjectVariableType gunixObjectVariableType() {
		return new GunixObjectVariableType();
	}

	@Bean
	public ProcessEngineFactoryBean processEngineFactoryBean(SpringProcessEngineConfiguration speConf) throws Exception {
		ProcessEngineFactoryBean pefbean = new ProcessEngineFactoryBean();
		pefbean.setProcessEngineConfiguration(speConf);
		return pefbean;

	}

	@Bean
	public VariableInstanceEntityManager variableInstanceEntityManager() {
		return new VariableInstanceEntityManager();
	}

	@Bean
	public RepositoryService repositoryService(ProcessEngineFactoryBean pefb) throws Exception {
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
