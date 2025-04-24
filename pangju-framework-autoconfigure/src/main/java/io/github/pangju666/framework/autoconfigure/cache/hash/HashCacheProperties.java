package io.github.pangju666.framework.autoconfigure.cache.hash;

import io.github.pangju666.framework.autoconfigure.cache.hash.enums.HashCacheType;
import io.github.pangju666.framework.autoconfigure.data.redis.enums.RedisSerializerType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pangju.cache.hash")
public class HashCacheProperties {
	private HashCacheType type = HashCacheType.REDIS;
	private Redis redis = new Redis();

	public HashCacheType getType() {
		return type;
	}

	public void setType(HashCacheType type) {
		this.type = type;
	}

	public Redis getRedis() {
		return redis;
	}

	public void setRedis(Redis redis) {
		this.redis = redis;
	}

	public static class Redis {
		private boolean cacheNullValues = true;
		private String keyPrefix;
		private boolean useKeyPrefix = true;
		private RedisSerializerType valueSerializer = RedisSerializerType.JAVA;

		public boolean isCacheNullValues() {
			return cacheNullValues;
		}

		public void setCacheNullValues(boolean cacheNullValues) {
			this.cacheNullValues = cacheNullValues;
		}

		public String getKeyPrefix() {
			return keyPrefix;
		}

		public void setKeyPrefix(String keyPrefix) {
			this.keyPrefix = keyPrefix;
		}

		public boolean isUseKeyPrefix() {
			return useKeyPrefix;
		}

		public void setUseKeyPrefix(boolean useKeyPrefix) {
			this.useKeyPrefix = useKeyPrefix;
		}

		public RedisSerializerType getValueSerializer() {
			return valueSerializer;
		}

		public void setValueSerializer(RedisSerializerType valueSerializer) {
			this.valueSerializer = valueSerializer;
		}
	}
}
