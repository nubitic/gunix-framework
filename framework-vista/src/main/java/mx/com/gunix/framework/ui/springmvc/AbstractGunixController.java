package mx.com.gunix.framework.ui.springmvc;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.gunix.framework.processes.domain.Variable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public abstract class AbstractGunixController<S extends Serializable> implements Controller {
	private ServletRequestDataBinder binder;
	@Autowired
	Validator validator;

	protected abstract String doConstruct(Model uiModel);

	protected abstract List<Variable<?>> getVariablesTarea(HttpServletRequest request);

	protected abstract String getComentarioTarea(HttpServletRequest request);

	protected abstract String doEnter(HttpServletRequest request);

	public final ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return new ModelAndView(doEnter(request).replace(".", "/"), null);
	}

	@SuppressWarnings("unchecked")
	protected final S getBean() {
		if (binder == null) {
			throw new IllegalStateException("No se puede obtener el Bean dado que no se indicó el tipo específico para la clase genérica AbstractGunixController");
		}
		return (S) binder.getTarget();
	}

	@SuppressWarnings("unchecked")
	void preInitDataBinder(HttpServletRequest request, String commandName) throws BindException {
		Type genSuperType = getClass().getGenericSuperclass();
		if (genSuperType instanceof ParameterizedType) {
			Type[] typeArguments = ((ParameterizedType) genSuperType).getActualTypeArguments();
			if (typeArguments.length == 1) {
				Class<S> clazz = ((Class<S>) typeArguments[0]);
				try {
					binder = new ServletRequestDataBinder(clazz.newInstance(), commandName);
					if (request != null) {
						binder.addValidators(validator);
						binder.bind(request);
						binder.validate();
						binder.close();
					}
				} catch (InstantiationException | IllegalAccessException e) {
					throw new IllegalArgumentException("No fue posible inicializar el ServletRequestDataBinder con una nueva instancia de " + clazz.getName(), e);
				}
			}
		}
	}
}
