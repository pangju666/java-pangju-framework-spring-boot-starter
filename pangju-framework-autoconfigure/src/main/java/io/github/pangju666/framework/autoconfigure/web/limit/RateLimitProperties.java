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

package io.github.pangju666.framework.autoconfigure.web.limit;

import io.github.pangju666.framework.autoconfigure.web.limit.enums.RateLimitScope;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 速率限制配置属性类
 * <p>
 * 用于配置应用中的速率限制功能。提供了限流实现方式的选择和相应的配置参数。
 * 配置前缀为{@code pangju.web.rate-limit}。
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * pangju:
 *   web:
 *     rate-limit:
 *       # 限流实现类型：RESILIENCE4J 或 REDISSON
 *       type: RESILIENCE4J
 *       # Redisson相关配置（当type为REDISSON时）
 *       redisson:
 *         # Redisson客户端Bean名称
 *         redisson-client-bean-name: redissonClient
 *         # Redis键前缀
 *         key-prefix: rate-limit
 * </pre>
 * </p>
 *
 * @author pangju666
 * @see RateLimitScope
 * @see RateLimiterAutoConfiguration
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "pangju.web.rate-limit")
public class RateLimitProperties {
	/**
	 * 限流实现类型
	 * <p>
	 * 支持两种实现方式：
	 * <ul>
	 *     <li>{@link Type#RESILIENCE4J} - 基于Resilience4j库的内存限流实现，适合单机应用</li>
	 *     <li>{@link Type#REDISSON} - 基于Redisson（Redis）的分布式限流实现，适合分布式应用</li>
	 * </ul>
	 * 默认为RESILIENCE4J。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private Type type = Type.RESILIENCE4J;
	/**
	 * Redisson限流配置
	 * <p>
	 * 当{@link #type}为{@link Type#REDISSON}时，该配置生效。
	 * 包含Redisson客户端的Bean名称和Redis键前缀等配置。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private Redisson redisson = new Redisson();

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Redisson getRedisson() {
		return redisson;
	}

	public void setRedisson(Redisson redisson) {
		this.redisson = redisson;
	}

	/**
	 * 限流实现类型枚举
	 * <p>
	 * 定义了支持的限流实现方式。
	 * </p>
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public enum Type {
		/**
		 * 基于Redisson（Redis）的分布式限流实现
		 * <p>
		 * 适合在分布式系统中使用，多个应用实例共享同一Redis实例进行限流计数。
		 * 支持高并发和分布式场景。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		REDISSON,
		/**
		 * 基于Resilience4j库的内存限流实现
		 * <p>
		 * 适合单机应用或需要简单限流的场景。限流计数存储在应用内存中，
		 * 不同应用实例之间的限流独立计算。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		RESILIENCE4J
	}

	/**
	 * Redisson限流实现配置内部类
	 * <p>
	 * 用于配置Redisson作为限流实现时所需的参数。
	 * </p>
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class Redisson {
		/**
		 * Redisson客户端Bean名称
		 * <p>
		 * 指定Spring容器中Redisson客户端对应的Bean名称。
		 * 如果不指定，框架会使用默认的Redisson Bean。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private String redissonClientBeanName;
		/**
		 * Redis键前缀
		 * <p>
		 * 用于限流计数的Redis键的前缀，便于区分和管理限流相关的Redis键。
		 * 完整的键格式为：{keyPrefix}:{limitName}:{source}
		 * 默认为"rate-limit"。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private String keyPrefix = "rate-limit";

		public String getRedissonClientBeanName() {
			return redissonClientBeanName;
		}

		public void setRedissonClientBeanName(String redissonClientBeanName) {
			this.redissonClientBeanName = redissonClientBeanName;
		}

		public String getKeyPrefix() {
			return keyPrefix;
		}

		public void setKeyPrefix(String keyPrefix) {
			this.keyPrefix = keyPrefix;
		}
	}
}
