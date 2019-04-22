package mx.com.gunix.framework.security.domain.persistence;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import mx.com.gunix.framework.security.domain.Usuario;

public interface UsuarioMapper {
	public Usuario getUsuario(String idUsuario);
	public String getPasswordActual(String idUsuario);
	public void updatePassword(Usuario usuario);
	public void guardaSAMLSSOAuth(@Param("idAplicacion") String idAplicacion, @Param("ssoIndex") String ssoIndex, @Param("localSessionID") String localSessionID, @Param("idUsuario") String idUsuario);
	public List<String> getSAMLLocalSessions(@Param("idAplicacion") String idAplicacion, @Param("ssoIndex") String ssoIndex, @Param("idUsuario") String idUsuario);
	public void deleteSAMLLocalSessions(@Param("idAplicacion") String idAplicacion, @Param("ssoIndex") String ssoIndex, @Param("idUsuario") String idUsuario, @Param("sesionesExpiradas") List<String> sesionesExpiradas);
}
