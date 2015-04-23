package mx.com.gunix.framework.service;

import mx.com.gunix.framework.domain.Usuario;
import mx.com.gunix.framework.domain.persistence.UsuarioMapper;
import mx.com.gunix.framework.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class UsuarioServiceImp implements UsuarioService {
	@Autowired
	UsuarioMapper um;

	@Override
	public Usuario getUsuario(String idUsuario) {
		return um.getUsuario(idUsuario);
	}
	
}
