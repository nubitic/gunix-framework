<%@ attribute name="envVarName" required="true" type="java.lang.String" %>
<%@ attribute name="defaultValue" required="false" type="java.lang.String" %>
<%getJspContext().setAttribute(envVarName, com.hunteron.core.Context.getEnvVar(envVarName, defaultValue), PageContext.REQUEST_SCOPE);%>