package mx.com.gunix.framework.josso.selfservices.password.lostpassword;

import org.josso.gateway.SSOException;
import org.josso.selfservices.ProcessResponse;
import org.josso.selfservices.ProcessState;
import org.josso.selfservices.password.PasswordManagementProcess;
import org.josso.selfservices.password.lostpassword.SimpleLostPasswordProcess;

/**
 * @org.apache.xbean.XBean element="lostpassword-process"
 */
public class SerializableStateSimpleLostPasswordProcess extends SimpleLostPasswordProcess {
	private String name;
	private long creationTime;
	private int maxTimeToLive;
	private boolean running;
	private String challengeId;
	private String challengeText;
	private SerializableLostPasswordProcessState state;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		super.setName(name);
		this.name = name;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public int getMaxTimeToLive() {
		return maxTimeToLive;
	}

	public void setMaxTimeToLive(int maxTimeToLive) {
		super.setMaxTimeToLive(maxTimeToLive);
		this.maxTimeToLive = maxTimeToLive;
	}

	public boolean isRunning() {
		return running;
	}

	public String getChallengeId() {
		return challengeId;
	}

	public void setChallengeId(String challengeId) {
		super.setChallengeId(challengeId);
		this.challengeId = challengeId;
	}

	public String getChallengeText() {
		return challengeText;
	}

	public void setChallengeText(String challengeText) {
		super.setChallengeText(challengeText);
		this.challengeText = challengeText;
	}

	@Override
	protected ProcessState doMakeState(String id) {
		return new SerializableLostPasswordProcessState(id);
	}

	@Override
	public PasswordManagementProcess createNewProcess(String id) throws SSOException {
		SerializableStateSimpleLostPasswordProcess newProcess = (SerializableStateSimpleLostPasswordProcess) super.createNewProcess(id);
		newProcess.state = (SerializableLostPasswordProcessState) newProcess.getState();
		newProcess.challengeId = this.challengeId;
		newProcess.challengeText = this.challengeText;
		newProcess.name = this.name;
		newProcess.state.setChallengeId(newProcess.challengeId);
		newProcess.state.setChallengeText(newProcess.challengeText);
		newProcess.state.setName(newProcess.name);
		newProcess.state.setCreationTime(newProcess.creationTime);
		newProcess.state.setMaxTimeToLive(newProcess.maxTimeToLive);
		newProcess.state.setRunning(newProcess.running);
		return newProcess;
	}

	@Override
	public ProcessState getState() {
		return state == null ? super.getState() : state;
	}

	public SerializableStateSimpleLostPasswordProcess createFrom(SerializableLostPasswordProcessState state, String processId) {
		SerializableStateSimpleLostPasswordProcess process;
		try {
			process = (SerializableStateSimpleLostPasswordProcess) createNewProcess(processId);
			process.setChallengeId(state.getChallengeId());
			process.setChallengeText(state.getChallengeId());
			process.setMaxTimeToLive(state.getMaxTimeToLive());
			process.setName(state.getName());
			process.running = state.isRunning();
			process.creationTime = state.getCreationTime();
			process.state = state;
		} catch (SSOException e) {
			throw new RuntimeException(e);
		}
		return process;
	}

	public ProcessResponse start() {
		ProcessResponse response = super.start();
		this.running = super.isRunning();
		this.state.setRunning(this.running);
		this.state.setCreationTime(super.getCreationTime());
		return response;
	}

	@Override
	public String getProcessId() {
		return state.getProcessId();
	}

	public void stop() {
		super.stop();
		this.running = false;
		this.state.setRunning(this.running);
	}
}
