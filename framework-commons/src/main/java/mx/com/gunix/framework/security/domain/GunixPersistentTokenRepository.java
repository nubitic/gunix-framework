package mx.com.gunix.framework.security.domain;

import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

public interface GunixPersistentTokenRepository extends PersistentTokenRepository {
	
	public void init(String tokensBySeriesSql, String insertTokenSql, String updateTokenSql, String removeUserTokensSql, String createTableSql);

}
