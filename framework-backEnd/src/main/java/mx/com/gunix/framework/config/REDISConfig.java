package mx.com.gunix.framework.config;

import java.io.File;

import mx.com.gunix.framework.processes.domain.ProgressUpdate;
import mx.com.gunix.framework.service.EmbeddedRedisManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class REDISConfig {
	@Bean
	JedisConnectionFactory jedisConnectionFactory() {
		JedisConnectionFactory jcf = new JedisConnectionFactory();
		jcf.setUsePool(true);
		String redisHostName = System.getenv("REDIS_HOST");
		if (redisHostName != null && !redisHostName.equals("")) {
			jcf.setHostName(redisHostName);
		} else {
			String redisHome = System.getenv("EMBEDDED_REDIS_HOME");
			if (redisHome == null || "".equals(redisHome)) {
				redisHome = System.getProperty("user.home") + File.separator + "redis";
			}
			EmbeddedRedisManager.start(redisHome);
		}
		String redisPort = System.getenv("REDIS_PORT");
		if (redisPort != null && !redisPort.equals("")) {
			jcf.setPort(Integer.parseInt(redisPort));
		}
		String redisPassword = System.getenv("REDIS_PASSWORD");
		if (redisPassword != null && !redisPassword.equals("")) {
			jcf.setPassword(redisPassword);
		}

		return jcf;
	}

	@Bean
	RedisTemplate<String, ProgressUpdate> redisProgressUpdateTemplate() {
		final RedisTemplate<String, ProgressUpdate> template = new RedisTemplate<String, ProgressUpdate>();
		template.setConnectionFactory(jedisConnectionFactory());
		return template;
	}
}
