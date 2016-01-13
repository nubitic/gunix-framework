package mx.com.gunix.adminapp.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.com.gunix.adminapp.domain.persistence.AplicacionMapper;
import mx.com.gunix.adminapp.domain.persistence.RolMapper;
import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.service.GunixActivitServiceSupport;

@Service("usuarioAdminService")
@Transactional(rollbackFor = Exception.class)
@Secured(AuthenticatedVoter.IS_AUTHENTICATED_REMEMBERED)
public class UsuarioAdminServiceImpl extends GunixActivitServiceSupport<Usuario> {
	@Autowired
	AplicacionMapper am;
	
	@Autowired
	RolMapper rm;
	
	public List<Aplicacion> getAppRoles() {
		List<Aplicacion> appRoles =  am.getAll();
		if(appRoles==null){
			throw new IllegalArgumentException("No se encontraron Aplicaciónes");
		}else{
			appRoles.forEach(app -> {app.setRoles(rm.getByIdAplicacion(app.getIdAplicacion()));} );						
		}
		return appRoles;	
	}

}
