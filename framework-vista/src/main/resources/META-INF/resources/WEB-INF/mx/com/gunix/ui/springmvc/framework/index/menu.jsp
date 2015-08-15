<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="index" uri="/framework/index/tags"%>

<sec:authentication property="principal.aplicaciones" var="apps" />
<c:forEach items="${apps}" var="app">
	<c:if test="${app.idAplicacion == param.idAplicacion}">
		<c:forEach items="${app.roles}" var="rol">
			<c:if test="${rol.idRol == param.idRol}">
				<c:forEach items="${rol.modulos}" var="modulo">
					<c:if test="${modulo.idModulo == param.idModulo}">
						<index:menu funciones="${modulo.funciones}" />
					</c:if>
				</c:forEach>
			</c:if>
		</c:forEach>
	</c:if>
</c:forEach>