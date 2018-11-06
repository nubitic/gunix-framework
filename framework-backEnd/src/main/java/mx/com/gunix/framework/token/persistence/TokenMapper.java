package mx.com.gunix.framework.token.persistence;

public interface TokenMapper {
	public void insertaToken(String token);
	public void finalizaToken(String token);
	public void eliminaToken(String token);
	public Boolean isTokenActivo(String token);
}
