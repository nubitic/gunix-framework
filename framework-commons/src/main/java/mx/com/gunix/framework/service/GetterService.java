package mx.com.gunix.framework.service;

import java.io.Serializable;

import com.hunteron.core.Hessian;

@Hessian("/getterService")
public interface GetterService {
	static final ThreadLocal<String[]> pidPdid = new ThreadLocal<String[]>();
	static final int PROCESS_INSTANCEID = 0;
	static final int PROCESS_DEFINITIONID = 1;
	static final String INCLUDES_PID_PDID = "INCLUDES_PID_PDID";

	Serializable get(String uri, Object... args);
}
