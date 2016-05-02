<%@ attribute name="label" required="false" type="java.lang.String" %>
<%@ taglib prefix="gunix" uri="/framework/tags"%>
<%@ tag dynamic-attributes="dynAttrs" %>
<input type="button" value="${label}" onClick="onCompleteTask('${param.idAplicacion}');" <gunix:mapToHtmlAttrString map="${dynAttrs}"/> />