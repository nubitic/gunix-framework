package mx.com.gunix.framework.ui.springmvc;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import mx.com.gunix.framework.processes.domain.Tarea;

final class Utils {
	private static final String APP_TAREA_ACTUAL_MAP = "APP_TAREA_ACTUAL_MAP";

	static Tarea getTareaActual(HttpServletRequest request) {
		return getAppTareaActualMap(request).get(request.getParameter("idAplicacion"));
	}

	static void setTareaActual(HttpServletRequest request, Tarea tarea) {
		getAppTareaActualMap(request).put(request.getParameter("idAplicacion"), tarea);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Tarea> getAppTareaActualMap(HttpServletRequest request) {
		HttpSession session = request.getSession();
		Map<String, Tarea> appTareaActualMap = (Map<String, Tarea>) session.getAttribute(APP_TAREA_ACTUAL_MAP);
		if (appTareaActualMap == null) {
			appTareaActualMap = new HashMap<String, Tarea>();
			session.setAttribute(APP_TAREA_ACTUAL_MAP, appTareaActualMap);
		}
		return appTareaActualMap;
	}
}
