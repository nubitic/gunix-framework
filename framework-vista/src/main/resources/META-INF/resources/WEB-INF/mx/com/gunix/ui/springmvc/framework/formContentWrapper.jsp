<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<form:form commandName="${commandName}" id="gunixMainForm" action="#">
	<script type="text/javascript">
		var cCurrentGunixViewPath = "<spring:url value="${cCurrentGunixViewPath}"/>";
		cGunixContextPath = "${gunixContextPath}";
		urlExceptions = [cGunixContextPath + 'uploadFile',
						 cGunixContextPath + 'ajaxFragment',
						 cGunixContextPath + 'startProcess'];
		var cIdAplicacion = "${param.idAplicacion}";
		var cgCommandName = "${commandName}";
	</script>
	<div id="formContent">
		<tiles:insertAttribute name="formContent" ignore="true" />
	</div>
</form:form>