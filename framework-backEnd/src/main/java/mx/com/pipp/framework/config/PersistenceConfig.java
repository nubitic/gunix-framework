package mx.com.pipp.framework.config;

import javax.annotation.Resource;
import javax.sql.DataSource;

import mx.com.pipp.framework.domain.UsuarioMapperInterceptor;

import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@MapperScan({"mx.com.pipp.domain.persistence","mx.com.pipp.framework.domain.persistence"})
public class PersistenceConfig {
	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	
	@Bean
	@Resource(name="jdbc/pippDS")
	public DataSource dataSource() {
	    JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
	    dsLookup.setResourceRef(true);
	    DataSource dataSource = dsLookup.getDataSource("java:comp/env/jdbc/pippDS");
	    return dataSource;
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}

	@Bean
	public SqlSessionFactoryBean sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
		sessionFactory.setDataSource(dataSource());
		sessionFactory.setPlugins(new Interceptor[]{new UsuarioMapperInterceptor()});
		sessionFactory.setMapperLocations(resourcePatternResolver.getResources("classpath*:/mx/com/pipp/**/domain/**/*Mapper.xml"));
		return sessionFactory;
	}
}
