package mx.com.gunix.framework.config;

import javax.el.ELContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ScopedAttributeELResolver;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import mx.com.gunix.framework.ui.GunixVariableGetter;
import mx.com.gunix.framework.ui.springmvc.MainController;

@WebListener
public class GunixServletContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		JspFactory.getDefaultFactory().getJspApplicationContext(sce.getServletContext()).addELResolver(new ScopedAttributeELResolver() {
			private ServletContext sc = sce.getServletContext();
			private WebApplicationContext wappCtx;

			@Override
			public Object getValue(ELContext context, Object base, Object property) {
				Object ans = super.getValue(context, base, property);
				if (ans == null) {
					if (!MainController.DEFAULT_END_TASK_SPRINGMVC_VIEW.equals(((JspContext) context.getContext(JspContext.class)).getAttribute("jspView", PageContext.REQUEST_SCOPE))) {
						setValue(context, base, property, getGunixVariableGetter().get(property.toString()));
						ans = super.getValue(context, base, property);
					}
				}
				return ans;
			}

			private GunixVariableGetter getGunixVariableGetter() {
				if (wappCtx == null) {
					wappCtx = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
				}

				return wappCtx.getBean(GunixVariableGetter.class);
			}

		});
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub

	}

}
