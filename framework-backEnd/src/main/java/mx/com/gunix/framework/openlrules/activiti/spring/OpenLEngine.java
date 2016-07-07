package mx.com.gunix.framework.openlrules.activiti.spring;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.deploy.DefaultDeploymentCache;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.log4j.Logger;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import mx.com.gunix.framework.documents.DocumentService;
import mx.com.gunix.framework.documents.domain.Documento;

public class OpenLEngine extends org.openl.rules.activiti.spring.OpenLEngine implements ApplicationContextAware {
	private static final Logger log = Logger.getLogger(OpenLEngine.class);
	private ApplicationContext applicationContext;
	private DocumentService ds;
	private static Field oLRHCacheField;

	static {
		try {
			oLRHCacheField = OpenLRulesHelper.class.getDeclaredField("cache");
			oLRHCacheField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ResultValue execute(DelegateExecution execution, String resource, String methodName, Object... args) throws Exception {
		ResultValue rv = null;

		if (Boolean.parseBoolean(System.getenv("LOGICALDOC_ENABLED"))) {
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

					final File workspaceFolder = FileUtils.createTempDirectory();

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

		rv = super.execute(execution, resource, methodName, args);
		if (log.isDebugEnabled()) {
			Object ans = rv.value();
			String ansStr = ans != null ? ans.getClass().isArray() ? Arrays.toString((Object[]) ans) : ans.toString() : "null";
			log.debug(new StringBuilder(execution.getProcessInstanceId()).append(": ").append(resource).append("/").append(methodName).append("(").append(Arrays.toString(args)).append(")").append("=").append(ansStr).toString());
		}
		return rv;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
