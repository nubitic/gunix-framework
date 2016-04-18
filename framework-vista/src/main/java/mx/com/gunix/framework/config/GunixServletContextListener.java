package mx.com.gunix.framework.config;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.jsp.JspFactory;

@WebListener
public class GunixServletContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		//JspFactory.getDefaultFactory().getJspApplicationContext(sce.getServletContext()).addELResolver(null);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub

	}

}
