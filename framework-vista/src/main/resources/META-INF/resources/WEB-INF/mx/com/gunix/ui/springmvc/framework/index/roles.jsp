<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<sec:authentication property="principal.aplicaciones" var="apps" />
<label for="rol">Rol</label>
<select name="rol" onchange="onRolChange(this.value,'${param.idAplicacion}');">
	<option value="">Selecciona un Rol</option>
	<c:forEach items="${apps}" var="app">
		<c:if test="${app.idAplicacion == param.idAplicacion}">
			<c:forEach items="${app.roles}" var="rol">
				<option value="${rol.idRol}">${rol.descripcion}</option>
			</c:forEach>
		</c:if>
	</c:forEach>
</select>