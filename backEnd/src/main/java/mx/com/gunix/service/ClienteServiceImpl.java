package mx.com.gunix.service;

import java.util.ArrayList;
import java.util.List;

import mx.com.gunix.domain.Cliente;
import mx.com.gunix.domain.persistence.ClienteMapper;
import mx.com.gunix.framework.service.GunixActivitServiceSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("clienteService")
@Transactional(rollbackFor = Exception.class)
public class ClienteServiceImpl extends GunixActivitServiceSupport implements ClienteService{
	@Autowired
	ClienteMapper cm;

	@Override
	public void guarda(Cliente cliente) {
		cm.guarda(cliente);
		actualizaVariable(cliente);
	}

	@Override
	public Boolean isValid(Cliente cliente) {
		Cliente clienteBD = cm.getClienteByNombre(cliente.getNombre());
		Boolean ans=Boolean.TRUE;
		if(clienteBD!=null){
			List<String> errores = new ArrayList<String>();
			errores.add(new StringBuilder("El cliente: ").append(cliente.getNombre()).append(" ya existe en la Base de Datos").toString());
			agregaVariable("errores", errores);
			ans = Boolean.FALSE;
		}
		return ans;
	}
}
