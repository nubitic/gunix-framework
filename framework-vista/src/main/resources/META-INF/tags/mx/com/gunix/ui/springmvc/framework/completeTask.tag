<%@ attribute name="label" required="false" type="java.lang.String" %>
<%@ attribute name="id" required="false" type="java.lang.String" %>
<input type="button" value="${label}" id="${id}" onClick="onCompleteTask('${param.idAplicacion}');"/>