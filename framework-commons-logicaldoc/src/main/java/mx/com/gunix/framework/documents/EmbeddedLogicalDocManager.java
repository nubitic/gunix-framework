package mx.com.gunix.framework.documents;

import com.hunteron.core.Context;

public interface EmbeddedLogicalDocManager {
	public void start(String logicalDocHome, ClassLoader classLoader) throws Exception;

	default public String getLogicalDocURL() {
		return (Context.LOGICALDOC_HOSTNAME.get() + Context.LOGICALDOC_PORT.get() + Context.LOGICALDOC_CONTEXT.get());
	}
}