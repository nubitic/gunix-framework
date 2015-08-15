<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE html>
<html>
<head>
<title>Gunix</title>
<spring:theme code="styleSheet" var="gunix_css" />
<spring:url value="/${gunix_css}" var="gunix_css_url" />
<spring:url value="/static/css/ie.css" var="gunix_ie_css_url" />
<spring:url value="/static/images" var="images" />
<link rel="stylesheet" type="text/css" media="screen"
	href="${gunix_css_url}" />
<link rel="SHORTCUT ICON" href="${images}/favicon.ico" />
<!--[if lt IE 9]>
			<link rel="stylesheet" href="${gunix_ie_css_url}" type="text/css" media="screen" />
			<script src="http:/html5shim.googlecode.com/svn/trunk/html5.js"></script>
		<![endif]-->
</head>

<body>
	<section id="main" class="column">
		<article class="module width_full">
			<form id="form" action="<c:url value='/login'/>" method="POST">
				<header>
					<h3>Login</h3>
				</header>
				<div class="module_content">
					<fieldset class="width_half">
						<c:if test="${not empty param.err}">
							<div>
								<c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}" />
							</div>
						</c:if>
						<c:if test="${not empty param.out}">
							<div>You've logged out successfully.</div>
						</c:if>
						<c:if test="${not empty param.time}">
							<div>You've been logged out due to inactivity.</div>
						</c:if>

						<label>Username:</label>
						<input type="text" name="username" />
						
						<label>Password:</label>
						<input type="password" name="password" />
						
						<label>Remember Me?</label>
						<input type="checkbox" name="remember-me" />
					</fieldset>

					<div class="clear"></div>
				</div>
				<footer>
					<div class="submit_link">
						<input value="Login" type="button" onclick="document.getElementById('form').submit();" />
					</div>
				</footer>
			</form>
		</article>
		<div class="spacer"></div>
	</section>
</body>
</html>