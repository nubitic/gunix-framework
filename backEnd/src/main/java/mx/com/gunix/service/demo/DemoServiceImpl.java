package mx.com.gunix.service.demo;

import mx.com.gunix.domain.demo.Formulario;
import mx.com.gunix.framework.service.GunixActivitServiceSupport;

import org.springframework.stereotype.Service;

@Service("demoService")
public class DemoServiceImpl extends GunixActivitServiceSupport implements DemoService {

	@Override
	public void simulaGuardado(Formulario form) {
		// TODO Auto-generated method stub

	}

	@Override
	public Boolean isValid(Formulario form) {
		// TODO Auto-generated method stub
		return Boolean.TRUE;
	}

}
