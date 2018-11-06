package mx.com.gunix.framework.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.apache.ibatis.cache.CacheException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import com.hunteron.core.Context;

import mx.com.gunix.framework.documents.EmbeddedLogicalDocManager;
import mx.com.gunix.framework.persistence.EmbeddedLogicalDocManagerOracle11g;
import mx.com.gunix.framework.security.domain.persistence.JdbcGunixPersistentTokenRepository;

public class PersistenceCustomization implements PersistenceCustomizationDefinition{
	
	@Bean
	@Primary
	@Override
	public synchronized DataSource dataSource() {
		return new JndiDataSourceLookup().getDataSource(Context.DB_JNDI_NAME.get());
	}
	
	@Bean
	@Override
	public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
		JdbcGunixPersistentTokenRepository ptr = new JdbcGunixPersistentTokenRepository();
		ptr.setDataSource(dataSource);
		ptr.init("select username,series,token,last_used from seguridad.persistent_logins where series = ?", 
				 "insert into seguridad.persistent_logins (username, series, token, last_used) values(?,?,?,?)", 
				 "update seguridad.persistent_logins set token = ?, last_used = ? where series = ?", 
				 "delete from seguridad.persistent_logins where username = ?", 
				 "create table seguridad.persistent_logins (username varchar(64) not null, series varchar(64) primary key, token varchar(64) not null, last_used timestamp not null)");
		return ptr;
	}
	
	@Bean
	@Override
	public EmbeddedLogicalDocManager embeddedLogicalDocManager() {
		if (Boolean.parseBoolean(Context.LOGICALDOC_ENABLED.get())) {
			return new EmbeddedLogicalDocManagerOracle11g();
		} else {
			return null;
		}
	}
	
	@Bean
	@Override
    public LookupStrategy aclLookupStrategy(AclCache aclCache, AclAuthorizationStrategy aclAuthorizationStrategy, PermissionGrantingStrategy aclPermissionGrantingStrategy, PermissionFactory aclPermissionFactory) throws CacheException, IOException {
        BasicLookupStrategy lookupStrategy = new BasicLookupStrategy(dataSource(), aclCache, aclAuthorizationStrategy, aclPermissionGrantingStrategy);
        lookupStrategy.setSelectClause("select acl_object_identity.object_id_identity, "
						    			+ "acl_entry.ace_order,  "
						    			+ "acl_object_identity.id as acl_id, "
						    			+ "acl_object_identity.parent_object, "
						    			+ "acl_object_identity.entries_inheriting, "
						    			+ "acl_entry.id as ace_id, "
						    			+ "acl_entry.mask,  "
						    			+ "acl_entry.granting,  "
						    			+ "acl_entry.audit_success, "
						    			+ "acl_entry.audit_failure,  "
						    			+ "acl_sid.principal as ace_principal, "
						    			+ "acl_sid.sid as ace_sid,  "
						    			+ "acli_sid.principal as acl_principal, "
						    			+ "acli_sid.sid as acl_sid, "
						    			+ "acl_class.class "
						    			+ "from seguridad.acl_object_identity "
						    			+ "left join seguridad.acl_sid acli_sid on acli_sid.id = acl_object_identity.owner_sid "
						    			+ "left join seguridad.acl_class on acl_class.id = acl_object_identity.object_id_class   "
						    			+ "left join seguridad.acl_entry on acl_object_identity.id = acl_entry.acl_object_identity "
						    			+ "left join seguridad.acl_sid on acl_entry.sid = acl_sid.id  " + "where ( ");
        lookupStrategy.setPermissionFactory(aclPermissionFactory);
        return lookupStrategy;
    }
	
	@Bean
	@Override
	public AclService aclService(DataSource dataSource, LookupStrategy aclLookupStrategy, AclCache aclCache) throws CacheException, IOException {

		JdbcMutableAclService aclService = new JdbcMutableAclService(dataSource, aclLookupStrategy, aclCache);
		aclService.setClassIdentityQuery("select currval(pg_get_serial_sequence('seguridad.acl_class', 'id'))");
		aclService.setSidIdentityQuery("select currval(pg_get_serial_sequence('seguridad.acl_sid', 'id'))");

		aclService.setDeleteEntryByObjectIdentityForeignKeySql("delete from seguridad.acl_entry where acl_object_identity=?");
		aclService.setDeleteObjectIdentityByPrimaryKeySql("delete from seguridad.acl_object_identity where id=?");
		aclService.setInsertClassSql("insert into seguridad.acl_class (class) values (?)");
		aclService.setInsertEntrySql("insert into seguridad.acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) values (?, ?, ?, ?, ?, ?, ?)");
		aclService.setInsertObjectIdentitySql("insert into seguridad.acl_object_identity (object_id_class, object_id_identity, owner_sid, entries_inheriting) values (?, ?, ?, ?)");
		aclService.setInsertSidSql("insert into seguridad.acl_sid (principal, sid) values (?, ?)");
		aclService.setClassPrimaryKeyQuery("select id from seguridad.acl_class where class=?");
		aclService.setObjectIdentityPrimaryKeyQuery("select acl_object_identity.id from seguridad.acl_object_identity, seguridad.acl_class where acl_object_identity.object_id_class = acl_class.id and acl_class.class=? and acl_object_identity.object_id_identity = ?");
		aclService.setSidPrimaryKeyQuery("select id from seguridad.acl_sid where principal=? and sid=?");
		aclService.setUpdateObjectIdentity("update seguridad.acl_object_identity set parent_object = ?, owner_sid = ?, entries_inheriting = ? where id = ?");
		aclService.setFindChildrenQuery("select obj.object_id_identity as obj_id, class.class as class "
										+ "from seguridad.acl_object_identity obj, seguridad.acl_object_identity parent, seguridad.acl_class class "
										+ "where obj.parent_object = parent.id and obj.object_id_class = class.id "
										+ "and parent.object_id_identity = ? and parent.object_id_class = ("
										+ "select id FROM seguridad.acl_class where acl_class.class = ?)");
		return aclService;
	}

}
