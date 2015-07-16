package com.hunteron.core;

import javax.naming.InitialContext;

/**
 * hessian
 * 
 * @author rocca.peng@hunteron.com
 * @Description
 * @Date 2015
 */
public enum Context {
	HOST("BACKEND_HOST");
	private String backEndHost;

	private Context(String backEndHost) {
		this.backEndHost = backEndHost;
	}

	public String getRemoteUrl() {
		String remoteURL = getFromEnvironment(backEndHost);
		return remoteURL != null ? remoteURL : "http://localhost:8081/backEnd";
	}

	String getFromEnvironment(final String name) {
		if (name == null)
			return null;
		String envVar = System.getenv(name);
		if (envVar == null) {

			try {
				final Object object = ((javax.naming.Context) (new InitialContext().lookup("java:comp/env"))).lookup(name);
				if (object != null)
					envVar = object.toString();
			} catch (final Exception ignore) {
			}
		}
		return envVar;
	}
}
