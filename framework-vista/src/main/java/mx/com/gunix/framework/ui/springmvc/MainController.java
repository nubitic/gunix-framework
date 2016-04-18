package mx.com.gunix.framework.ui.springmvc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.processes.domain.Tarea;
import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.Funcion;
import mx.com.gunix.framework.security.domain.Modulo;
import mx.com.gunix.framework.security.domain.Rol;
import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.service.ActivitiService;
import mx.com.gunix.framework.ui.springmvc.spring.GunixSpringMVCView;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@Secured(AuthenticatedVoter.IS_AUTHENTICATED_REMEMBERED)
public class MainController {
	private final String APP_TAREA_ACTUAL_MAP = "APP_TAREA_ACTUAL_MAP";

	@Autowired
	@Lazy
	ActivitiService as;

	@Autowired
	@Lazy
	ApplicationContext applicationContext;

	@RequestMapping(value = "/startProcess", method = RequestMethod.GET)
	public String main(Model uiModel, HttpServletRequest request) throws BeansException, ClassNotFoundException {
		Funcion funcion = determinaFuncion(request);
		Instancia instancia = as.iniciaProceso(funcion.getProcessKey(), Variable.fromParametros(funcion.getParametros()), "");
		Map<String, Tarea> appTareaActualMap = getTareaActualMap(request);
		doControl(request, instancia, uiModel, appTareaActualMap, false);
		return "index";
	}

	@RequestMapping(value = "/ajaxFragment", method = { RequestMethod.POST })
	public String ajaxFragment(Model uiModel, HttpServletRequest request) throws BeansException, ClassNotFoundException {
		if ("content".equals(request.getParameter("fragments"))) {
			Map<String, Tarea> appTareaActualMap = getTareaActualMap(request);
			 
			doControl(request, null, uiModel, appTareaActualMap, true);
		}
		return "index";
	}

	@SuppressWarnings("unchecked")
	private Map<String, Tarea> getTareaActualMap(HttpServletRequest request) {
		HttpSession session = request.getSession();
		Map<String, Tarea> appTareaActualMap = (Map<String, Tarea>) session.getAttribute(APP_TAREA_ACTUAL_MAP);
		if (appTareaActualMap == null) {
			appTareaActualMap = new HashMap<String, Tarea>();
			session.setAttribute(APP_TAREA_ACTUAL_MAP, appTareaActualMap);
		}
		return appTareaActualMap;
	}

	private <S extends Serializable> void doControl(HttpServletRequest request, Instancia instancia, Model uiModel, Map<String, Tarea> appTareaActualMap, boolean isCompleteTask) throws BeansException, ClassNotFoundException {
		Tarea tareaActual = null;
		if (isCompleteTask) {
			tareaActual = appTareaActualMap.get(request.getParameter("idAplicacion"));
		} else {
			appTareaActualMap.put(request.getParameter("idAplicacion"), instancia.getTareaActual());
			toModel(uiModel, instancia.getVariables());
		}
		AbstractGunixController<S> agc = null;

		try {
			if (isCompleteTask) {
				agc = getAgc(tareaActual.getVista(), uiModel);
				tareaActual.setVariables(agc.getVariablesTarea(request));
				tareaActual.setComentario(agc.getComentarioTarea(request));
				instancia = as.completaTarea(tareaActual);
			}
			
			String newJspView = null;
			if ((instancia.getTareaActual() == null || instancia.getTareaActual().getVista().equals(Tarea.DEFAULT_END_TASK_VIEW))) {
				newJspView = "framework.defaultProcessEndView".replace(".", "/");
			} else {
				agc = getAgc(instancia.getTareaActual().getVista(), uiModel);
				agc.setTareaActual(instancia.getTareaActual());
				newJspView = agc.doConstruct(uiModel).replace(".", "/");
			}

			uiModel.addAttribute("jspView", newJspView);
		} catch (BindException e) {
			if (isCompleteTask) {
				uiModel.addAllAttributes(e.getBindingResult().getModel());
				instancia = tareaActual.getInstancia();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <S extends Serializable> AbstractGunixController<S> getAgc(String claseVista, Model uiModel) throws BindException, ClassNotFoundException {
		Class<?> agcClass = getClass().getClassLoader().loadClass(claseVista);
		AbstractGunixController<S> agc = (AbstractGunixController<S>) applicationContext.getBean(agcClass);
		final GunixSpringMVCView annotation = applicationContext.findAnnotationOnBean(applicationContext.getBeanNamesForType(agcClass)[0], GunixSpringMVCView.class);
		String commandName = annotation.value();
		agc.preInitDataBinder(null, commandName);
		if (!"na".equals(commandName)) {
			Map<String, Object> modelMap = uiModel.asMap();
			uiModel.addAttribute(commandName, modelMap.get(commandName) == null ? agc.getBean() : modelMap.get(commandName));
			uiModel.addAttribute("commandName", commandName);
		}
		return agc;
	}

	private void toModel(Model uiModel, List<Variable<?>> variables) {
		if (variables != null) {
			for (Variable<?> variable : variables) {
				uiModel.addAttribute(variable.getNombre(), variable.getValor());
			}
		}
	}

	private Funcion determinaFuncion(HttpServletRequest request) {
		Funcion funcion = null;
		Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		for (Aplicacion app : usuario.getAplicaciones()) {
			if (app.getIdAplicacion().equals(request.getParameter("idAplicacion"))) {
				for (Rol rol : app.getRoles()) {
					if (rol.getIdRol().equals(request.getParameter("idRol"))) {
						for (Modulo modulo : rol.getModulos()) {
							if (modulo.getIdModulo().equals(request.getParameter("idModulo"))) {
								funcion = seekFuncion(modulo.getFunciones(), request.getParameter("idFuncion"));
								break;
							}
						}
						break;
					}
				}
				break;
			}
		}
		return funcion;
	}

	private Funcion seekFuncion(List<Funcion> funciones, String idHija) {
		Funcion funcion = null;
		for (Funcion f : funciones) {
			if (f.getIdFuncion().equals(idHija)) {
				funcion = f;
				break;
			} else {
				if (f.getHijas() != null) {
					funcion = seekFuncion(f.getHijas(), idHija);
				}
			}
		}
		return funcion;
	}
}