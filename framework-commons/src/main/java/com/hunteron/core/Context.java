package com.hunteron.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.com.gunix.framework.security.domain.Funcion;

/**
 * 
 * Clase de utileria para obtener valores de tres posibles fuentes: 1) System.getEnv(), 2) System.getProperty() , 3) Atributos JNDI de java:comp/env, 
 *  o 4) un valor default. El potencial de esta clase es que puede obtener/especificar 
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
	VIEW_SSO_IMPLEMENTATION("VIEW_SSO_IMPLEMENTATION","josso"),
	
	VIEW_SSO_PARTNER_ID("VIEW_SSO_PARTNER_ID","false"),
	VIEW_SSO_GATEWAY_ENDPOINT_TRANSPORT_PROTOCOL("VIEW_SSO_GATEWAY_ENDPOINT_TRANSPORT_PROTOCOL",null),
	VIEW_SSO_GATEWAY_ENDPOINT_HOST_PORT("VIEW_SSO_GATEWAY_ENDPOINT_HOST_PORT",null),
	VIEW_SSO_GATEWAY_ENDPOINT_WEB_CONTEXT("VIEW_SSO_GATEWAY_ENDPOINT_WEB_CONTEXT",null),
	VIEW_SSO_GATEWAY_ENDPOINT_PASSWORD("VIEW_SSO_GATEWAY_ENDPOINT_PASSWORD",null),
	VIEW_SSO_BACKTO_HOST("VIEW_SSO_BACKTO_HOST",null),
	VIEW_SSO_BACKTO_CONTEXT("VIEW_SSO_BACKTO_CONTEXT",null),
	
	VIEW_SSO_SAML_SUCCESS_PAGE("VIEW_SSO_SAML_SUCCESS_PAGE","/"),
	VIEW_SSO_SAML_SUCCESS_LOGOUT("VIEW_SSO_SAML_SUCCESS_LOGOUT","/logout"),
	VIEW_SSO_SAML_METADATA_FILE("VIEW_SSO_SAML_METADATA_FILE",null),
	VIEW_SSO_SAML_KEYSTORE("VIEW_SSO_SAML_KEYSTORE",null),
	VIEW_SSO_SAML_KEYSTORE_ALIAS("VIEW_SSO_SAML_KEYSTORE_ALIAS",null),
	VIEW_SSO_SAML_KEYSTORE_PASS("VIEW_SSO_SAML_KEYSTORE_PASS",null),
	VIEW_SSO_SAML_IPID("VIEW_SSO_SAML_IPID",null),
	
	VIEW_VAADIN_STATIC_RESOURCES("VIEW_VAADIN_STATIC_RESOURCES", ""),
	
	VIEW_ENABLE_ANONYMOUS("VIEW_ENABLE_ANONYMOUS","false"),
	
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
	MAIL_FROM("MAIL_FROM",null), 
	REDIS_ENABLED("REDIS_ENABLED","true");
	
	private String envVar;
	private String defaultValue;
	private static ConcurrentMap<String,Object> cachedValueMap = new ConcurrentHashMap<String,Object>();
	private static javax.naming.Context ic;
	
	private static Object NULL_OBJECT  = new Object();

	private Context(String envVar, String defaultValue) {
		this.envVar = envVar;
		this.defaultValue = defaultValue;
	}

	public String get() {
		if (envVar == null) {
			return null;
		}
		
		return getEnvVar(envVar, defaultValue);
	}

	private static javax.naming.Context getIc() throws NamingException {
		if (ic == null) {
			ic = (javax.naming.Context) new InitialContext().lookup("java:comp/env");
		}
		return ic;
	}
	
	public static String getEnvVar(String envVar, String defaultValue) {
		String value = null;
		if(cachedValueMap.get(envVar) == null) {
			value = System.getenv(envVar);
			if (value == null) {
				value = System.getProperty(envVar);
				if (value == null) {
					try {
						Object object = getIc().lookup(envVar);
						if (object != null) {
							value = object.toString();
							cachedValueMap.put(envVar, value);
						} else {
							cachedValueMap.put(envVar, NULL_OBJECT);
						}
					} catch (Throwable logE) {
						cachedValueMap.put(envVar, NULL_OBJECT);
					}
				}
			}
		} else {
			value = cachedValueMap.get(envVar) == NULL_OBJECT ? null : cachedValueMap.get(envVar).toString();
		}
		
		return value != null ? value : defaultValue;		
	}
}
