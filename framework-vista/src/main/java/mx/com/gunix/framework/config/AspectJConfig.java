package mx.com.gunix.framework.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@ComponentScan({"mx.com.gunix.framework.config.aspects","mx.com.gunix.config.aspects"})
@EnableAspectJAutoProxy
public class AspectJConfig {
	
}
