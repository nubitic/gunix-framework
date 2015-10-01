package mx.com.gunix.framework.config;

import javax.sql.DataSource;

import mx.com.gunix.framework.security.domain.UsuarioMapperInterceptor;

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
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
@MapperScan({ "mx.com.gunix.domain.persistence", "mx.com.gunix.framework.security.domain.persistence", "mx.com.gunix.framework.activiti.persistence.entity" })
public class PersistenceConfig {
	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	@Bean
	public DataSource dataSource() {
		HikariConfig config = new HikariConfig();
		
		config.setAutoCommit(false);
		config.setMaximumPoolSize(15);
		config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
		
		if(Boolean.valueOf(System.getenv("DB_USE_SSL"))) {
			config.addDataSourceProperty("ssl", "true");
			config.addDataSourceProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");	
		}
				
		config.addDataSourceProperty("password", System.getenv("DB_PASSWORD"));
		config.addDataSourceProperty("user", System.getenv("DB_USER"));
		config.addDataSourceProperty("databaseName", System.getenv("DB_NAME"));
		config.addDataSourceProperty("serverName", System.getenv("DB_SERVER_NAME"));
		config.addDataSourceProperty("prepareThreshold", "1");
		config.addDataSourceProperty("preparedStatementCacheQueries", "1024");
		config.addDataSourceProperty("preparedStatementCacheSizeMiB", "20");		

		return new HikariDataSource(config);
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}

	@Bean
	public SqlSessionFactoryBean sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
		sessionFactory.setDataSource(dataSource());
		sessionFactory.setDatabaseIdProvider(new VendorDatabaseIdProvider());
		sessionFactory.setPlugins(new Interceptor[] { new UsuarioMapperInterceptor() });
		sessionFactory.setMapperLocations(resourcePatternResolver.getResources("classpath*:/mx/com/gunix/**/*Mapper.xml"));
		sessionFactory.setTypeAliases(new Class<?>[] { ByteArrayRefTypeHandlerAlias.class });
		sessionFactory.setTypeHandlers(new TypeHandler[] { new ByteArrayRefTypeHandlerAlias(), new IbatisVariableTypeHandler() });
		return sessionFactory;
	}

	@Bean
	public PersistentTokenRepository persistentTokenRepository() {
		JdbcTokenRepositoryImpl ptr = new JdbcTokenRepositoryImpl();
		ptr.setDataSource(dataSource());
		return ptr;
	}

	@Alias("ByteArrayRefTypeHandler")
	public static class ByteArrayRefTypeHandlerAlias extends ByteArrayRefTypeHandler {

	}

	@MappedTypes(VariableType.class)
	@MappedJdbcTypes(JdbcType.VARCHAR)
	public static class IbatisVariableTypeHandler extends org.activiti.engine.impl.db.IbatisVariableTypeHandler {

	}
}
