package mx.com.gunix.framework.config;

import javax.sql.DataSource;

import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

public interface PersistenceCustomizationDefinition {
	public LookupStrategy aclLookupStrategy(DataSource dataSource, AclCache aclCache, AclAuthorizationStrategy aclAuthorizationStrategy, PermissionGrantingStrategy aclPermissionGrantingStrategy, PermissionFactory aclPermissionFactory) throws Exception;
	public AclService aclService(DataSource dataSource, LookupStrategy aclLookupStrategy, AclCache aclCache) throws Exception;
	public DataSource dataSource();
	public PersistentTokenRepository persistentTokenRepository(DataSource dataSource);
}
