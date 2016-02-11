package mx.com.gunix.framework.josso.redis.clients.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class JedisPool extends redis.clients.jedis.JedisPool {

	public JedisPool(GenericObjectPoolConfig poolConfig, String host, int port, int timeout, String password) {
		super(poolConfig, host, port, timeout, "".equals(password.trim()) ? null : password);
	}

}
