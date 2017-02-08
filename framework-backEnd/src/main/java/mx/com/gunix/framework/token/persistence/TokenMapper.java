package mx.com.gunix.framework.token.persistence;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface TokenMapper {
	@Insert("INSERT INTO SEGURIDAD.JOB_TOKEN(TOKEN, INICIO) VALUES(#{token},now())")
	public void insertaToken(String token);

	@Update("UPDATE SEGURIDAD.JOB_TOKEN set FIN = now() WHERE TOKEN = #{token}")
	public void finalizaToken(String token);
	
	@Delete("DELETE FROM SEGURIDAD.JOB_TOKEN WHERE TOKEN = #{token}")
	public void eliminaToken(String token);

	@Select("SELECT case when FIN IS NULL then 1::BOOLEAN else 0::BOOLEAN end FROM SEGURIDAD.JOB_TOKEN WHERE TOKEN = #{token}")
	public Boolean isTokenActivo(String token);
}
