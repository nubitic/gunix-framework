package mx.com.gunix.framework.openlrules.activiti.spring;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;

import org.activiti.engine.DynamicBpmnService;
import org.activiti.engine.EngineServices;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.deploy.DefaultDeploymentCache;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.log4j.Logger;
import org.openl.rules.activiti.ResourceCompileException;
import org.openl.rules.activiti.spring.OpenLRulesHelper;
import org.openl.rules.activiti.spring.result.ResultValue;
import org.openl.rules.activiti.util.ResourceUtils;
import org.openl.rules.project.instantiation.ProjectEngineFactory;
import org.openl.rules.project.instantiation.SimpleProjectEngineFactory;
import org.openl.rules.project.instantiation.SimpleProjectEngineFactory.SimpleProjectEngineFactoryBuilder;
import org.openl.rules.project.model.RulesDeploy;
import org.openl.util.FileUtils;
import org.openl.util.IOUtils;
import org.openl.util.ZipUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;

import mx.com.gunix.framework.documents.DocumentService;
import mx.com.gunix.framework.documents.domain.Documento;
import mx.com.gunix.framework.service.GetterService;

public class OpenLEngine extends org.openl.rules.activiti.spring.OpenLEngine implements ApplicationContextAware {
	private static final Logger log = Logger.getLogger(OpenLEngine.class);
	private ApplicationContext applicationContext;
	private DocumentService ds;
	private static Field oLRHCacheField;
	private static Field oLRHcacheInstance;
	
	@Autowired
	@Lazy
	RepositoryService repositoryService;

	@Autowired
	@Lazy
	SpringProcessEngineConfiguration spec;

	static {
		try {
			oLRHCacheField = OpenLRulesHelper.class.getDeclaredField("cache");
			oLRHCacheField.setAccessible(true);
			oLRHcacheInstance = OpenLRulesHelper.class.getDeclaredField("cacheInstance");
			oLRHcacheInstance.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	public ResultValue executeWithinProcessKey(String processKeyHolder, String resource, String methodName, Object... args) throws Exception {
		String processDefinitionId = null;
		if (processKeyHolder != null) {
			ProcessDefinition pdef = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processKeyHolder).active().latestVersion().singleResult();
			if (pdef == null) {
				throw new IllegalArgumentException("El proceso con key " + processKeyHolder + " no existe");
			} else {
				processDefinitionId = pdef.getId();
			}
		}
		if (!Context.isExecutionContextActive()) {
			ExecutionEntity ejec = new ExecutionEntity() {
				private static final long serialVersionUID = 1L;

				@Override
				public EngineServices getEngineServices() {
					// TODO Auto-generated method stub
					return new EngineServices() {

						@Override
						public RepositoryService getRepositoryService() {
							return repositoryService;
						}

						@Override
						public RuntimeService getRuntimeService() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public FormService getFormService() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public TaskService getTaskService() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public HistoryService getHistoryService() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public IdentityService getIdentityService() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public ManagementService getManagementService() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public DynamicBpmnService getDynamicBpmnService() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public ProcessEngineConfiguration getProcessEngineConfiguration() {
							// TODO Auto-generated method stub
							return null;
						}
					};
				}

			};
			String[] pidPdidArr = GetterService.pidPdid.get();
			ejec.setProcessDefinitionId(processDefinitionId == null ? pidPdidArr[GetterService.PROCESS_DEFINITIONID] : processDefinitionId);
			ejec.setId(pidPdidArr[GetterService.PROCESS_INSTANCEID]);
			ejec.setProcessInstance(ejec);
			Context.setCommandContext(spec.getCommandContextFactory().createCommandContext(null));
			Context.setProcessEngineConfiguration(spec);
			try {
				return execute(ejec,  resource,  methodName, args);
			} finally {
				Context.removeCommandContext();
				Context.removeProcessEngineConfiguration();
			}
		}else{
			String currProcessDefinitionId = Context.getExecutionContext().getExecution().getProcessDefinitionId();
			try {
				if (processDefinitionId != null) {
					Context.getExecutionContext().getExecution().setProcessDefinitionId(processDefinitionId);
				}
				return execute(Context.getExecutionContext().getExecution(), resource, methodName, args);
			} finally {
				Context.getExecutionContext().getExecution().setProcessDefinitionId(currProcessDefinitionId);
			}
		}
	}

	public ResultValue execute(String resource, String methodName, Object... args) throws Exception {
		return executeWithinProcessKey(null, resource, methodName, args);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ResultValue execute(DelegateExecution execution, String resource, String methodName, Object... args) throws Exception {
		ResultValue rv = null;

		if (Boolean.parseBoolean(com.hunteron.core.Context.SOPORTE_DOCUMENTAL_ENABLED.get())) {
			Map<String, DeploymentCache<ProjectEngineFactory>> cache = (Map<String, DeploymentCache<ProjectEngineFactory>>) oLRHCacheField.get(OpenLRulesHelper.getInstance());
			DeploymentCache<ProjectEngineFactory> depCach = null;
			String processDefinitionId = execution.getProcessDefinitionId();
			RepositoryService repositoryService = execution.getEngineServices().getRepositoryService();
			ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);

			if ((depCach = cache.get(processDefinition.getDeploymentId())) == null || depCach.get(resource) == null) {
				if (ds == null) {
					ds = applicationContext.getBean(DocumentService.class);
				}

				Documento rulesDoc = ds.get("openLRules/" + resource);

				if (rulesDoc != null) {
					// Si lo encontramos en logicaldoc procedemos a inicializar el ambiente
					InputStream inputStream = ds.getContent(rulesDoc.getId());

					final File workspaceFolder = Files.createTempDirectory("openLwksDir").toFile();

					if (resource.endsWith(".zip")) {
						// Unzip
						ZipUtils.extractAll(inputStream, workspaceFolder);
					} else {
						// Copy
						File resourceFile = new File(workspaceFolder, resource);
						FileOutputStream fos = new FileOutputStream(resourceFile);
						IOUtils.copyAndClose(inputStream, fos);
					}

					Runtime.getRuntime().addShutdownHook(new Thread() {
						@Override
						public void run() {
							FileUtils.deleteQuietly(workspaceFolder);
						}
					});

					RulesDeploy rulesDeploy = ResourceUtils.readRulesDeploy(workspaceFolder);

					SimpleProjectEngineFactoryBuilder simpleProjectEngineFactoryBuilder = new SimpleProjectEngineFactoryBuilder().setExecutionMode(true).setProject(workspaceFolder.getCanonicalPath()).setWorkspace(workspaceFolder.getCanonicalPath());
					if (rulesDeploy != null) {
						simpleProjectEngineFactoryBuilder.setProvideRuntimeContext(rulesDeploy.isProvideRuntimeContext());
						if (rulesDeploy.getServiceClass() != null) {
							Class<?> interfaceClass = Thread.currentThread().getContextClassLoader().loadClass(rulesDeploy.getServiceClass());
							simpleProjectEngineFactoryBuilder.setInterfaceClass(interfaceClass);
						}
					}

					SimpleProjectEngineFactory<Object> simpleProjectEngineFactory = simpleProjectEngineFactoryBuilder.build();

					if (depCach == null) {
						depCach = new DefaultDeploymentCache<ProjectEngineFactory>();
						cache.put(processDefinition.getDeploymentId(), depCach);
					}
					depCach.add(resource, simpleProjectEngineFactory);
				}
			}
		}

		rv = doExecute(execution, resource, methodName, args);
		if (log.isDebugEnabled()) {
			Object ans = rv.value();
			String ansStr = ans != null ? ans.getClass().isArray() ? Arrays.toString((Object[]) ans) : ans.toString() : "null";
			log.debug(new StringBuilder(execution.getProcessInstanceId()).append(": ").append(resource).append("/").append(methodName).append("(").append(Arrays.toString(args)).append(")").append("=").append(ansStr).toString());
		}
		return rv;
	}

	private ResultValue doExecute(DelegateExecution execution, String resource, String methodName, Object[] args) throws Exception {
		String processDefinitionId = execution.getProcessDefinitionId();
		RepositoryService repositoryService = execution.getEngineServices().getRepositoryService();
		ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);

		@SuppressWarnings("rawtypes")
		ProjectEngineFactory projectEngineFactory = OpenLRulesHelper.getInstance().get(processDefinition.getDeploymentId(), resource);
		Class<?> interfaceClass = projectEngineFactory.getInterfaceClass();
		Object instance = doGetInstance(processDefinition.getDeploymentId(), resource);
		if (interfaceClass == null) {
			interfaceClass = instance.getClass();
		}

		Object result = org.openl.rules.activiti.spring.OpenLEngine.findAndInvokeMethod(methodName, instance, interfaceClass, args);

		return new ResultValue(result);
	}

	@SuppressWarnings("unchecked")
	private Object doGetInstance(String deploymentId, String resource) throws IllegalArgumentException, IllegalAccessException {
		// First find in cache
		Map<String, DeploymentCache<Object>> cacheInstance = (Map<String, DeploymentCache<Object>>) oLRHcacheInstance.get(OpenLRulesHelper.getInstance());
		DeploymentCache<Object> deploymentCache = cacheInstance.get(deploymentId);
		if (deploymentCache == null) {
			deploymentCache = new DefaultDeploymentCache<Object>();
			cacheInstance.put(deploymentId, deploymentCache);
		}
		Object instance = deploymentCache.get(resource);
		if (instance == null) {
			@SuppressWarnings("rawtypes")
			ProjectEngineFactory projectEngineFactory = OpenLRulesHelper.getInstance().get(deploymentId, resource);
			try {
				instance = projectEngineFactory.newInstance();
			} catch (Exception e) {
				throw new ResourceCompileException("Resource with name '" + resource + "' in deployment with id '" + deploymentId + "' compile was fail!", e);
			}
			deploymentCache.add(resource, instance);
		}
		return instance;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
