package mx.com.gunix.framework.josso.selfservices.password.lostpassword;

import org.josso.selfservices.password.lostpassword.LostPasswordProcessState;

public class LostPasswordProcessStateDefaultConstructor extends LostPasswordProcessState {

	public LostPasswordProcessStateDefaultConstructor() {
		super("");
	}

	public LostPasswordProcessStateDefaultConstructor(String procesId) {
		super(procesId);
	}

}