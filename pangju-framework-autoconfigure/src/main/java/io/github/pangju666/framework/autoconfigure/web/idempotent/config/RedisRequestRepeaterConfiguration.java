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

package io.github.pangju666.framework.autoconfigure.web.idempotent.config;

import io.github.pangju666.framework.autoconfigure.web.idempotent.IdempotentProperties;
import io.github.pangju666.framework.autoconfigure.web.idempotent.validator.IdempotentValidator;
import io.github.pangju666.framework.autoconfigure.web.idempotent.validator.impl.RedisIdempotentValidator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;

/**
 * 基于 Redis 的幂等性校验配置类。
 * <p>
 * 当幂等性校验的类型配置为 {@code REDIS} 时（即 {@code pangju.web.idempotent.type=REDIS}），
 * 自动配置一个基于 Redis 的 {@link RedisIdempotentValidator} 幂等性校验器实例。
 * 该实现利用 Redis 的分布式存储特性，在分布式环境下提供幂等性支持。
 * </p>
 *
 * <p>配置特性：</p>
 * <ul>
 *     <li>仅在类路径中存在 {@link RedisOperations} 时生效，确保 Redis 相关依赖已加载。</li>
 *     <li>当 {@code pangju.web.idempotent.type} 配置为 {@code REDIS} 时激活。</li>
 *     <li>避免重复注册：当上下文中不存在 {@link IdempotentValidator} Bean 时才会注册。</li>
 * </ul>
 *
 * <p>适用场景：</p>
 * <ul>
 *     <li>分布式系统：适用于需要分布式幂等性校验的场景。</li>
 *     <li>高并发环境：利用 Redis 的高性能特点提供快速响应。</li>
 * </ul>
 *
 * <p>配置示例：</p>
 * <pre>
 * pangju.web.idempotent.type=REDIS
 * pangju.web.idempotent.redis.key-prefix=idempotent
 * pangju.web.idempotent.redis.redis-template-bean-name=customRedisTemplate
 * </pre>
 *
 * @author pangju666
 * @see RedisIdempotentValidator
 * @see IdempotentValidator
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisOperations.class)
@ConditionalOnProperty(prefix = "pangju.web.idempotent", value = "type", havingValue = "REDIS")
public class RedisRequestRepeaterConfiguration {
	/**
	 * 创建基于 Redis 的幂等性校验器。
	 * <p>
	 * 当上下文中没有已定义的 {@link IdempotentValidator} Bean 时，自动注册
	 * {@link RedisIdempotentValidator} 实例。
	 * </p>
	 *
	 * <p>校验器的核心功能：</p>
	 * <ul>
	 *     <li>基于 Redis 的键值存储实现分布式幂等性校验。</li>
	 *     <li>支持动态配置 Redis 键的前缀和使用的 RedisTemplate。</li>
	 * </ul>
	 *
	 * @param properties  幂等性配置属性 {@link IdempotentProperties}，包含 Redis 相关配置。
	 * @param beanFactory Spring 的 {@link BeanFactory}，用于动态获取 RedisTemplate 实例。
	 * @return {@link RedisIdempotentValidator} 实例，用于幂等性校验。
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(IdempotentValidator.class)
	@Bean
	public RedisIdempotentValidator redisIdempotentValidator(IdempotentProperties properties, BeanFactory beanFactory) {
		return new RedisIdempotentValidator(properties, beanFactory);
	}
}
