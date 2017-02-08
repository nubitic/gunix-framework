package mx.com.gunix.framework.service;

import com.hunteron.core.Hessian;

@Hessian("/tokenService")
public interface TokenService {
	public void registraToken(String token);
	public void liberaToken(String token);
	public void eliminaToken(String token);
	public Boolean tokenActivo(String token);
}
