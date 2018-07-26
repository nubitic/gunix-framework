package com.hunteron.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

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
	
	Logger log = Logger.getLogger(Context.class.getName());
	
	private String envVar;
	private String defaultValue;
	private javax.naming.Context ic;

	private Context(String envVar, String defaultValue) {
		this.envVar = envVar;
		this.defaultValue = defaultValue;
	}

	public String get() {
		if (envVar == null) {
			return null;
		}
		
		String value = System.getenv(envVar);
		
		if (value == null) {
			try {
				 Object object = getIc().lookup(envVar);
				if (object != null) {
					value = object.toString();
				}
			} catch (Throwable logE) {
				log.log(Level.INFO, "No fue posible obtener el valor para la variable " + envVar + ": " + logE.getMessage());
			}
		}
		return value != null ? value : defaultValue;
	}

	private javax.naming.Context getIc() throws NamingException {
		if (ic == null) {
			ic = (javax.naming.Context) new InitialContext().lookup("java:comp/env");
		}
		return ic;
	}
}
