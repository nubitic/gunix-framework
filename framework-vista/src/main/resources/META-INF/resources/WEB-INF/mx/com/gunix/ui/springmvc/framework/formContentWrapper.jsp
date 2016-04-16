<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<form:form commandName="${commandName}" id="gunixMainForm" action="#" data-abide="ajax">
	<tiles:insertAttribute name="formContent" ignore="true" />
</form:form>