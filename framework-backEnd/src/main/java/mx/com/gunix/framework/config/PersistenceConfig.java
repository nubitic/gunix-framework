package mx.com.gunix.framework.config;

import javax.sql.DataSource;

import org.activiti.engine.impl.persistence.ByteArrayRefTypeHandler;
import org.activiti.engine.impl.variable.VariableType;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.type.Alias;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.hunteron.core.Context;

import mx.com.gunix.framework.security.domain.persistence.UsuarioMapperInterceptor;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@MapperScan({ "mx.com.gunix.domain.persistence", "mx.com.gunix.framework.security.domain.persistence", "mx.com.gunix.framework.activiti.persistence.entity", "mx.com.gunix.framework.token.persistence" })
@Import(PersistenceCustomization.class)
public class PersistenceConfig {
	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	
	@Bean
	@Primary
	public PlatformTransactionManager transactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean
	@Primary
	public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) throws Exception {
		SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
		sessionFactory.setDataSource(dataSource);
		sessionFactory.setDatabaseIdProvider(new VendorDatabaseIdProvider());
		sessionFactory.setPlugins(new Interceptor[] { new UsuarioMapperInterceptor() });

		Resource[] appResources = resourcePatternResolver.getResources("classpath*:/mx/com/gunix/domain/persistence/**/*Mapper.xml");
		Resource[] activitiAppResources = resourcePatternResolver.getResources("classpath*:/mx/com/gunix/framework/activiti/persistence/entity/*Mapper.xml");
		Resource[] resources = appResources != null ? appResources : new Resource[] {};

		if (appResources != null && appResources.length > 0) {
			resources = new Resource[appResources.length + activitiAppResources.length];
			System.arraycopy(activitiAppResources, 0, resources, 0, activitiAppResources.length);
			System.arraycopy(appResources, 0, resources, activitiAppResources.length, appResources.length);
		} else {
			resources = activitiAppResources;
		}
		
		if (Boolean.valueOf(Context.STANDALONE_APP.get())) {
			Resource[] adminAppResources = resourcePatternResolver.getResources("classpath*:/mx/com/gunix/adminapp/domain/persistence/*Mapper.xml");
			Resource[] securityAppResources = resourcePatternResolver.getResources("classpath*:/mx/com/gunix/framework/security/domain/*Mapper.xml");

			Resource[] finalResources = new Resource[resources.length + adminAppResources.length + securityAppResources.length];
			System.arraycopy(adminAppResources, 0, finalResources, 0, adminAppResources.length);
			System.arraycopy(resources, 0, finalResources, adminAppResources.length, resources.length);
			System.arraycopy(securityAppResources, 0, finalResources, resources.length + adminAppResources.length, securityAppResources.length);

			sessionFactory.setMapperLocations(finalResources);
		} else {
			sessionFactory.setMapperLocations(resources);
		}
		
		sessionFactory.setTypeAliases(new Class<?>[] { ByteArrayRefTypeHandlerAlias.class });
		sessionFactory.setTypeHandlers(new TypeHandler[] { new ByteArrayRefTypeHandlerAlias(), new IbatisVariableTypeHandler() });
		
		return sessionFactory;
	}
	
	@Alias("ByteArrayRefTypeHandler")
	public static class ByteArrayRefTypeHandlerAlias extends ByteArrayRefTypeHandler {

	}

	@MappedTypes(VariableType.class)
	@MappedJdbcTypes(JdbcType.VARCHAR)
	public static class IbatisVariableTypeHandler extends org.activiti.engine.impl.db.IbatisVariableTypeHandler {

	}
}
