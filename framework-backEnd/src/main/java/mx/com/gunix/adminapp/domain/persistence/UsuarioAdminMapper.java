package mx.com.gunix.adminapp.domain.persistence;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.security.access.prepost.PostFilter;

import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.DatosUsuario;
import mx.com.gunix.framework.security.domain.Rol;
import mx.com.gunix.framework.security.domain.Usuario;

public interface UsuarioAdminMapper {

	public Usuario getByidUsuario(String idUsuario);
	
	public void insertaUsuario(Usuario usuario);
	
	public void insertaDatos(@Param("idUsuario") String idUsuario,@Param("datosUsuario") DatosUsuario datosUsuario);	
	
	public void insertaUsuarioApp(@Param("idUsuario") String idUsuario,@Param("idAplicacion") String idAplicacion);
	
	public void insertaUsuarioRol(@Param("idUsuario") String idUsuario,@Param("idAplicacion") String idAplicacion,@Param("idRol") String idRol);
	
	public List<Usuario> getByExample(Usuario usuario);
	
	@PostFilter("hasPermission(filterObject, 'ADMINISTRATION')")
	public List<Aplicacion> getAppUsuario(String idUsuario);
	
	public List<Rol> getRolAppUsuario(@Param("idUsuario") String idUsuario,@Param("idAplicacion") String idAplicacion);
	
	public Usuario getDetalleUById(@Param("idUsuario") String idUsuario);
	
	public void updateUsuario(Usuario usuario);
	
	public void updateDatosUsuario(@Param("idUsuario") String idUsuario,@Param("datosUsuario") DatosUsuario datosUsuario);
	
    public void deleteRolesUsuario(@Param("idUsuario") String idUsuario);
	
	public void deleteAppUsuario(@Param("idUsuario") String idUsuario);
	
}
