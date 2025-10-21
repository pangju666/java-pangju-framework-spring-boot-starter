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

package io.github.pangju666.framework.autoconfigure.web.idempotent.validator.impl;

import io.github.pangju666.framework.autoconfigure.web.idempotent.IdempotentProperties;
import io.github.pangju666.framework.autoconfigure.web.idempotent.annotation.Idempotent;
import io.github.pangju666.framework.autoconfigure.web.idempotent.validator.IdempotentValidator;
import io.github.pangju666.framework.data.redis.pool.RedisConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 基于 Redis 的幂等验证器实现。
 * <p>
 * 该类通过使用 Redis 作为存储介质，实现请求幂等性的验证和处理。
 * 在指定的时间间隔内，基于唯一的请求键判断请求是否重复。
 * </p>
 *
 * <p>
 * 功能特性：
 * <ul>
 *     <li>使用 Redis 的键值存储（`setIfAbsent`）判断请求是否重复。</li>
 *     <li>支持动态设置 Redis 键的存储前缀（可从配置中加载）。</li>
 *     <li>支持指定请求键的自动过期时间及时间单位。</li>
 *     <li>幂等验证失败的场景（如重复请求）会返回失败校验结果。</li>
 * </ul>
 * </p>
 *
 * <p>
 * 使用场景：
 * <ul>
 *     <li>适用于分布式环境，保证多个微服务节点下的幂等性校验。</li>
 *     <li>当请求需要较长时间处理或存在高并发场景时，可通过此方案避免重复提交或重复处理。</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see Idempotent
 * @see IdempotentValidator
 * @see IdempotentProperties
 * @since 1.0.0
 */
public class RedisIdempotentValidator implements IdempotentValidator {
	/**
	 * 幂等性功能相关的配置信息。
	 * <p>
	 * 该字段用于读取和管理幂等功能的配置信息，包括 Redis 的键前缀、
	 * RedisTemplate 配置等内容，支持灵活的幂等性功能。
	 * </p>
	 *
	 * @see IdempotentProperties
	 * @since 1.0.0
	 */
	private final IdempotentProperties properties;
	/**
	 * RedisTemplate 实例，用于与 Redis 交互。
	 * <p>
	 * 该字段通过 RedisTemplate 提供对 Redis 的操作能力，用于存储幂等性验证的请求键，
	 * 实现幂等性校验逻辑（如设置键值，自动过期管理，删除键等）。
	 * </p>
	 *
	 * @see org.springframework.data.redis.core.RedisTemplate
	 * @since 1.0.0
	 */
	private final RedisTemplate<String, Object> redisTemplate;

	/**
	 * 构造方法，根据配置初始化 Redis 和模板。
	 * <p>
	 * 如果配置了 Redis 模板 Bean 名称，则使用指定的模板实例；
	 * 否则，使用默认名称为 "redisTemplate" 的 Redis 模板。
	 * </p>
	 *
	 * @param properties 幂等相关的配置信息 {@link IdempotentProperties}。
	 * @param beanFactory Spring 的 {@link BeanFactory}，用于获取 RedisTemplate Bean。
	 */
	@SuppressWarnings("unchecked")
	public RedisIdempotentValidator(IdempotentProperties properties, BeanFactory beanFactory) {
		this.properties = properties;
		if (StringUtils.isNotBlank(properties.getRedis().getRedisTemplateBeanName())) {
			this.redisTemplate = beanFactory.getBean(properties.getRedis().getRedisTemplateBeanName(), RedisTemplate.class);
		} else {
			this.redisTemplate = beanFactory.getBean("redisTemplate", RedisTemplate.class);
		}
	}

	/**
	 * 验证请求是否为重复提交。
	 * <p>
	 * 基于 Redis 的键值存储，通过 {@link RedisTemplate#opsForValue()} 检查指定的键是否已存在，
	 * 如果不存在则将键存储到 Redis，同时设置过期时间；如果已存在，则返回验证失败。
	 * </p>
	 * <p>
	 * 支持以下功能：
	 * <ul>
	 *     <li>动态拼接 Redis 键的前缀。</li>
	 *     <li>根据 {@link Idempotent} 注解设定的间隔时间和时间单位完成 Redis 键的过期控制。</li>
	 * </ul>
	 * </p>
	 *
	 * @param key 请求的唯一标识，作为幂等性校验的关键字段。
	 * @param repeat {@link Idempotent} 注解，提供幂等相关的配置信息（如过期时间等）。
	 * @return 如果请求未重复且验证成功，返回 {@code true}；否则返回 {@code false}。
	 */
	@Override
	public boolean validate(String key, Idempotent repeat) {
		String repeatKey = key;
		if (StringUtils.isNotBlank(properties.getRedis().getKeyPrefix())) {
			repeatKey = properties.getRedis().getKeyPrefix() + RedisConstants.REDIS_PATH_DELIMITER + repeatKey;
		}
		Boolean acquired = redisTemplate.opsForValue().setIfAbsent(repeatKey, true, repeat.interval(), repeat.timeUnit());
		return !Boolean.FALSE.equals(acquired);
	}

	/**
	 * 移除指定请求的幂等性记录。
	 * <p>
	 * 通过 Redis 的 {@link RedisTemplate#delete(Object)} 方法，根据唯一键删除幂等性记录。
	 * 适用于未完成有效期就被特殊情况要求手动清理记录的场景。
	 * </p>
	 *
	 * @param key 请求的唯一标识。
	 * @param repeat {@link Idempotent} 注解，提供幂等性相关的配置信息。
	 */
	@Override
	public void remove(String key, Idempotent repeat) {
		redisTemplate.delete(key);
	}
}
