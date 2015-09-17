<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<sec:authentication property="principal.aplicaciones" var="apps" />
<article class="module width_full">
	<header><h3>Modulos</h3></header>
	<div class="module_content">
		<c:forEach items="${apps}" var="app">
			<c:if test="${app.idAplicacion == param.idAplicacion}">
				<c:forEach items="${app.roles}" var="rol">
					<c:if test="${rol.idRol == param.idRol}">
						<c:forEach items="${rol.modulos}" var="modulo">
							<article class="module width_quarter">
								<img src="<spring:url value="/VAADIN/themes/gunix/img/"/>${modulo.icono}" onclick="onModuloChange('${modulo.idModulo}','${param.idRol}','${param.idAplicacion}','${modulo.descripcion}');"/>
								<p>${modulo.descripcion}</p>
							</article>
						</c:forEach>
					</c:if>
				</c:forEach>
			</c:if>
		</c:forEach>
		<div class="clear"></div>
	</div>
</article>