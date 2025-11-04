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

package io.github.pangju666.framework.boot.autoconfigure.web.idempotent;

import io.github.pangju666.framework.boot.web.idempotent.annotation.Idempotent;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 用于配置幂等功能的属性类。
 * <p>
 * 此类支持幂等功能的两种实现方式：
 * <ul>
 *     <li>{@link Type#EXPIRE_MAP}：基于内存的本地过期映射，用于较轻量的幂等操作场景。</li>
 *     <li>{@link Type#REDIS}：基于 Redis 的分布式幂等实现，适合分布式环境下的幂等操作。</li>
 * </ul>
 * 通过配置前缀 <b>"pangju.web.idempotent"</b> 对其进行自定义。
 * <p>
 * 可配置的属性包括：
 * <ul>
 *     <li>type：指定幂等类型，默认为 {@link Type#EXPIRE_MAP}。</li>
 *     <li>redis：当使用 Redis 方式时，该配置节点允许设置 Redis 的模板 Bean 名称以及键前缀。</li>
 * </ul>
 * <p>
 * 示例配置：
 * <pre>
 * pangju.web.idempotent.type=REDIS
 * pangju.web.idempotent.redis.redis-template-bean-name=myRedisTemplate
 * pangju.web.idempotent.redis.key-prefix=myKeyPrefix
 * </pre>
 * 此类与 {@link Idempotent} 注解和 {@link IdempotentAutoConfiguration} 配合使用，
 * 实现幂等验证功能的自动化管理。
 *
 * @author pangju666
 * @see Idempotent
 * @see IdempotentAutoConfiguration
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "pangju.web.idempotent")
public class IdempotentProperties {
	/**
	 * 幂等功能的实现类型。
	 *
	 * @since 1.0.0
	 */
	private Type type = Type.EXPIRE_MAP;
	/**
	 * Redis 的相关配置。
	 *
	 * @since 1.0.0
	 */
	private Redis redis = new Redis();

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Redis getRedis() {
		return redis;
	}

	public void setRedis(Redis redis) {
		this.redis = redis;
	}

	/**
	 * 幂等实现方式的枚举类型。
	 */
	public enum Type {
		/**
		 * Redis 实现分布式幂等。
		 */
		REDIS,
		/**
		 * 本地内存映射方式实现幂等。
		 */
		EXPIRE_MAP
	}

	/**
	 * Redis 相关配置定义
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class Redis {
		/**
		 * 指定 Redis 模板 Bean 的名称，用于幂等操作。
		 *
		 * @since 1.0.0
		 */
		private String redisTemplateBeanName;
		/**
		 * Redis 键的默认前缀，默认值为 "idempotent"。
		 *
		 * @since 1.0.0
		 */
		private String keyPrefix = "idempotent";

		public String getRedisTemplateBeanName() {
			return redisTemplateBeanName;
		}

		public void setRedisTemplateBeanName(String redisTemplateBeanName) {
			this.redisTemplateBeanName = redisTemplateBeanName;
		}

		public String getKeyPrefix() {
			return keyPrefix;
		}

		public void setKeyPrefix(String keyPrefix) {
			this.keyPrefix = keyPrefix;
		}
	}
}
