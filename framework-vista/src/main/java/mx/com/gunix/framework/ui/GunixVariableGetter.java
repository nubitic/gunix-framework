package mx.com.gunix.framework.ui;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.service.ActivitiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class GunixVariableGetter implements Serializable {
	private static final long serialVersionUID = 1L;

	@Autowired
	@Lazy
	ActivitiService as;

	private Map<String, Serializable> varCache = new HashMap<String, Serializable>();
	private static final Serializable NULL_OBJECT = new Serializable() {
		private static final long serialVersionUID = 1L;
	};

	public Serializable get(Instancia instancia, String nombreVariable) {

		Serializable s = varCache.get(nombreVariable);
		if (s == null) {
			s = as.getVar(instancia, nombreVariable);
			if (s == null) {
				varCache.put(nombreVariable, NULL_OBJECT);
			} else {
				varCache.put(nombreVariable, s);
			}
		} else {
			if (s == NULL_OBJECT) {
				s = null;
			}
		}
		return s;
	}
}
