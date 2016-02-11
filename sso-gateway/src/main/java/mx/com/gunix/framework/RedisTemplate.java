package mx.com.gunix.framework;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.core.ConfigurableObjectInputStream;
import org.springframework.core.NestedIOException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisTemplate<T> {
	private JedisPool connectionFactory;

	public JedisPool getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(JedisPool connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public void set(String key, T value, int i, TimeUnit milliseconds) {
		final byte[] rawValue = toRaw(value);
		final byte[] rawKey = toRaw(key);
		Jedis connection = null;
		try {
			connection = getConnection();
			connection.set(rawKey, rawValue);
		} finally {
			if (connection != null)
				connection.close();
		}
	}

	private Jedis getConnection() {
		Jedis connection = connectionFactory.getResource();
		connection.select(0);
		return connection;
	}

	private byte[] toRaw(Object value) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024);
		try {
			serialize(value, byteStream);
			return byteStream.toByteArray();
		} catch (Throwable ex) {
			throw new RuntimeException("Failed to serialize object", ex);
		}
	}

	private void serialize(Object value, ByteArrayOutputStream byteStream) throws IOException {
		if (!(value instanceof Serializable)) {
			throw new IllegalArgumentException(getClass().getSimpleName() + " requires a Serializable payload " + "but received an object of type [" + value.getClass().getName() + "]");
		}
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream);
		objectOutputStream.writeObject(value);
		objectOutputStream.flush();
	}

	@SuppressWarnings("unchecked")
	public T get(String key) {
		final byte[] rawKey = toRaw(key);
		T value = null;
		Jedis connection = null;
		try {
			connection = getConnection();
			value = (T) deserealize(connection.get(rawKey));
		} finally {
			if (connection != null)
				connection.close();
		}
		return value;
	}

	private Object deserealize(byte[] bs) {
		ByteArrayInputStream byteStream = new ByteArrayInputStream(bs);
		try {
			return deserialize(byteStream);
		} catch (Throwable ex) {
			throw new RuntimeException("Failed to deserialize payload", ex);
		}
	}

	@SuppressWarnings({ "resource" })
	private Object deserialize(ByteArrayInputStream bis) throws IOException {
		ObjectInputStream objectInputStream = new ConfigurableObjectInputStream(bis, getClass().getClassLoader());
		try {
			return objectInputStream.readObject();
		} catch (ClassNotFoundException ex) {
			throw new NestedIOException("Failed to deserialize object type", ex);
		}
	}

	public Set<String> keys(String redisKeyPattern) {
		final byte[] rawKey = toRaw(redisKeyPattern);
		Set<byte[]> keys = null;

		Jedis connection = null;
		try {
			connection = getConnection();
			keys = connection.keys(rawKey);
		} finally {
			if (connection != null)
				connection.close();
		}

		Set<String> keysString = new HashSet<String>();
		if (keys != null) {
			for (byte[] key : keys) {
				keysString.add((String) deserealize(key));
			}
		}

		return keysString;
	}

	public void delete(String key) {
		final byte[] rawKey = toRaw(key);

		Jedis connection = null;
		try {
			connection = getConnection();
			connection.del(rawKey);
		} finally {
			if (connection != null)
				connection.close();
		}
	}
}
