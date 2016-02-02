<%@ page contentType="text/html; charset=UTF-8" language="java"%>
<%@ taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean"%>
<html:errors />

<div class="v-caption" id="gwt-uid-19" for="gwt-uid-20">
	<img class="v-icon" src="<%=request.getContextPath()%>/resources/img/1454464590_change_password.png"><span class="v-captiontext"><strong><bean:message key="sso.title.passwordResetted" /></strong></span>
</div>
<div class="v-captiontext" id="gwt-uid-19" for="gwt-uid-20">
	<span class="v-text">
		<p>
			<bean:message key="sso.text.passwordResetted" />
		</p>
	</span> <span class="v-href-caption"><html:link forward="login">
			<bean:message key="sso.button.login" />
		</html:link>
</div>