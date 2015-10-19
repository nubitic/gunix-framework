package mx.com.gunix.framework.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("mx.com.gunix.adminapp.service")
@MapperScan("mx.com.gunix.adminapp.domain.persistence")
public class AdminAppServicesConfig {

}
