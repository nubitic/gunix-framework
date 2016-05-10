package mx.com.gunix.framework.ui.springmvc;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.ControllerClassNameHandlerMapping;

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
import mx.com.gunix.framework.util.GunixFile;

@Controller
@Secured(AuthenticatedVoter.IS_AUTHENTICATED_REMEMBERED)
public class MainController {
	public static final String GUNIX_FILE_UPLOAD_ATTR ="gunixFile";

	@Autowired
	@Lazy
	ActivitiService as;

	@Autowired
	@Lazy
	ApplicationContext applicationContext;
	
	@Autowired
	ControllerClassNameHandlerMapping ccnhm;
	
	private static final Method generatePathMappings = ReflectionUtils.findMethod(ControllerClassNameHandlerMapping.class, "generatePathMappings", new Class[] { Class.class });

	public static final String DEFAULT_END_TASK_SPRINGMVC_VIEW = "framework/defaultProcessEndView";
	static{
		ReflectionUtils.makeAccessible(generatePathMappings);
	}

	@RequestMapping(value = "/startProcess", method = RequestMethod.GET)
	public String main(Model uiModel, HttpServletRequest request) throws BeansException, ClassNotFoundException {
		Funcion funcion = determinaFuncion(request);
		Instancia instancia = as.iniciaProceso(funcion.getProcessKey(), Variable.fromParametros(funcion.getParametros()), "");
		doControl(request, instancia, uiModel, false, false);
		return "index";
	}

	@RequestMapping(value = "/ajaxFragment", method = { RequestMethod.POST })
	public String ajaxFragment(Model uiModel, HttpServletRequest request) throws BeansException, ClassNotFoundException {
		if ("content".equals(request.getParameter("fragments"))) {		 
			doControl(request, null, uiModel, true, false);
		}
		return "index";
	}

	@RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
	public String uploadFileHandler(@RequestParam("gunixFile") MultipartFile file, @RequestParam("forwardFileTo") String forwardFileTo, HttpServletRequest request) throws IOException {
		GunixFile gf = new GunixFile(file.getOriginalFilename());
		gf.setMimeType(file.getContentType());
		File f = File.createTempFile("uploadedFile", ".tmp");
		file.transferTo(f);
		gf.setFile(f);

		request.setAttribute(GUNIX_FILE_UPLOAD_ATTR, gf);
		String contextPath = request.getContextPath();		
		return "forward:" + (forwardFileTo.startsWith(contextPath) ? forwardFileTo.replaceAll(contextPath, "") : forwardFileTo);
	}
	
	private <S extends Serializable> void doControl(HttpServletRequest request, Instancia instancia, Model uiModel, boolean isCompleteTask, boolean isCommitFailed) throws BeansException, ClassNotFoundException {
		Tarea tareaActual = null;
		if (isCompleteTask) {
			tareaActual = Utils.getTareaActual(request);
		} else {
			Utils.setTareaActual(request, instancia.getTareaActual());
			toModel(uiModel, instancia.getVariables());
		}
		AbstractGunixController<S> agc = null;

		try {
			if (isCompleteTask) {
				agc = getAgc(request, tareaActual.getVista(), uiModel);
				tareaActual.setVariables(agc.getVariablesTarea(request));
				tareaActual.setComentario(agc.getComentarioTarea(request));
				instancia = as.completaTarea(tareaActual);
			}
			
			String newJspView = null;
			if ((instancia.getTareaActual() == null || instancia.getTareaActual().getVista().equals(Tarea.DEFAULT_END_TASK_VIEW))) {
				newJspView = DEFAULT_END_TASK_SPRINGMVC_VIEW;
			} else {
				agc = getAgc(null, instancia.getTareaActual().getVista(), uiModel);
				agc.setTareaActual(instancia.getTareaActual());
				newJspView = (isCommitFailed ? agc.doEnter(request, uiModel) :agc.doConstruct(request.getSession(), uiModel)).replace(".", "/");
			}
			String[] urls = (String[]) ReflectionUtils.invokeMethod(generatePathMappings, ccnhm, new Object[] { agc.getClass() });
			uiModel.addAttribute("jspView", newJspView);
			uiModel.addAttribute("cGunixViewPath", urls[0]);
		} catch (BindException e) {
			if (isCompleteTask) {
				uiModel.addAllAttributes(e.getBindingResult().getModel());
				instancia = tareaActual.getInstancia();
				doControl(request, instancia, uiModel, false, true);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <S extends Serializable> AbstractGunixController<S> getAgc(HttpServletRequest request,String claseVista, Model uiModel) throws BindException, ClassNotFoundException {
		Class<?> agcClass = getClass().getClassLoader().loadClass(claseVista);
		AbstractGunixController<S> agc = (AbstractGunixController<S>) applicationContext.getBean(agcClass);
		final GunixSpringMVCView annotation = applicationContext.findAnnotationOnBean(applicationContext.getBeanNamesForType(agcClass)[0], GunixSpringMVCView.class);
		String commandName = annotation.value();
		agc.preInitDataBinder(request, commandName);
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