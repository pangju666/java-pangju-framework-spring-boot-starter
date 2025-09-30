/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.framework.autoconfigure.data.dynamic.redis;

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
