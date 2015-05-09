package mx.com.gunix.service;

import mx.com.gunix.domain.Cliente;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.AuthenticatedVoter;

@Secured(AuthenticatedVoter.IS_AUTHENTICATED_FULLY)
public interface ClienteService {
	public void guarda(Cliente cliente);
	public Boolean isValid(Cliente cliente);
}
