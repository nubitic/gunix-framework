package mx.com.gunix.framework.josso.selfservices.password;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import mx.com.gunix.framework.RedisTemplate;
import mx.com.gunix.framework.josso.selfservices.password.lostpassword.SerializableLostPasswordProcessState;
import mx.com.gunix.framework.josso.selfservices.password.lostpassword.SerializableStateSimpleLostPasswordProcess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.SSOException;
import org.josso.selfservices.BaseProcessState;
import org.josso.selfservices.ProcessRequest;
import org.josso.selfservices.ProcessResponse;
import org.josso.selfservices.ProcessState;
import org.josso.selfservices.annotations.Action;
import org.josso.selfservices.annotations.Extension;
import org.josso.selfservices.password.PasswordManagementException;
import org.josso.selfservices.password.PasswordManagementProcess;
import org.josso.selfservices.password.PasswordManagementServiceImpl;
import org.josso.util.id.IdGenerator;
import org.springframework.beans.factory.InitializingBean;

/**
 * @org.apache.xbean.XBean element="password-manager"
 */
public class PasswordManagementServiceGunixImpl extends PasswordManagementServiceImpl implements InitializingBean {

	private static final Log log = LogFactory.getLog(PasswordManagementServiceGunixImpl.class);
	private static final String REDIS_KEY = "JPMPIRKP";
	private static final String REDIS_KEY_PATTERN = REDIS_KEY + "\\.[A-Z0-9]+";
	private RedisTemplate<SerializableLostPasswordProcessState> runningProcesses;
	private SerializableStateSimpleLostPasswordProcess prototype;
	private Map<Field, Object> extensions = new ConcurrentHashMap<Field, Object>();
	private List<String> loadedExtensions = new Vector<String>();

	public ProcessResponse startProcess(String name) throws SSOException {
		String id = new StringBuilder(REDIS_KEY).append(".").append(getProcessIdGenerator().generateId()).toString();

		// Create a new process based on the received prototype
		SerializableStateSimpleLostPasswordProcess p = (SerializableStateSimpleLostPasswordProcess) prototype.createNewProcess(id);
		ProcessResponse r = p.start();
		((BaseProcessState) p.getState()).setNextStep(r.getNextStep());
		runningProcesses.set(p.getProcessId(), (SerializableLostPasswordProcessState) p.getState(), p.getMaxTimeToLive() + 60000, TimeUnit.MILLISECONDS);
		return r;
	}

	public ProcessResponse handleRequest(ProcessRequest request) throws PasswordManagementException {
		String processId = request.getProcessId();
		String actionName = null;

		if (log.isDebugEnabled())
			log.debug("Handling request for process [" + processId + "]");

		try {
			SerializableStateSimpleLostPasswordProcess p = init(prototype.createFrom(runningProcesses.get(processId), processId));
			if (p == null)
				throw new PasswordManagementException("No such process " + processId);

			String nextStep = p.getState().getNextStep();

			if (log.isDebugEnabled())
				log.debug("Handling request for process [" + processId + "]");

			Method[] methods = p.getClass().getMethods();
			for (Method method : methods) {
				if (log.isDebugEnabled())
					log.debug("Processing method : " + method.getName());

				if (!method.isAnnotationPresent(Action.class))
					continue;

				Action action = method.getAnnotation(Action.class);

				if (log.isDebugEnabled())
					log.debug("Processing method annotation : " + action);

				for (String actionStep : action.fromSteps()) {

					if (log.isDebugEnabled())
						log.debug("Processing annotation step : " + actionStep);

					if (actionStep.equals(nextStep)) {
						actionName = method.getName();

						if (log.isDebugEnabled())
							log.debug("Dispatching request from step " + nextStep + " to process [" + processId + "] action " + actionName);

						// Store response next step in process state :
						ProcessResponse r = (ProcessResponse) method.invoke(p, request);
						((BaseProcessState) p.getState()).setNextStep(r.getNextStep());
						updateProcessState((SerializableLostPasswordProcessState) p.getState());
						return r;
					}
				}

			}
			throw new PasswordManagementException("Step [" + nextStep + "] not supported by process");
		} catch (InvocationTargetException e) {
			throw new PasswordManagementException("Cannot invoke process action [" + actionName + "] : " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new PasswordManagementException("Cannot invoke process action [" + actionName + "] : " + e.getMessage(), e);
		}
	}

	public RedisTemplate<SerializableLostPasswordProcessState> getRunningProcesses() {
		return runningProcesses;
	}

	/**
	 * @org.apache.xbean.Property alias="password-management-running-processes" nestedType="mx.com.gunix.framework.josso.selfservices.password.lostpassword.SerializableLostPasswordProcessState"
	 *
	 */
	public void setRunningProcesses(RedisTemplate<SerializableLostPasswordProcessState> runningProcesses) {
		this.runningProcesses = runningProcesses;
	}

	public ProcessState getProcessState(String processId) {
		return this.runningProcesses.get(processId);
	}

	/**
	 * @org.apache.xbean.Property alias="process-id-generator"
	 *
	 */
	public void setProcessIdGenerator(IdGenerator idGen) {
		super.setProcessIdGenerator(idGen);
	}

	/**
	 * @org.apache.xbean.Property alias="processes" nestedType="org.josso.selfservices.passwdmanagement.PasswordManagementProcess"
	 *
	 * @param prototypeProcesses
	 */
	public void setPrototypeProcesses(Collection<PasswordManagementProcess> prototypeProcesses) {
		super.setPrototypeProcesses(prototypeProcesses);
	}

	public ProcessRequest createRequest(String processId) throws PasswordManagementException {
		SerializableStateSimpleLostPasswordProcess p = init(prototype.createFrom(this.runningProcesses.get(processId), processId));
		if (p == null || !p.isRunning())
			throw new PasswordManagementException("Invalid proces ID : " + processId);

		return p.createRequest();
	}

	public void checkPendingProcesses() {
		try {
			long now = System.currentTimeMillis();

			for (String processId : runningProcesses.keys(REDIS_KEY_PATTERN)) {
				SerializableStateSimpleLostPasswordProcess process = init(prototype.createFrom(runningProcesses.get(processId), processId));
				try {
					List<PasswordManagementProcess> toRemove = new ArrayList<PasswordManagementProcess>();

					// Ignore valid assertions, they have not expired yet.
					if (!process.isRunning() || process.getCreationTime() - now > process.getMaxTimeToLive()) {
						toRemove.add(process);
						// /registry.unregisterToken(securityDomainName, TOKEN_TYPE, process.getId());
						if (log.isDebugEnabled())
							log.debug("[checkPendingProcesses()] Process expired : " + process.getProcessId());
					}
					for (PasswordManagementProcess passwordManagementProcess : toRemove) {
						try {
							passwordManagementProcess.stop();
						} catch (Exception e) {
							log.debug(e.getMessage(), e);
						}
						runningProcesses.delete(passwordManagementProcess.getProcessId());
					}
				} catch (Exception e) {
					log.warn("Can't remove process " + e.getMessage() != null ? e.getMessage() : e.toString(), e);
				}
			}
		} catch (Exception e) {
			log.error("Cannot check pending processes! " + e.getMessage(), e);
		}
	}

	private SerializableStateSimpleLostPasswordProcess init(SerializableStateSimpleLostPasswordProcess createFrom) {
		for (Field f : extensions.keySet()) {
			try {
				f.set(createFrom, extensions.get(f));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				log.error(e.getMessage(), e);
			}
		}
		return createFrom;
	}

	public void register(String processId, String name, Object extension) {
		if (!loadedExtensions.contains(name)) {
			if (log.isDebugEnabled())
				log.debug("Registering " + name);

			Class<?> clazz = SerializableStateSimpleLostPasswordProcess.class;

			while (clazz != null) {
				Field[] fields = clazz.getDeclaredFields();
				for (Field field : fields) {

					if (log.isDebugEnabled())
						log.debug("Checking field : " + field.getName());

					if (field.isAnnotationPresent(Extension.class)) {

						Extension ex = field.getAnnotation(Extension.class);

						if (ex.value().equals(name)) {
							log.debug("Storing extension : " + name);
							// Make field accessible ...
							if (!field.isAccessible()) {
								field.setAccessible(true);
							}
							extensions.put(field, extension);
							loadedExtensions.add(name);
							return;
						}
					}
				}
				clazz = clazz.getSuperclass();
			}
		}
	}

	public void updateProcessState(SerializableLostPasswordProcessState state) {
		runningProcesses.set(state.getProcessId(), state, state.getMaxTimeToLive() + 60000, TimeUnit.MILLISECONDS);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		prototype = (SerializableStateSimpleLostPasswordProcess) getPrototypeProcesses().iterator().next();
	}
}
