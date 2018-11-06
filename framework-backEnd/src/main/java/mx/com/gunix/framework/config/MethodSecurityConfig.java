package mx.com.gunix.framework.config;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.ibatis.cache.CacheException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.PermissionCacheOptimizer;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.access.intercept.AfterInvocationManager;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionFactory;
import org.springframework.security.acls.domain.EhCacheBasedAclCache;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.AclService;
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

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

@Configuration
@Import(PersistenceConfig.class)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, proxyTargetClass = true)
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
    public PermissionEvaluator aclPermissionEvaluator(AclService aclService) throws CacheException, IOException {
        return new AclPermissionEvaluator(aclService);
    }
	

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler(){
		DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler() {
			private PermissionCacheOptimizer permissionCacheOptimizer = null;
			private Field methodInvocationField;
			@Override
			public void setPermissionCacheOptimizer(PermissionCacheOptimizer permissionCacheOptimizer) {
				this.permissionCacheOptimizer = permissionCacheOptimizer;
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public Object filter(Object filterTarget, Expression filterExpression, EvaluationContext ctx) {
				MethodSecurityExpressionOperations rootObject = (MethodSecurityExpressionOperations) ctx.getRootObject().getValue();
				if(methodInvocationField==null){
					try {
						methodInvocationField = ctx.getClass().getDeclaredField("mi");
						methodInvocationField.setAccessible(true);
					} catch (NoSuchFieldException | SecurityException e) {
						throw new RuntimeException(e);
					}
				}
				
				final boolean debug = logger.isDebugEnabled();
				List retainList;

				if (debug) {
					logger.debug("Filtering with expression: " + filterExpression.getExpressionString());
				}

				if (filterTarget instanceof Collection) {
					Collection collection = (Collection) filterTarget;

					if (debug) {
						logger.debug("Filtering collection with " + collection.size() + " elements");
					}
					
					retainList = doFilter((Collection) filterTarget, rootObject, filterExpression, ctx);
					
					if (debug) {
						logger.debug("Retaining elements: " + retainList);
					}

					collection.clear();
					collection.addAll(retainList);

					return filterTarget;
				}

				if (filterTarget.getClass().isArray()) {
					Object[] array = (Object[]) filterTarget;

					if (debug) {
						logger.debug("Filtering array with " + array.length + " elements");
					}

					retainList = doFilter(Arrays.asList(array), rootObject, filterExpression, ctx);

					if (debug) {
						logger.debug("Retaining elements: " + retainList);
					}

					Object[] filtered = (Object[]) Array.newInstance(filterTarget.getClass().getComponentType(), retainList.size());
					for (int i = 0; i < retainList.size(); i++) {
						filtered[i] = retainList.get(i);
					}

					return filtered;
				}

				throw new IllegalArgumentException("Filter target must be a collection or array type, but was " + filterTarget);
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			private List doFilter(Collection filterTarget, MethodSecurityExpressionOperations rootObject, Expression filterExpression, EvaluationContext ctx) {
				if (permissionCacheOptimizer != null) {
					permissionCacheOptimizer.cachePermissionsFor(rootObject.getAuthentication(), filterTarget);
				}
				
				return (List) ((Collection) filterTarget)
										.parallelStream()
										.filter(filterObject->{
													EvaluationContext newCtx = null;
													try {
														MethodInvocation mi = (MethodInvocation) methodInvocationField.get(ctx);
														newCtx = createEvaluationContext(rootObject.getAuthentication(), mi);
														((MethodSecurityExpressionOperations) newCtx.getRootObject().getValue()).setFilterObject(filterObject);
													} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
														throw new RuntimeException(e);
													}
													return ExpressionUtils.evaluateAsBoolean(filterExpression, newCtx);
												})
										.collect(Collectors.toList());
			}

		};
        expressionHandler.setDefaultRolePrefix("");
        try {
			expressionHandler.setPermissionEvaluator(aclPermissionEvaluator(beanFactory.getBean(AclService.class)));
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
