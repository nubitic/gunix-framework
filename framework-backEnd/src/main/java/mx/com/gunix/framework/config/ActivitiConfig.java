package mx.com.gunix.framework.config;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import javax.sql.DataSource;

import mx.com.gunix.framework.activiti.ExecuteAsyncSecuredRunnable;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.bpmn.data.ItemInstance;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.el.ReadOnlyMapELResolver;
import org.activiti.engine.impl.el.VariableScopeElResolver;
import org.activiti.engine.impl.javax.el.ArrayELResolver;
import org.activiti.engine.impl.javax.el.BeanELResolver;
import org.activiti.engine.impl.javax.el.CompositeELResolver;
import org.activiti.engine.impl.javax.el.DynamicBeanPropertyELResolver;
import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELResolver;
import org.activiti.engine.impl.javax.el.ListELResolver;
import org.activiti.engine.impl.javax.el.MapELResolver;
import org.activiti.engine.impl.javax.el.MethodNotFoundException;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.spring.ApplicationContextElResolver;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringExpressionManager;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.SpringRejectedJobsHandler;
import org.activiti.spring.autodeployment.AutoDeploymentStrategy;
import org.activiti.spring.autodeployment.ResourceParentFolderAutoDeploymentStrategy;
import org.openl.rules.activiti.spring.OpenLResourcesHandleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ReflectionUtils;

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
		SpringProcessEngineConfiguration speConf = new SpringProcessEngineConfiguration() {
			private AutoDeploymentStrategy autoDS = new ResourceParentFolderAutoDeploymentStrategy() {
				@Override
				protected String determineResourceName(final Resource resource) {
					String resourceName = null;

					if (resource instanceof ContextResource) {
						resourceName = ((ContextResource) resource).getPathWithinContext();

					} else if (resource instanceof ByteArrayResource) {
						resourceName = resource.getDescription();

					} else {
						try {
							resourceName = resource.getFile().getName();
						} catch (IOException e) {
							resourceName = resource.getFilename();
						}
					}
					return resourceName;
				}
			};

			@Override
			protected AutoDeploymentStrategy getAutoDeploymentStrategy(final String mode) {
				return autoDS;
			}
		};
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
		
		Resource[] appPackedBPMNResources = resourcePatternResolver.getResources("classpath*:/mx/com/gunix/procesos/**/*.*");
		Resource[] resources = new Resource[(appPackedBPMNResources != null ? appPackedBPMNResources.length : 0)];
		
		if (appPackedBPMNResources != null && appPackedBPMNResources.length > 0) {
			System.arraycopy(appPackedBPMNResources, 0, resources, 0, appPackedBPMNResources.length);
		}
		
		if (Boolean.valueOf(System.getenv("STANDALONE_APP"))) {
			Resource[] adminAppResources = resourcePatternResolver.getResources("classpath*:/mx/com/gunix/adminapp/procesos/*.bpmn");

			if (resources != null && resources.length > 0) {
				Resource[] finalResources = new Resource[resources.length + adminAppResources.length];
				System.arraycopy(adminAppResources, 0, finalResources, 0, adminAppResources.length);
				System.arraycopy(resources, 0, finalResources, adminAppResources.length, resources.length);
				resources = finalResources;
			} else {
				resources = adminAppResources;
			}
		}
		
		speConf.setDeploymentResources(resources);

		List<ActivitiEventListener> evntListners =  new ArrayList<ActivitiEventListener>();
		evntListners.add(new OpenLResourcesHandleListener());
		speConf.setEventListeners(evntListners);

		return speConf;
	}

	@Bean
	public AsyncExecutor asyncExecutor() {
		SpringAsyncExecutor sajex = new SpringAsyncSecuredExecutor(taskExecutor());
		sajex.setCorePoolSize(5);
		sajex.setMaxPoolSize(100);
		sajex.setKeepAliveTime(5000);
		sajex.setQueueSize(2);
		sajex.setMaxTimerJobsPerAcquisition(75);
		sajex.setMaxAsyncJobsDuePerAcquisition(75);
		sajex.setDefaultTimerJobAcquireWaitTimeInMillis(60000);
		sajex.setDefaultAsyncJobAcquireWaitTimeInMillis(2000);
		sajex.setTimerLockTimeInMillis(2147483647);
		sajex.setAsyncJobLockTimeInMillis(2147483647);
		return sajex;
	}

	@Bean
	public TaskExecutor taskExecutor() {
		return new SimpleAsyncTaskExecutor();
	}

	@Bean
	public ProcessEngineFactoryBean processEngineFactoryBean(SpringProcessEngineConfiguration speConf) throws Exception {
		ProcessEngineFactoryBean pefbean = new ProcessEngineFactoryBean() {

			protected void configureExpressionManager() {
				if (processEngineConfiguration.getExpressionManager() == null && applicationContext != null) {
					processEngineConfiguration.setExpressionManager(new SpringExpressionManager(applicationContext, processEngineConfiguration.getBeans()) {
						@Override
						protected ELResolver createElResolver(VariableScope variableScope) {
							CompositeELResolver elResolver = new CompositeELResolver();
							elResolver.add(new VariableScopeElResolver(variableScope));

							if (beans != null) {
								// Only expose limited set of beans in expressions
								elResolver.add(new ReadOnlyMapELResolver(beans));
							} else {
								// Expose full application-context in expressions
								elResolver.add(new ApplicationContextElResolver(applicationContext));
							}

							elResolver.add(new ArrayELResolver());
							elResolver.add(new ListELResolver());
							elResolver.add(new MapELResolver());
							elResolver.add(new DynamicBeanPropertyELResolver(ItemInstance.class, "getFieldValue", "setFieldValue")); // TODO: needs verification
							elResolver.add(new BeanELResolver() {
								@Override
								public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
									try {
										return super.invoke(context, base, method, paramTypes, params);
									} catch (MethodNotFoundException mnfe) {
										if (AopUtils.isAopProxy(base)) {
											Class<?> targetClass = AopUtils.getTargetClass(base);
											Method m = null;
											if (paramTypes != null) {
												m = ReflectionUtils.findMethod(targetClass, method.toString(), paramTypes);
											} else {
												String name = method.toString();
												int paramCount = params == null ? 0 : params.length;
												for (Method mf : targetClass.getMethods()) {
													if (mf.getName().equals(name)) {
														int formalParamCount = mf.getParameterTypes().length;
														if ((mf.isVarArgs() && paramCount >= formalParamCount - 1) || (paramCount == formalParamCount)) {
															m = mf;
															break;
														}
													}
												}
											}

											try {
												Object result = Proxy.getInvocationHandler(base).invoke(base, m, params);
												context.setPropertyResolved(true);
												return result;
											} catch (Throwable e) {
												throw new MethodNotFoundException("Cannot find method " + method + " with " + params.length + " parameters in " + base.getClass(), e);
											}
										} else {
											throw mnfe;
										}
									}
								}

							});
							return elResolver;
						}
					});
				}
			}
		};
		pefbean.setProcessEngineConfiguration(speConf);
		return pefbean;
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
	
	static final class SpringAsyncSecuredExecutor extends SpringAsyncExecutor implements SpringRejectedJobsHandler {
		private static Logger log = LoggerFactory.getLogger(SpringAsyncSecuredExecutor.class);

		@Autowired
		@Lazy
		RuntimeService rs;
		
		public SpringAsyncSecuredExecutor(TaskExecutor taskExecutor) {
			super(taskExecutor, null);
			setRejectedJobsHandler(this);
		}

		@Override
		public void executeAsyncJob(JobEntity job) {
			try {
				taskExecutor.execute(new ExecuteAsyncSecuredRunnable(job, commandExecutor, rs));
			} catch (RejectedExecutionException e) {
				rejectedJobsHandler.jobRejected(this, job);
			}
		}

		@Override
		public void jobRejected(AsyncExecutor asyncExecutor, JobEntity job) {
			try {
				// execute rejected work in caller thread (potentially blocking job acquisition)
				new ExecuteAsyncSecuredRunnable(job, asyncExecutor.getCommandExecutor(), rs).run();
			} catch (Exception e) {
				log.error("Failed to execute rejected job " + job.getId(), e);
			}
		}
	}
}
