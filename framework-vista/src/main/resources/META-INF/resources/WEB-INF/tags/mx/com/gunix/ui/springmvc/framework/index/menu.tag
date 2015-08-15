<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="index" uri="/framework/index/tags"%>
<%@ attribute name="funciones" required="false" type="java.util.List" %>
<%@ attribute name="padre" required="false" type="mx.com.gunix.framework.security.domain.Funcion" %>

<c:choose>
	<c:when test="${not empty funciones}">
		<c:set var="funcionesVar" value="${funciones}" />
	</c:when>
	<c:otherwise>
		<c:set var="funcionesVar" value="${padre.hijas}" />
	</c:otherwise>
</c:choose>


<c:forEach items="${funcionesVar}" var="funcion">
	<c:choose>
	    <c:when test="${not empty funcion.hijas}">
			<h3 title="${funcion.descripcion}">${funcion.titulo}</h3>
			<ul class="toggle">
	    		<index:menu padre="${funcion}"/>	    		
	    	</ul>          
	    </c:when>
	    <c:otherwise>
	        <li class="icn_categories"><a href="#" onclick="onFuncionClick('${funcion.idFuncion}', '${param.idModulo}','${param.idRol}','${param.idAplicacion}', $(this));">${funcion.titulo}</a></li>
	    </c:otherwise>
	</c:choose> 
</c:forEach>