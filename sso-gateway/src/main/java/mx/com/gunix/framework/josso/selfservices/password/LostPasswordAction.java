package mx.com.gunix.framework.josso.selfservices.password;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import mx.com.gunix.framework.josso.selfservices.password.lostpassword.SerializableLostPasswordProcessState;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.josso.gateway.SSOContext;
import org.josso.selfservices.ChallengeResponseCredential;
import org.josso.selfservices.password.lostpassword.LostPasswordProcessState;

public class LostPasswordAction extends org.josso.selfservices.password.LostPasswordAction {
	private final static String REQUEST_SCHEME = System.getenv("JOSSO_LOST_PASSWORD_RESET_URL_SCHEME");

	@Override
	public ActionForward execute(ActionMapping arg0, ActionForm arg1, HttpServletRequest arg2, HttpServletResponse arg3) throws Exception {
		return super.execute(arg0, arg1, new HttpServletRequestWrapper(arg2) {
			@Override
			public String getScheme() {
				return REQUEST_SCHEME != null && !REQUEST_SCHEME.equals("") ? REQUEST_SCHEME : super.getScheme();
			}
		}, arg3);
	}

	@Override
	protected ChallengeResponseCredential[] fillChallengeResponses(LostPasswordProcessState state, ActionForm form, HttpServletRequest request) {
		SSOContext ctx = SSOContext.getCurrent();
		PasswordManagementServiceGunixImpl pwdService = (PasswordManagementServiceGunixImpl) ctx.getSecurityDomain().getPasswordManager();
		ChallengeResponseCredential[] crc = super.fillChallengeResponses(state, form, request);
		pwdService.updateProcessState((SerializableLostPasswordProcessState) state);
		return crc;
	}

}
