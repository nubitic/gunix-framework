package com.hunteron.core;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.com.gunix.framework.security.domain.Funcion;

/**
 * 
 * Clase de utileria para obtener valores de tres posibles fuentes: 1) System.getEnv(), 2) tags <Environment> en META-INF/context.xml, 
 *  o 3) un valor default. El potencial de esta clase es que puede obtener/especificar 
 * valores default hasta para parámetros de anotaciones (p. ej. com.hunteron.core.@Hessian(host default Context.HOST))
 * 
 * #hessian, #gunix
 * 
 * @author rocca.peng@hunteron.com, jesus.agv@nubitic.mx
 * @Description
 * @Date 2015, 2018
 * @see https://www.oschina.net/code/piece_full?code=46675 http://www.freeteam.cn/site/FreeCMS/share/info/2015/3785.html http://www.voidcn.com/code/p-qytsivzm-n.html http://www.bcxue.com/thread-29924-1-1.html
 */
public enum Context {
	HOST("BACKEND_HOST", "http://localhost:8081/backEnd"), 
	VIEW_INDEX_TILE_DEF("VIEW_INDEX_TILE_DEF", "gunix.index"), 
	VIEW_ENGINE("VIEW_ENGINE", Funcion.ViewEngine.VAADIN.name()),
	VIEW_THEME("VIEW_THEME","gunix"),
	VIEW_VAADIN_ENABLE_DEBUG_MODE("VIEW_VAADIN_ENABLE_DEBUG_MODE","false"),
	
	VIEW_ENABLE_SSO("VIEW_ENABLE_SSO","false"),
	VIEW_SSO_PARTNER_ID("VIEW_SSO_PARTNER_ID","false"),
	VIEW_SSO_GATEWAY_ENDPOINT_TRANSPORT_PROTOCOL("VIEW_SSO_GATEWAY_ENDPOINT_TRANSPORT_PROTOCOL",null),
	VIEW_SSO_GATEWAY_ENDPOINT_HOST_PORT("VIEW_SSO_GATEWAY_ENDPOINT_HOST_PORT",null),
	VIEW_SSO_GATEWAY_ENDPOINT_WEB_CONTEXT("VIEW_SSO_GATEWAY_ENDPOINT_WEB_CONTEXT",null),
	VIEW_SSO_GATEWAY_ENDPOINT_PASSWORD("VIEW_SSO_GATEWAY_ENDPOINT_PASSWORD",null),
	VIEW_SSO_BACKTO_HOST("VIEW_SSO_BACKTO_HOST",null),
	VIEW_SSO_BACKTO_CONTEXT("VIEW_SSO_BACKTO_CONTEXT",null),
	
	ACTIVITI_MASTER("ACTIVITI_MASTER","false"),
	
	STANDALONE_APP("STANDALONE_APP","false"),
	ENABLE_ADMIN_APP_SERVICES("ENABLE_ADMIN_APP_SERVICES","false"),
	ID_APLICACION("ID_APLICACION",null),
	
	DB_USE_EMBEDDED("DB_USE_EMBEDDED","false"),
	DB_MAX_POOL_SIZE("DB_MAX_POOL_SIZE","15"),
	DB_USE_SSL("DB_USE_SSL","false"),
	DB_APP_SCHEMA("DB_APP_SCHEMA",""),
	DB_EMBEDDED_HOME("DB_EMBEDDED_HOME",null),
	DB_USER("DB_USER",null),
	DB_PASSWORD("DB_PASSWORD",null),
	DB_NAME("DB_NAME",null),
	DB_PORT("DB_PORT",null),
	DB_SERVER_NAME("DB_SERVER_NAME",null),
	DB_JNDI_NAME("DB_JNDI_NAME",null),
	
	REDIS_HOST("REDIS_HOST",null),
	EMBEDDED_REDIS_HOME("EMBEDDED_REDIS_HOME",null),
	REDIS_PORT("REDIS_PORT",null),
	REDIS_PASSWORD("REDIS_PASSWORD",null),
	
	MONGO_DB_NAME("MONGO_DB_NAME",null),
	MONGO_PORT("MONGO_PORT",null),
	MONGO_HOSTNAME("MONGO_HOSTNAME",null),
	MONGO_USER("MONGO_USER",null),
	MONGO_PASSWORD("MONGO_PASSWORD",null),
	MONGO_INSTALL_PATH("MONGO_INSTALL_PATH",null),
	
	LOGICALDOC_ENABLED("LOGICALDOC_ENABLED","false"),
	LOGICALDOC_EMBEDDED_HOME("LOGICALDOC_EMBEDDED_HOME",".logicalDocRepo"),
	LOGICALDOC_USER("LOGICALDOC_USER","admin"),
	LOGICALDOC_PASSWORD("LOGICALDOC_PASSWORD","admin"),
	LOGICALDOC_HOSTNAME("LOGICALDOC_HOSTNAME","http://localhost:"),
	LOGICALDOC_PORT("LOGICALDOC_PORT","7080"),
	LOGICALDOC_CONTEXT("LOGICALDOC_CONTEXT","/logicaldoc"),
	
	MAIL_SERVER("MAIL_SERVER",null),
	MAIL_PORT("MAIL_PORT",null),
	MAIL_USERNAME("MAIL_USERNAME",null),
	MAIL_PASSWORD("MAIL_PASSWORD",null),
	MAIL_PROPERTIES("MAIL_PROPERTIES",null),
	MAIL_FROM("MAIL_FROM",null);	
	
	private String envVar;
	private String defaultValue;
	private Object cachedValue;
	private javax.naming.Context ic;
	
	private Object NULL_OBJECT;

	private Context(String envVar, String defaultValue) {
		this.envVar = envVar;
		this.defaultValue = defaultValue;
		this.NULL_OBJECT = new Object();
	}

	public String get() {
		if (envVar == null) {
			return null;
		}
		
		String value = null;
		
		if(cachedValue == null) {
			value = System.getenv(envVar);
			if (value == null) {
				try {
					Object object = getIc().lookup(envVar);
					if (object != null) {
						value = object.toString();
						cachedValue = value;
					} else {
						cachedValue = NULL_OBJECT;
					}
				} catch (Throwable logE) {
					cachedValue = NULL_OBJECT;
				}
			}	
		}else {
			value = cachedValue == NULL_OBJECT ? null : cachedValue.toString();
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
