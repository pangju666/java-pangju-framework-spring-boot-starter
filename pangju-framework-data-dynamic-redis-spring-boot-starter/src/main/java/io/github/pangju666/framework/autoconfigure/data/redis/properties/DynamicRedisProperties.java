package io.github.pangju666.framework.autoconfigure.data.redis.properties;

import io.github.pangju666.framework.autoconfigure.data.redis.enums.RedisSerializerType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = DynamicRedisProperties.PREFIX)
public class DynamicRedisProperties {
	public static final String PREFIX = "spring.data.redis.dynamic";

	private String primary;
	private Map<String, RedisProperties> databases;

	public Map<String, RedisProperties> getDatabases() {
		return databases;
	}

	public void setDatabases(Map<String, RedisProperties> databases) {
		this.databases = databases;
	}

	public String getPrimary() {
		return primary;
	}

	public void setPrimary(String primary) {
		this.primary = primary;
	}

	public static class RedisProperties extends org.springframework.boot.autoconfigure.data.redis.RedisProperties {
		private RedisSerializerType keySerializer = RedisSerializerType.STRING;
		private RedisSerializerType valueSerializer = RedisSerializerType.JAVA;
		private RedisSerializerType hashKeySerializer = RedisSerializerType.STRING;
		private RedisSerializerType hashValueSerializer = RedisSerializerType.JAVA;

		public RedisSerializerType getKeySerializer() {
			return keySerializer;
		}

		public void setKeySerializer(RedisSerializerType keySerializer) {
			this.keySerializer = keySerializer;
		}

		public RedisSerializerType getValueSerializer() {
			return valueSerializer;
		}

		public void setValueSerializer(RedisSerializerType valueSerializer) {
			this.valueSerializer = valueSerializer;
		}

		public RedisSerializerType getHashKeySerializer() {
			return hashKeySerializer;
		}

		public void setHashKeySerializer(RedisSerializerType hashKeySerializer) {
			this.hashKeySerializer = hashKeySerializer;
		}

		public RedisSerializerType getHashValueSerializer() {
			return hashValueSerializer;
		}

		public void setHashValueSerializer(RedisSerializerType hashValueSerializer) {
			this.hashValueSerializer = hashValueSerializer;
		}
	}
}
