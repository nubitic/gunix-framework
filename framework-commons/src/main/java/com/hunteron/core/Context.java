package com.hunteron.core;

import javax.naming.InitialContext;

import mx.com.gunix.framework.security.domain.Funcion;

/**
 * hessian, gunix
 * 
 * @author rocca.peng@hunteron.com, jesus.agv@nubitic.mx
 * @Description
 * @Date 2015, 2018
 */
public enum Context {
	HOST("BACKEND_HOST", "http://localhost:8081/backEnd"), 
	VIEW_INDEX_TILE_DEF("VIEW_INDEX_TILE_DEF", "gunix.index"), 
	VIEW_ENGINE("VIEW_ENGINE", Funcion.ViewEngine.VAADIN.name());

	private String envVar;
	private String defaultValue;

	private Context(String envVar, String defaultValue) {
		this.envVar = envVar;
	}

	public String get() {
		String value = getFromEnvironment(envVar);
		return value != null ? value : defaultValue;
	}

	String getFromEnvironment(final String name) {
		if (name == null)
			return null;
		String envVar = System.getenv(name);
		if (envVar == null) {

			try {
				final Object object = ((javax.naming.Context) (new InitialContext().lookup("java:comp/env")))
						.lookup(name);
				if (object != null)
					envVar = object.toString();
			} catch (final Exception ignore) {
			}
		}
		return envVar;
	}
}
