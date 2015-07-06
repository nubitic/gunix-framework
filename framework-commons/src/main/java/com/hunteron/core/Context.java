package com.hunteron.core;
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
    	String remoteURL = System.getenv(backEndHost);
        return remoteURL!=null?remoteURL:"http://localhost:8081/backEnd";
    }
}
