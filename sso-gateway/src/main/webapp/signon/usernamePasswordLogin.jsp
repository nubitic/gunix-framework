<%@ page contentType="text/html; charset=UTF-8" language="java"%>
<%@ taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean"%>


<div class="v-caption" id="gwt-uid-19" for="gwt-uid-20">
	<img class="v-icon" src="<%=request.getContextPath()%>/resources/img/1440816106_window.png"><span class="v-captiontext"><strong><bean:message key="sso.title.userLogin" /></strong></span>
</div>
<div class="v-captiontext" id="gwt-uid-19" for="gwt-uid-20">
	<span class="v-text">
		<p>
			<bean:message key="sso.text.userLogin" />
		</p>
	</span>
</div>
<html:form action="/signon/usernamePasswordLogin" focus="josso_username">
	<html:hidden property="josso_cmd" value="login" />
	<html:hidden property="josso_back_to" />
	<div class="v-formlayout v-layout v-widget v-has-width" id="gwt-uid-20" aria-labelledby="gwt-uid-19" style="width: 100%;">
		<table cellpadding="0" cellspacing="0" role="presentation" class="v-formlayout-spacing">
			<colgroup>
				<col>
			</colgroup>
			<tbody>
				<tr class="v-formlayout-row v-formlayout-firstrow">
					<td class="v-formlayout-captioncell"><div class="v-caption v-caption-hasdescription">
							<span id="gwt-uid-21" for="gwt-uid-22"><bean:message key="sso.label.username" /></span>
						</div></td>
					<td class="v-formlayout-errorcell"><div class="v-formlayout-error-indicator"></div></td>
					<td class="v-formlayout-contentcell"><html:text styleClass="v-textfield v-widget" property="josso_username" /></td>
				</tr>
				<tr class="v-formlayout-row">
					<td class="v-formlayout-captioncell"><div class="v-caption v-caption-hasdescription">
							<span id="gwt-uid-23" for="gwt-uid-24"><bean:message key="sso.label.password" /></span>
						</div></td>
					<td class="v-formlayout-errorcell"><div class="v-formlayout-error-indicator"></div></td>
					<td class="v-formlayout-contentcell"><html:password styleClass="v-textfield v-widget" property="josso_password" /></td>
				</tr>
				<tr class="v-formlayout-row v-formlayout-firstrow">
					<td class="v-formlayout-captioncell"><div class="v-caption v-caption-hasdescription">
							<span id="gwt-uid-21" for="gwt-uid-22"><bean:message key="sso.label.rememberme" /></span>
						</div></td>
					<td class="v-formlayout-errorcell"><div class="v-formlayout-error-indicator"></div></td>
					<td class="v-formlayout-contentcell"><html:checkbox property="josso_rememberme" styleClass="v-checkbox v-widget" /></td>
				</tr>
				<tr class="v-formlayout-row">
					<td class="v-formlayout-captioncell"><div class="gwt-HTML"></div></td>
					<td class="v-formlayout-errorcell"><div class="v-formlayout-error-indicator"></div></td>
					<td class="v-formlayout-contentcell">
						<div tabindex="0" role="button" class="v-button v-widget" onclick="document.forms[0].submit();">
							<span class="v-button-wrap"><span class="v-button-caption"><bean:message key="sso.button.login" /></span></span>
						</div>
					</td>
				</tr>
				<tr class="v-formlayout-row">
					<td class="v-formlayout-captioncell"><div class="gwt-HTML"></div></td>
					<td class="v-formlayout-errorcell"><div class="v-formlayout-error-indicator"></div></td>
					<td class="v-formlayout-contentcell">
					<span class="v-href-caption"><a href="<%=request.getContextPath()%>/selfservices/lostpassword/lostPassword.do?josso_cmd=lostPwd"><bean:message key="sso.label.forgotPassword" /></a></span></td>
				</tr>
			</tbody>
		</table>
	</div>
</html:form>