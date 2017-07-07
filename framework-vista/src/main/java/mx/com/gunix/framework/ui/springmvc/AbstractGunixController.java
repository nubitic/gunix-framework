package mx.com.gunix.framework.ui.springmvc;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.multiaction.InternalPathMethodNameResolver;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;

import mx.com.gunix.framework.processes.domain.Tarea;
import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.service.GetterService;
import mx.com.gunix.framework.ui.GunixVariableGetter;
import mx.com.gunix.framework.util.GunixLogger;

public abstract class AbstractGunixController<S extends Serializable> implements Controller {
	private static final ThreadLocal<ServletRequestDataBinder> binder = new ThreadLocal<ServletRequestDataBinder>();
	private MethodNameResolver methodNameResolver = new InternalPathMethodNameResolver();
	private Boolean parameterBindingEmptyStringIsNull = Boolean.TRUE;
	private Map<String, Method> methodMap = new HashMap<String, Method>();
	private static final Method NOT_FOUND_METHOD = ReflectionUtils.findMethod(AbstractGunixController.class, "notFoundMethod");
	private Tarea tarea;
	private static GunixLogger log;
	@Autowired
	GunixVariableGetter vg;

	@Autowired
	Validator validator;

	@Autowired
	@Lazy
	GetterService gs;

	@Autowired
	RequestMappingHandlerAdapter rmha;

	@Autowired
	MessageSource ms;
	
	protected abstract String doConstruct(HttpSession session, Model uiModel);

	protected abstract List<Variable<?>> getVariablesTarea(HttpServletRequest request);

	protected abstract String getComentarioTarea(HttpServletRequest request);

	protected abstract String doEnter(HttpServletRequest request, Model uiModel);
	
	protected void configBinder(ServletRequestDataBinder binder) {
		if (parameterBindingEmptyStringIsNull) {
			binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
		}
	}

	@SuppressWarnings("unchecked")
	public final ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		vg.setInstancia(Utils.getTareaActual(request).getInstancia());
		preInitDataBinder(request,request.getParameter("cgCommandName"), false);
		String methodName = methodNameResolver.getHandlerMethodName(request);

		if (methodName != null) {
			Method m = methodMap.get(methodName);
			if (m == null) {
				for (Method cM : ReflectionUtils.getAllDeclaredMethods(getClass())) {
					if (methodName.equals(cM.getName())) {
						m = cM;
						break;
					}
				}
				if (m == null) {
					m = NOT_FOUND_METHOD;
				}
				methodMap.put(methodName, m);
			}
			if (m != NOT_FOUND_METHOD) {
				return rmha.handle(request, response, new HandlerMethod(this, m));
			}
		}
		// Si llega a hasta este punto se realiza la invocación default a doEnter
		Model uiModel = new ExtendedModelMap();
		return new ModelAndView(doEnter(request, uiModel).replace(".", "/"), (Map<String, ?>) uiModel);
	}

	@SuppressWarnings("unchecked")
	protected final S getBean() {
		if (binder.get() == null) {
			throw new IllegalStateException("No se puede obtener el Bean dado que no se indicó el tipo específico para la clase genérica AbstractGunixController");
		}
		return (S) binder.get().getTarget();
	}
	protected void bindDejaStringVacioAsStringVacio() {
		parameterBindingEmptyStringIsNull = Boolean.FALSE;
	}
	@SuppressWarnings("unchecked")
	void preInitDataBinder(HttpServletRequest request, String commandName, boolean validar) throws BindException {
		Type genSuperType = getClass().getGenericSuperclass();
		if (genSuperType instanceof ParameterizedType) {
			Type[] typeArguments = ((ParameterizedType) genSuperType).getActualTypeArguments();
			if (typeArguments.length == 1) {
				Class<S> clazz = ((Class<S>) typeArguments[0]);
				try {
					binder.set(new ServletRequestDataBinder(clazz.newInstance(), commandName));
					configBinder(binder.get());
					if (request != null) {
						binder.get().addValidators(validator);
						binder.get().bind(request);
						if(validar){
							binder.get().validate();
						}
						binder.get().close();
					}
				} catch (InstantiationException | IllegalAccessException e) {
					throw new IllegalArgumentException("No fue posible inicializar el ServletRequestDataBinder con una nueva instancia de " + clazz.getName(), e);
				}
			}
		}
	}

	void setTareaActual(Tarea tarea) {
		this.tarea = tarea;
		vg.setInstancia(tarea.getInstancia());
	}

	protected final Serializable $(String nombreVariable) {
		return vg.get(nombreVariable);
	}

	protected Serializable get(String uri, Object... args) {
		Object[] argsWithPIDDIF = null;
		if (args != null) {
			argsWithPIDDIF = new Object[args.length + 3];
			System.arraycopy(args, 0, argsWithPIDDIF, 1, args.length);
			argsWithPIDDIF[0] = GetterService.INCLUDES_PID_PDID;
			argsWithPIDDIF[args.length + 1] = tarea == null ? vg.getInstancia().getId() : tarea.getInstancia().getId();
			argsWithPIDDIF[args.length + 2] = tarea == null ? vg.getInstancia().getProcessDefinitionId() : tarea.getInstancia().getProcessDefinitionId();
		} else {
			argsWithPIDDIF = new Object[3];
			argsWithPIDDIF[0] = GetterService.INCLUDES_PID_PDID;
			argsWithPIDDIF[1] = tarea == null ? vg.getInstancia().getId() : tarea.getInstancia().getId();
			argsWithPIDDIF[2] = tarea == null ? vg.getInstancia().getProcessDefinitionId() : tarea.getInstancia().getProcessDefinitionId();
		}
		return gs.get(uri, args);
	}
	
	@SuppressWarnings("unused")
	private final void notFoundMethod() {
	}

	protected String gMssg(String mKey, String defaultMessage, Object... mArgs) {
		return mx.com.gunix.framework.util.Utils.procesaMensaje(ms, getClass(), mKey, defaultMessage, mArgs);
	}
	
	protected void log(Level nivel, Supplier<String> mensajeSupplier, Supplier<Throwable> throwableSupplier) {
		if (log == null) {
			log = GunixLogger.getLogger(getClass());
		}
		log.log(nivel, mensajeSupplier, throwableSupplier);
	}
}
