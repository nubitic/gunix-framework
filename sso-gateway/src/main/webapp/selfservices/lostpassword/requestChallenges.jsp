<%@ page contentType="text/html; charset=UTF-8" language="java"%>
<%@ taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean"%>
<html:errors />

<div class="v-caption" id="gwt-uid-19" for="gwt-uid-20">
	<img class="v-icon" src="<%=request.getContextPath()%>/resources/img/1454464590_change_password.png"><span class="v-captiontext"><strong><bean:message key="sso.title.lostPassword" /></strong></span>
</div>
<div class="v-captiontext" id="gwt-uid-19" for="gwt-uid-20">
	<span class="v-text">
		<p>
			<bean:message key="sso.text.lostPassword" />
		</p>
	</span>
</div>
<html:form action="/selfservices/lostpassword/processChallenges" focus="ID_USUARIO">
	<div class="v-formlayout v-layout v-widget v-has-width" id="gwt-uid-20" aria-labelledby="gwt-uid-19" style="width: 100%;">
		<table cellpadding="0" cellspacing="0" role="presentation" class="v-formlayout-spacing">
			<colgroup>
				<col>
			</colgroup>
			<tbody>
				<tr class="v-formlayout-row v-formlayout-firstrow">
					<td class="v-formlayout-captioncell">
						<div class="v-caption v-caption-hasdescription">
							<span id="gwt-uid-21" for="gwt-uid-22"><bean:message key="sso.label.email" /></span>
						</div>
					</td>
					<td class="v-formlayout-errorcell"><div class="v-formlayout-error-indicator"></div></td>
					<td class="v-formlayout-contentcell"><html:text styleClass="v-textfield v-widget" property="ID_USUARIO" /></td>
				</tr>
				<tr class="v-formlayout-row">
					<td class="v-formlayout-captioncell"><div class="gwt-HTML"></div></td>
					<td class="v-formlayout-errorcell"><div class="v-formlayout-error-indicator"></div></td>
					<td class="v-formlayout-contentcell">
						<div tabindex="0" role="button" class="v-button v-widget" onclick="document.forms[0].submit();">
							<span class="v-button-wrap"><span class="v-button-caption"><bean:message key="sso.button.resetPassword" /></span></span>
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</div>
</html:form>
<div class="v-captiontext" id="gwt-uid-19" for="gwt-uid-20">
	<span class="v-text">
		<p>
			<bean:message key="sso.text.buttonOnlyOnce" />
		</p>
	</span>
</div>