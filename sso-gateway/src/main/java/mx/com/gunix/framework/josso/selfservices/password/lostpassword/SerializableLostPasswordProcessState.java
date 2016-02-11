package mx.com.gunix.framework.josso.selfservices.password.lostpassword;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.josso.auth.Credential;
import org.josso.gateway.identity.SSOUser;
import org.josso.selfservices.ChallengeResponseCredential;

public class SerializableLostPasswordProcessState extends LostPasswordProcessStateDefaultConstructor implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private long creationTime;
	private int maxTimeToLive;
	private boolean running;
	private String challengeId;
	private String challengeText;
	private String assertionId;
	private Set<ChallengeResponseCredential> challenges;
	private Credential newPasswordCredential;
	private String passwordConfirmUrl;
	private SSOUser user;
	private Map<String, Object> attributes = new HashMap<String, Object>();
	private String nextStep;
	private String processId;

	public SerializableLostPasswordProcessState(String procesId) {
		super(procesId);
		this.processId = procesId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public int getMaxTimeToLive() {
		return maxTimeToLive;
	}

	public void setMaxTimeToLive(int maxTimeToLive) {
		this.maxTimeToLive = maxTimeToLive;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public String getChallengeId() {
		return challengeId;
	}

	public void setChallengeId(String challengeId) {
		this.challengeId = challengeId;
	}

	public String getChallengeText() {
		return challengeText;
	}

	public void setChallengeText(String challengeText) {
		this.challengeText = challengeText;
	}

	@Override
	public String getAssertionId() {
		return assertionId;
	}

	@Override
	public Set<ChallengeResponseCredential> getChallenges() {
		return challenges;
	}

	@Override
	public Credential getNewPasswordCredential() {
		return newPasswordCredential;
	}

	@Override
	public String getPasswordConfirmUrl() {
		return passwordConfirmUrl;
	}

	@Override
	public SSOUser getUser() {
		return user;
	}

	@Override
	public void setAssertionId(String assertionId) {
		super.setAssertionId(assertionId);
		this.assertionId = assertionId;
	}

	@Override
	public void setChallenges(Set<ChallengeResponseCredential> challenges) {
		super.setChallenges(challenges);
		this.challenges = challenges;
	}

	@Override
	public void setNewPasswordCredential(Credential newPasswordCredential) {
		super.setNewPasswordCredential(newPasswordCredential);
		this.newPasswordCredential = newPasswordCredential;
	}

	@Override
	public void setPasswordConfirmUrl(String passwordConfirmUrl) {
		super.setPasswordConfirmUrl(passwordConfirmUrl);
		this.passwordConfirmUrl = passwordConfirmUrl;
	}

	@Override
	public void setUser(SSOUser user) {
		super.setUser(user);
		this.user = user;
	}

	@Override
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	public String getNextStep() {
		return nextStep;
	}

	@Override
	public String getProcessId() {
		return processId;
	}

	@Override
	public void removeAttribute(String key) {
		super.removeAttribute(key);
		attributes.remove(key);
	}

	@Override
	public void setAttribute(String key, Object value) {
		super.setAttribute(key, value);
		attributes.put(key, value);
	}

	@Override
	public void setNextStep(String nextStep) {
		super.setNextStep(nextStep);
		this.nextStep = nextStep;
	}

}
