<%@ page import="mx.com.gunix.framework.config.VaadinSecurityConfig" %>
<%@ page import="mx.com.gunix.framework.ui.vaadin.VaadinUtils" %>
<%@ page import="com.hunteron.core.Context" %>
<!DOCTYPE html>
<html>
	<body>
		<iframe
			src="vdn/?<%=VaadinUtils.SELECTED_APP_TAB_REQUEST_PARAMETER + "=" + request.getParameter(VaadinUtils.SELECTED_APP_TAB_REQUEST_PARAMETER) + ("true".equals(Context.VIEW_VAADIN_ENABLE_DEBUG_MODE.get()) ? "&debug" : "") %>"
			style="position: fixed; top: 0px; left: 0px; bottom: 0px; right: 0px; width: 100%; height: 100%; border: none; margin: 0; padding: 0; overflow: auto; z-index: 999999;"
			id="<%=VaadinUtils.GUNIX_VAADIN_IFRAME_ID%>">
			<p>Navegador no soportado</p>
		</iframe>
	</body>
</html>