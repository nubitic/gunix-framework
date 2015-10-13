package mx.com.gunix.framework.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import mx.com.gunix.framework.security.RolAccessDecisionVoter;
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
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.ExpressionBasedAnnotationAttributeFactory;
import org.springframework.security.access.expression.method.ExpressionBasedPostInvocationAdvice;
import org.springframework.security.access.expression.method.ExpressionBasedPreInvocationAdvice;
import org.springframework.security.access.intercept.AfterInvocationProviderManager;
import org.springframework.security.access.intercept.aspectj.AspectJMethodSecurityInterceptor;
import org.springframework.security.access.method.DelegatingMethodSecurityMetadataSource;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.access.prepost.PostInvocationAdviceProvider;
import org.springframework.security.access.prepost.PreInvocationAuthorizationAdviceVoter;
import org.springframework.security.access.prepost.PrePostAnnotationSecurityMetadataSource;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionFactory;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.domain.EhCacheBasedAclCache;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
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

@Configuration
@Import(PersistenceConfig.class)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration implements BeanFactoryAware{
	public static String ACL_CACHE_NAME = "aclCache";
	public static final String ACL_ADMIN_ROLE = "ACL_ADMIN";
	private BeanFactory beanFactory;

	
	@Bean(name="expressionHandler")
    public DefaultMethodSecurityExpressionHandler aclExpressionHandler() throws CacheException, IOException {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(aclPermissionEvaluator());
        return expressionHandler;

    }
	
    @Bean(name = "cacheManager")
    public CacheManager ehCacheManager() throws IOException {
        //CacheManager manager = CacheManager.getCacheManager(CacheManager.DEFAULT_NAME);
        //if(manager!=null){
        //    return manager;
        //}
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
        return new DefaultPermissionGrantingStrategy(aclAuditLogger());
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
	
    @Bean
    public ExpressionBasedAnnotationAttributeFactory expressionBasedAnnotationAttributeFactory() throws CacheException, IOException {
        return new ExpressionBasedAnnotationAttributeFactory(aclExpressionHandler());
    }
	
    @Bean
    public PrePostAnnotationSecurityMetadataSource prePostAnnotationSecurityMetadataSource() throws CacheException, IOException {
        return new PrePostAnnotationSecurityMetadataSource(expressionBasedAnnotationAttributeFactory());
    }
	
    @Bean(name = "delegatingMethodSecurityMetadataSource")
    public DelegatingMethodSecurityMetadataSource delegatingMethodSecurityMetadataSource() throws CacheException, IOException {
        List<MethodSecurityMetadataSource> methodSecurityMetadateSources = new ArrayList<MethodSecurityMetadataSource>();
        methodSecurityMetadateSources.add(prePostAnnotationSecurityMetadataSource());

        return new DelegatingMethodSecurityMetadataSource(methodSecurityMetadateSources);
    }
	
    @Bean
    public ExpressionBasedPostInvocationAdvice expressionBasedPostInvocationAdvice() throws CacheException, IOException {
        return new ExpressionBasedPostInvocationAdvice(aclExpressionHandler());
    }
    
    @Bean
    public PostInvocationAdviceProvider postInvocationAdviceProvider() throws CacheException, IOException {
        return new PostInvocationAdviceProvider(expressionBasedPostInvocationAdvice());
    }
    
    @SuppressWarnings("unchecked")
    @Bean
    public AfterInvocationProviderManager afterInvocationProviderManager() throws CacheException, IOException {

        @SuppressWarnings("rawtypes")
        List list = new ArrayList();
        list.add(postInvocationAdviceProvider());


        AfterInvocationProviderManager mgmt = new AfterInvocationProviderManager();
        mgmt.setProviders(list);

        return mgmt;
    }
    
    @Bean
    public AspectJMethodSecurityInterceptor aspectJMethodSecurityInterceptor() throws CacheException, IOException, Exception{
        
        AspectJMethodSecurityInterceptor interceptor = new AspectJMethodSecurityInterceptor();
        interceptor.setAccessDecisionManager(accessDecisionManager());
        interceptor.setAuthenticationManager(authenticationManager());
        interceptor.setSecurityMetadataSource(delegatingMethodSecurityMetadataSource());
        interceptor.setAfterInvocationManager(afterInvocationProviderManager());
        interceptor.afterPropertiesSet();
        return interceptor;        
    }

	@Override
	protected AccessDecisionManager accessDecisionManager() {
		List<AccessDecisionVoter<? extends Object>> voters = new ArrayList<AccessDecisionVoter<? extends Object>>();
    	ExpressionBasedPreInvocationAdvice expressionAdvice = new ExpressionBasedPreInvocationAdvice();
		try {
			expressionAdvice.setExpressionHandler(aclExpressionHandler());
		} catch (CacheException | IOException e) {
			throw new RuntimeException(e);
		}
    	voters.add(new PreInvocationAuthorizationAdviceVoter(expressionAdvice));
    	voters.add(new AuthenticatedVoter());
        voters.add(new RolAccessDecisionVoter());
        return new UnanimousBased(voters);
    }

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
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
}
