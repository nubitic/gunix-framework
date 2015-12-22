<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html>
	<head>
		<title>Gunix</title>
		<spring:theme code="styleSheet" var="gunix_css" />
		<spring:url value="/" var="home_url" />
		<spring:url value="/${gunix_css}" var="gunix_css_url" />
	    <spring:url value="/static/css/ie.css" var="gunix_ie_css_url" />
		<spring:url value="/static/css/showLoading.css" var="showLoading_css_url" />
		<spring:url value="/static/js/gunix.js" var="gunix_js_url" />
		<spring:url value="/static/js/hideshow.js" var="hideshow_js_url" />
		<spring:url value="/static/js/jquery.equalHeight.js" var="jquery_equalHeight_js_url" />
		<spring:url value="/static/js/jquery.showLoading.min.js" var="jquery_showLoading_min_js_url" />
		<spring:url value="/static/images" var="images" />
		<link rel="stylesheet" type="text/css" media="screen" href="${gunix_css_url}" />
		<link rel="stylesheet" type="text/css" href="${showLoading_css_url}" />
		<link rel="SHORTCUT ICON" href="${images}/favicon.ico" />
		<!--[if lt IE 9]>
			<link rel="stylesheet" href="${gunix_ie_css_url}" type="text/css" media="screen" />
			<script src="http:/html5shim.googlecode.com/svn/trunk/html5.js"></script>
		<![endif]-->
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"><!-- required for FF3 and Opera --></script>
		<script src="${hideshow_js_url}" type="text/javascript"><!-- required for FF3 and Opera --></script>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery.tablesorter/2.22.5/js/jquery.tablesorter.min.js"><!-- required for FF3 and Opera --></script>
		<script src="${jquery_equalHeight_js_url}" type="text/javascript"><!-- required for FF3 and Opera --></script>
		<script src="${jquery_showLoading_min_js_url}" type="text/javascript"><!-- required for FF3 and Opera --></script>
		<spring:url value="/ajaxFragment?fragments=" var="showFragment"/>
		<script type="text/javascript">
			var showFragment= "${showFragment}";
		</script>
		<script src="${gunix_js_url}" type="text/javascript"><!-- required for FF3 and Opera --></script>
	</head>
	<body>
		<header id="header">
			<hgroup>
				<h1 class="site_title"><a href="${home_url}">Gunix</a></h1>
				<sec:authentication property="principal.aplicaciones" var="apps" />
				<c:forEach items="${apps}" var="app">
					<div class="btn_view_site"><a href="#" onclick="onAppChange('${app.idAplicacion}');">${app.descripcion}</a></div>
				</c:forEach>
			</hgroup>
		</header> <!-- end of header bar -->
		<section id="secondary_bar">
			<div class="user">
				<p><sec:authentication property="principal.username" /></p>
				<a class="logout_user" href="<spring:url value="/logout"/>" title="Logout">Cerrar Sesión</a>
			</div>
			<div class="breadcrumbs_container" id="breadcrumb">
			</div>
		</section><!-- end of secondary bar -->
		<aside id="sidebar" class="column">
			<form class="quick_search" id="roles">
				<tiles:insertAttribute name="roles" ignore="true" />
			</form>
			<hr/>
			<div id="menu">
				<tiles:insertAttribute name="menu" ignore="true" />
			</div>
			<footer>
				<hr />
				<p>Theme by <a href="http://www.medialoot.com">MediaLoot</a></p>
			</footer>
		</aside><!-- end of sidebar -->
		<section id="main" class="column">
			<div id="modulos">
				<tiles:insertAttribute name="modulos" ignore="true" />
			</div>
			<div id="content">
				<tiles:insertAttribute name="content" ignore="true" />
			</div>
			<div class="spacer"></div>
		</section>
	</body>
</html>