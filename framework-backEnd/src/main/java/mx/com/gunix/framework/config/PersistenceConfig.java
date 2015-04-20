package mx.com.gunix.framework.config;

import javax.annotation.Resource;
import javax.sql.DataSource;

import mx.com.gunix.framework.domain.UsuarioMapperInterceptor;

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
@MapperScan({"mx.com.gunix.domain.persistence","mx.com.gunix.framework.domain.persistence"})
public class PersistenceConfig {
	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	
	@Bean
	@Resource(name="jdbc/gunixDS")
	public DataSource dataSource() {
	    JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
	    dsLookup.setResourceRef(true);
	    DataSource dataSource = dsLookup.getDataSource("java:comp/env/jdbc/gunixDS");
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
		sessionFactory.setMapperLocations(resourcePatternResolver.getResources("classpath*:/mx/com/gunix/**/domain/**/*Mapper.xml"));
		return sessionFactory;
	}
}
