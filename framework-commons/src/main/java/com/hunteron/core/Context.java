package com.hunteron.core;
/**
 * hessian
 * 
 * @author rocca.peng@hunteron.com
 * @Description
 * @Date 2015
 */
public enum Context {
	HOST("backEnd.host");
	private String backEndHost;
	 
    private Context(String backEndHost) {
        this.backEndHost = backEndHost;
    }
 
    public String getRemoteUrl() {
        return System.getProperty(backEndHost, "http://localhost:8080/backEnd");
    }
}
