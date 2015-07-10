package mx.com.gunix.service.demo;

import mx.com.gunix.domain.demo.Formulario;

public interface DemoService {
	public void simulaGuardado(Formulario form);

	public Boolean isValid(Formulario form);
}
