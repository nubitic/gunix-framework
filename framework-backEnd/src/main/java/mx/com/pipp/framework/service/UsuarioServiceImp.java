package mx.com.pipp.framework.service;

import mx.com.pipp.framework.domain.Usuario;
import mx.com.pipp.framework.domain.persistence.UsuarioMapper;
import mx.com.pipp.service.UsuarioService;

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
