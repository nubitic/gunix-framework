package mx.com.gunix.framework.service;

import java.io.Serializable;

import com.hunteron.core.Hessian;

@Hessian("/getterService")
public interface GetterService {
	Serializable get(String uri, Object... args);
}
