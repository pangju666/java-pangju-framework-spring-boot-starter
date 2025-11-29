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

package io.github.pangju666.framework.boot.autoconfigure.data.dynamic.redis;

import io.github.pangju666.framework.data.redis.enums.RedisSerializerType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 动态 Redis 配置属性。
 *
 * <p><strong>前缀</strong>：{@code spring.data.redis.dynamic}</p>
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>配置多个 Redis 数据源，并通过 {@code primary} 指定主数据源。</li>
 *   <li>每个数据源支持序列化器类型配置（键/值与哈希键/值）。</li>
 * </ul>
 *
 * <p><strong>属性映射（含默认值）</strong></p>
 * <ul>
 *   <li>{@code spring.data.redis.dynamic.primary}：主数据源名称，必须存在于 {@code databases} 键集合中。</li>
 *   <li>{@code spring.data.redis.dynamic.databases}：数据源配置映射，至少包含一个条目。</li>
 *   <li>序列化器默认值：
 *     <ul>
 *       <li>{@code key-serializer}：默认 {@code STRING}</li>
 *       <li>{@code value-serializer}：默认 {@code JSON}</li>
 *       <li>{@code hash-key-serializer}：默认 {@code STRING}</li>
 *       <li>{@code hash-value-serializer}：默认 {@code JSON}</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><strong>配置示例</strong></p>
 * <pre>
 * spring:
 *   data:
 *     redis:
 *       dynamic:
 *         primary: redis1
 *         databases:
 *           redis1:
 *             host: localhost
 *             port: 6379
 *             password: password
 *             key-serializer: STRING
 *             value-serializer: JSON
 *             hash-key-serializer: STRING
 *             hash-value-serializer: JSON
 *             database: 0
 *           redis2:
 *             host: localhost
 *             port: 6380
 *             password: password2
 *             key-serializer: STRING
 *             value-serializer: JAVA
 *             hash-key-serializer: STRING
 *             hash-value-serializer: BYTE_ARRAY
 *             database: 1
 * </pre>
 *
 * @author pangju666
 * @see RedisProperties
 * @see DynamicRedisAutoConfiguration
 * @see DynamicRedisRegistrar
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = DynamicRedisProperties.PREFIX)
public class DynamicRedisProperties {
	/**
	 * 配置属性前缀
	 *
	 * @since 1.0.0
	 */
	public static final String PREFIX = "spring.data.redis.dynamic";

	/**
	 * 主Redis数据源名称
	 * <p>
	 * 该值必须对应{@link #databases}中的一个键。
	 * 主数据源的Bean将被标记为主Bean，在自动注入时默认使用。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private String primary;
	/**
	 * Redis数据源配置集合
	 * <p>
	 * key为数据源名称，value为该数据源的配置属性。
	 * 集合不可为空，至少需要配置一个数据源。
	 * </p>
	 *
	 * @since 1.0.0
	 */
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

    /**
     * Redis 数据源配置。
     *
     * <p><strong>概述</strong></p>
     * <ul>
     *   <li>继承 Spring Boot {@link org.springframework.boot.autoconfigure.data.redis.RedisProperties}，并扩展序列化器类型。</li>
     *   <li>可配置键/值与哈希键/值的序列化器，提升兼容性与可读性。</li>
     * </ul>
     *
     * <p><strong>序列化器类型</strong></p>
     * <ul>
     *   <li>{@link RedisSerializerType#STRING}：字符串序列化。</li>
     *   <li>{@link RedisSerializerType#JSON}：JSON 序列化。</li>
     *   <li>{@link RedisSerializerType#JAVA}：Java 对象序列化。</li>
     *   <li>{@link RedisSerializerType#BYTE_ARRAY}：二进制序列化。</li>
     * </ul>
     *
     * @author pangju666
     * @see RedisSerializerType
     * @since 1.0.0
     */
	public static class RedisProperties extends org.springframework.boot.autoconfigure.data.redis.RedisProperties {
		/**
		 * Redis键的序列化类型
		 * <p>
		 * 用于配置Redis键的序列化方式。
		 * 默认值为{@link RedisSerializerType#STRING}
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private RedisSerializerType keySerializer = RedisSerializerType.STRING;
		/**
		 * Redis值的序列化类型
		 * <p>
		 * 用于配置Redis普通key的value序列化方式。
		 * 默认值为{@link RedisSerializerType#JSON}
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private RedisSerializerType valueSerializer = RedisSerializerType.JSON;
		/**
		 * Redis哈希表键的序列化类型
		 * <p>
		 * 用于配置Redis哈希表结构中的键序列化方式。
		 * 默认值为{@link RedisSerializerType#STRING}
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private RedisSerializerType hashKeySerializer = RedisSerializerType.STRING;
		/**
		 * Redis哈希表值的序列化类型
		 * <p>
		 * 用于配置Redis哈希表结构中的值序列化方式。
		 * 默认值为{@link RedisSerializerType#JSON}
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private RedisSerializerType hashValueSerializer = RedisSerializerType.JSON;

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
