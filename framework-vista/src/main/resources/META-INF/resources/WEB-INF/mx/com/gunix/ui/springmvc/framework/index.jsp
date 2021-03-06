<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<!DOCTYPE html>
<html>
	<head>
		<title>Gunix</title>
		<spring:theme code="styleSheet" var="gunix_css" />
		<spring:url value="/" var="gunixContextPath" scope="session"/>
		<spring:url value="/${gunix_css}" var="gunix_css_url" />
		<spring:url value="/static/css/showLoading.css" var="showLoading_css_url" />
		<spring:url value="/static/js/gunix.js" var="gunix_js_url" />
		<spring:url value="/static/js/jquery.showLoading.min.js" var="jquery_showLoading_min_js_url" />
		<spring:url value="/static/images" var="images" />
		<link rel="stylesheet" type="text/css" media="screen" href="${gunix_css_url}" />
		<link rel="stylesheet" type="text/css" href="${showLoading_css_url}" />
		<link rel="SHORTCUT ICON" href="${images}/favicon.ico" />
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"><!-- required for FF3 and Opera --></script>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/blueimp-file-upload/9.12.1/js/vendor/jquery.ui.widget.min.js"><!-- required for FF3 and Opera --></script>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/blueimp-file-upload/9.12.1/js/jquery.iframe-transport.min.js"><!-- required for FF3 and Opera --></script>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/blueimp-file-upload/9.12.1/js/jquery.fileupload.min.js"><!-- required for FF3 and Opera --></script>
		<script src="${jquery_showLoading_min_js_url}" type="text/javascript"><!-- required for FF3 and Opera --></script>
		<script type="text/javascript">
			var showFragment= "ajaxFragment?fragments=";
			var fragmentToUpdate = "content";
		</script>
		<script src="${gunix_js_url}" type="text/javascript"><!-- required for FF3 and Opera --></script>
		<tiles:insertAttribute name="headerExtras" ignore="true" />
	</head>
	<body>
		<div id="content">
			<tiles:insertAttribute name="content" ignore="true" />
		</div>
	</body>
</html>