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

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String main() {
		return "index";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/ajaxFragment", method = RequestMethod.POST)
	public <S extends Serializable> String ajaxFragment(Model uiModel, HttpServletRequest request) throws BeansException, ClassNotFoundException {
		if ("roles".equals(request.getParameter("fragments"))) {
			uiModel.addAttribute("jspView", "framework.index.roles".replace(".", "/"));
		} else {
			if ("modulos".equals(request.getParameter("fragments"))) {
				uiModel.addAttribute("jspView", "framework.index.modulos".replace(".", "/"));
			} else {
				if ("menu".equals(request.getParameter("fragments"))) {
					uiModel.addAttribute("jspView", "framework.index.menu".replace(".", "/"));
				} else {
					if ("content".equals(request.getParameter("fragments"))) {
						HttpSession session = request.getSession();
						Map<String, Tarea> appTareaActualMap = (Map<String, Tarea>) session.getAttribute(APP_TAREA_ACTUAL_MAP);
						if (appTareaActualMap == null) {
							appTareaActualMap = new HashMap<String, Tarea>();
							session.setAttribute(APP_TAREA_ACTUAL_MAP, appTareaActualMap);
						}

						Instancia instancia = null;

						if (request.getParameter("isCompleteTask") != null && "true".equals(request.getParameter("isCompleteTask"))) {
							Tarea tareaActual = appTareaActualMap.get(request.getParameter("idAplicacion"));
							Class<?> agcClass = getClass().getClassLoader().loadClass(tareaActual.getVista());
							AbstractGunixController<S> agc = (AbstractGunixController<S>) applicationContext.getBean(agcClass);
							final GunixSpringMVCView annotation = applicationContext.findAnnotationOnBean(applicationContext.getBeanNamesForType(agcClass)[0], GunixSpringMVCView.class);
							String commandName = annotation.value();
							try {
								agc.preInitDataBinder(request, commandName);
								tareaActual.setVariables(agc.getVariablesTarea(request));
								tareaActual.setComentario(agc.getComentarioTarea(request));

								instancia = as.completaTarea(tareaActual);
							} catch (BindException e) {
								uiModel.addAllAttributes(e.getBindingResult().getModel());
								instancia = tareaActual.getInstancia();
							}
						} else {
							Funcion funcion = determinaFuncion(request);
							instancia = as.iniciaProceso(funcion.getProcessKey(), Variable.fromParametros(funcion.getParametros()), "");
						}

						if (instancia.getTareaActual() == null || instancia.getTareaActual().getVista().equals(Tarea.DEFAULT_END_TASK_VIEW)) {
							uiModel.addAttribute("jspView", "framework.defaultProcessEndView".replace(".", "/"));
						} else {
							appTareaActualMap.put(request.getParameter("idAplicacion"), instancia.getTareaActual());
							toModel(uiModel, instancia.getVariables());
							Class<?> agcClass = getClass().getClassLoader().loadClass(instancia.getTareaActual().getVista());
							AbstractGunixController<S> agc = (AbstractGunixController<S>) applicationContext.getBean(agcClass);
							final GunixSpringMVCView annotation = applicationContext.findAnnotationOnBean(applicationContext.getBeanNamesForType(agcClass)[0], GunixSpringMVCView.class);
							String commandName = annotation.value();

							try {
								agc.preInitDataBinder(null, commandName);
							} catch (BindException ignorar) {}

							if (!"na".equals(commandName)) {
								Map<String, Object> modelMap = uiModel.asMap();
								uiModel.addAttribute(commandName, modelMap.get(commandName) == null ? agc.getBean() : modelMap.get(commandName));
								uiModel.addAttribute("commandName", commandName);
							}
							uiModel.addAttribute("jspView", agc.doConstruct(uiModel).replace(".", "/"));
						}
					}
				}
			}
		}
		return "index";
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