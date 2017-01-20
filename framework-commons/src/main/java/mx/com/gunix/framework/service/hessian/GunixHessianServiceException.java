package mx.com.gunix.framework.service.hessian;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class GunixHessianServiceException extends Throwable {
	private static final long serialVersionUID = 1L;

	public GunixHessianServiceException(Throwable e1) {
		super(ExceptionUtils.getRootCause(e1) == null ? e1.getClass().getName() : (ExceptionUtils.getRootCause(e1).getClass().getName()) +": "+ ExceptionUtils.getRootCause(e1).getMessage(), null);
		setStackTrace(ExceptionUtils.getRootCause(e1) == null ? e1.getStackTrace() : (ExceptionUtils.getRootCause(e1).getStackTrace()));
	}

}
