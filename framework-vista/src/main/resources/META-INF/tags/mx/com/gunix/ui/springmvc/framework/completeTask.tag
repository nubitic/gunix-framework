<%@ attribute name="label" required="false" type="java.lang.String" %>
<%@ attribute name="beforeComplete" required="false" type="java.lang.String" %>
<%@ taglib prefix="gunix" uri="/framework/tags"%>
<%@ tag dynamic-attributes="dynAttrs" %>
<input type="button" value="${not empty label?label:'Completar Tarea'}" onClick="onCompleteTask('${param.idAplicacion}',${not empty beforeComplete?beforeComplete:'null'}, this);" <gunix:mapToHtmlAttrString map="${dynAttrs}"/> />