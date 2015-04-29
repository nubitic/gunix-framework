package mx.com.gunix.framework.processes.domain;

import mx.com.gunix.framework.security.domain.Parametro;

public class Variable extends Parametro {
	public static enum Scope {
		PROCESO,
		TAREA
	}

	private static final long serialVersionUID = 1L;
	private Scope scope;
	
	public static Variable fromParametro(Parametro parametro){
		Variable v= new Variable();
		v.setNombre(parametro.getNombre());
		v.setValor(parametro.getValor());
		v.setScope(Scope.PROCESO);
		return v;
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}
}
