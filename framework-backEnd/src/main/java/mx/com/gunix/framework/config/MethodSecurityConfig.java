package mx.com.gunix.framework.config;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.apache.ibatis.cache.CacheException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.intercept.AfterInvocationManager;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionFactory;
import org.springframework.security.acls.domain.EhCacheBasedAclCache;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.UserDetailsAwareConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

@Configuration
@Import(PersistenceConfig.class)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration implements BeanFactoryAware{
	public static String ACL_CACHE_NAME = "aclCache";
	public static final String ACL_ADMIN_ROLE = "ACL_ADMIN";
	private BeanFactory beanFactory;

    @Bean(name = "cacheManager")
    public CacheManager ehCacheManager() throws IOException {
        EhCacheManagerFactoryBean factory = new EhCacheManagerFactoryBean();
        factory.setCacheManagerName(CacheManager.DEFAULT_NAME);
        factory.setShared(true);
        factory.afterPropertiesSet();
        return factory.getObject();
    }
	
    @Bean(name = "ehcache")
    public Ehcache ehCacheFactory() throws CacheException, IOException {
        EhCacheFactoryBean factory = new EhCacheFactoryBean();
        factory.setCacheManager(ehCacheManager());
        factory.setCacheName(ACL_CACHE_NAME);
        factory.afterPropertiesSet();
        return factory.getObject();
    }
    
    @Bean
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority(ACL_ADMIN_ROLE));
    }
    
    @Bean
    public AuditLogger aclAuditLogger() {
        return new ConsoleAuditLogger();
    }
    
	@Bean
	public PermissionGrantingStrategy aclPermissionGrantingStrategy() {
		return new PermissionGrantingStrategy() {

			private final transient AuditLogger auditLogger = aclAuditLogger();

			/**
			 * Determines authorization. The order of the <code>permission</code> and <code>sid</code> arguments is <em>extremely important</em>! The method will iterate through each of the
			 * <code>permission</code>s in the order specified. For each iteration, all of the <code>sid</code>s will be considered, again in the order they are presented. A search will then be
			 * performed for the first {@link AccessControlEntry} object that directly matches that <code>permission:sid</code> combination. When the <em>first containing match</em> is found (ie an ACE that
			 * has the SID currently being searched for and contains the permission bit mask being search for), the grant or deny flag for that ACE will prevail. If the ACE specifies to grant access, the
			 * method will return <code>true</code>. If the ACE specifies to deny access, the loop will stop and the next <code>permission</code> iteration will be performed. If each permission
			 * indicates to deny access, the first deny ACE found will be considered the reason for the failure (as it was the first match found, and is therefore the one most logically requiring
			 * changes - although not always). If absolutely no matching ACE was found at all for any permission, the parent ACL will be tried (provided that there is a parent and
			 * {@link Acl#isEntriesInheriting()} is <code>true</code>. The parent ACL will also scan its parent and so on. If ultimately no matching ACE is found, a <code>NotFoundException</code> will
			 * be thrown and the caller will need to decide how to handle the permission check. Similarly, if any of the SID arguments presented to the method were not loaded by the ACL,
			 * <code>UnloadedSidException</code> will be thrown.
			 *
			 * @param permission
			 *            the exact permissions to scan for (order is important)
			 * @param sids
			 *            the exact SIDs to scan for (order is important)
			 * @param administrativeMode
			 *            if <code>true</code> denotes the query is for administrative purposes and no auditing will be undertaken
			 *
			 * @return <code>true</code> if one of the permissions has been granted, <code>false</code> if one of the permissions has been specifically revoked
			 *
			 * @throws NotFoundException
			 *             if an exact ACE for one of the permission bit masks and SID combination could not be found
			 */
			public boolean isGranted(Acl acl, List<Permission> permission, List<Sid> sids, boolean administrativeMode) throws NotFoundException {
				Assert.notNull(auditLogger, "auditLogger cannot be null");
				final List<AccessControlEntry> aces = acl.getEntries();

				AccessControlEntry firstRejection = null;

				for (Permission p : permission) {
					for (Sid sid : sids) {
						// Attempt to find exact match for this permission mask and SID
						boolean scanNextSid = true;

						for (AccessControlEntry ace : aces) {
							if (((ace.getPermission().getMask() & p.getMask()) == p.getMask()) && ace.getSid().equals(sid)) {
								// Found a matching ACE, so its authorization decision will
								// prevail
								if (ace.isGranting()) {
									// Success
									if (!administrativeMode) {
										auditLogger.logIfNeeded(true, ace);
									}

									return true;
								}

								// Failure for this permission, so stop search
								// We will see if they have a different permission
								// (this permission is 100% rejected for this SID)
								if (firstRejection == null) {
									// Store first rejection for auditing reasons
									firstRejection = ace;
								}

								scanNextSid = false; // helps break the loop

								break; // exit aces loop
							}
						}

						if (!scanNextSid) {
							break; // exit SID for loop (now try next permission)
						}
					}
				}

				if (firstRejection != null) {
					// We found an ACE to reject the request at this point, as no
					// other ACEs were found that granted a different permission
					if (!administrativeMode) {
						auditLogger.logIfNeeded(false, firstRejection);
					}

					return false;
				}

				// No matches have been found so far
				if (acl.isEntriesInheriting() && (acl.getParentAcl() != null)) {
					// We have a parent, so let them try to find a matching ACE
					return acl.getParentAcl().isGranted(permission, sids, false);
				} else {
					// We either have no parent, or we're the uppermost parent
					throw new NotFoundException("Unable to locate a matching ACE for passed permissions and SIDs");
				}
			}
		};
	}
    
	@Bean
    public AclCache aclCache() throws CacheException, IOException {
        return new EhCacheBasedAclCache(ehCacheFactory(), aclPermissionGrantingStrategy(), aclAuthorizationStrategy());
    }
	
    @Bean
    public DefaultPermissionFactory aclPermissionFactory() {
        return new DefaultPermissionFactory();
    }
	
	@Bean
    public LookupStrategy aclLookupStrategy() throws CacheException, IOException {
        BasicLookupStrategy lookupStrategy = new BasicLookupStrategy(beanFactory.getBean(DataSource.class), aclCache(), aclAuthorizationStrategy(), aclPermissionGrantingStrategy());
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
        lookupStrategy.setPermissionFactory(aclPermissionFactory());
        return lookupStrategy;
    }
	
	@Bean
	public MutableAclService aclService() throws CacheException, IOException {

		JdbcMutableAclService aclService = new JdbcMutableAclService(beanFactory.getBean(DataSource.class), aclLookupStrategy(), aclCache());
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
	
	@Bean
    public PermissionEvaluator aclPermissionEvaluator() throws CacheException, IOException {
        return new AclPermissionEvaluator(aclService());
    }
	

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler(){
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setDefaultRolePrefix("");
        try {
			expressionHandler.setPermissionEvaluator(aclPermissionEvaluator());
		} catch (CacheException | IOException e) {
			throw new RuntimeException(e);
		}
        return expressionHandler;
    }

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
		auth
		.authenticationProvider(new AuthenticationProvider(){

			@Override
			public Authentication authenticate(Authentication authentication) throws AuthenticationException {
				return SecurityContextHolder.getContext().getAuthentication();
			}

			@Override
			public boolean supports(Class<?> authentication) {
				return true;
			}
			
		})
		.apply(new UserDetailsAwareConfigurer<AuthenticationManagerBuilder, UserDetailsService>() {

			@Override
			public UserDetailsService getUserDetailsService() {
				return new UserDetailsService() {

					@Override
					public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
						Authentication auth = SecurityContextHolder.getContext().getAuthentication();
						return auth!=null?(UserDetails)auth.getPrincipal():null;
					}

				};
			}
		});
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory=beanFactory;
	}

	@Override
	protected AfterInvocationManager afterInvocationManager() {
		AfterInvocationManager aimOr = super.afterInvocationManager();
		if (aimOr != null) {
			AfterInvocationManager aimW = new AfterInvocationManager() {

				@Override
				public Object decide(Authentication authentication, Object object, Collection<ConfigAttribute> attributes, Object returnedObject) throws AccessDeniedException {
					Object result = null;
					try {
						result = aimOr.decide(authentication, object, attributes, returnedObject);
					} catch (AccessDeniedException ignorar) {}
					return result;
				}

				@Override
				public boolean supports(ConfigAttribute attribute) {
					return aimOr.supports(attribute);
				}

				@Override
				public boolean supports(Class<?> clazz) {
					return aimOr.supports(clazz);
				}

			};
			return aimW;
		}
		return aimOr;
	}
}
