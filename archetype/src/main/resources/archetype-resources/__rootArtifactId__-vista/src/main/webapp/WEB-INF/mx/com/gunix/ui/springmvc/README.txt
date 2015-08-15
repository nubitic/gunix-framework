En esta carpeta se deben depositar las Java Server Pages de la Aplicación

Ejemplo:

<%@ taglib prefix="gunix" uri="/framework/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<article class="module width_full">
	<header>
		<h3>${operación} de Cliente</h3>
	</header>
	<div class="module_content">
		<c:if test="${not empty errores}">
			<h4 class="alert_error">Existen errores en el formulario: ${errores}</h4>
		</c:if>
		<fieldset class="width_half">
			<label>ID Usuario</label>
			<form:input path="id" />
			<form:errors path="id" cssClass="alert_error" element="h4"/>
			
			<label>RFC</label>
			<form:input path="rfc" />
			<form:errors path="rfc" cssClass="alert_error" element="h4"/>
			
			<label>Razón Social</label>
			<form:input path="razonSocial" />
			<form:errors path="razonSocial" cssClass="alert_error" element="h4"/>
		</fieldset>
		<div class="clear"></div>
	</div>
	<footer>
		<div class="submit_link">
			<gunix:completeTask label="Enviar..." />
		</div>
	</footer>
</article>

Para mayor información consulta: 

http://docs.spring.io/spring/docs/current/spring-framework-reference/html/view.html#view-jsp
http://docs.oracle.com/javaee/5/tutorial/doc/bnake.html
https://docs.oracle.com/javaee/6/tutorial/doc/gjddd.html
