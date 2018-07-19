<%@ attribute name="label" required="false" type="java.lang.String" %>
<%@ attribute name="beforeComplete" required="false" type="java.lang.String" %>
<%@ attribute name="tipo" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="gunix" uri="/framework/tags"%>
<%@ tag dynamic-attributes="dynAttrs" %>
<c:choose><c:when test="${tipo=='link'}"><a href="#"</c:when><c:when test="${empty tipo || tipo=='botón'}"><input type="button" value="${not empty label?label:'Completar Tarea'}"</c:when></c:choose> onClick="onCompleteTask('${param.idAplicacion}',${not empty beforeComplete?beforeComplete:'null'}, this);" <gunix:mapToHtmlAttrString map="${dynAttrs}"/> <c:choose><c:when test="${tipo=='link'}">>${label}</a></c:when><c:when test="${empty tipo || tipo=='botón'}">/></c:when></c:choose>