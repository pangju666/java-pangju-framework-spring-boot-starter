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

package io.github.pangju666.framework.boot.autoconfigure.web.idempotent.config;

import io.github.pangju666.framework.boot.autoconfigure.web.idempotent.IdempotentProperties;
import io.github.pangju666.framework.boot.web.idempotent.validator.IdempotentValidator;
import io.github.pangju666.framework.boot.web.idempotent.validator.impl.RedisIdempotentValidator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

/**
 * 基于 Redis 的幂等校验自动配置
 * <p>
 * 当选择 {@code REDIS} 实现时自动注册 {@link RedisIdempotentValidator}，
 * 依赖 {@link RedisOperations}，在分布式环境下提供幂等校验能力。
 * </p>
 * <p><b>生效条件</b></p>
 * <ul>
 *   <li>类路径存在 {@link RedisOperations}（{@link ConditionalOnClass}）。</li>
 *   <li>属性 {@code pangju.web.idempotent.type} 为 {@code REDIS}（{@link ConditionalOnProperty}）。</li>
 *   <li>上下文中不存在 {@link IdempotentValidator} Bean（{@link ConditionalOnMissingBean}）。</li>
 * </ul>
 * <p><b>行为说明</b></p>
 * <ul>
 *   <li>根据 {@link IdempotentProperties.Redis#getRedisTemplateRef()} 指定的 Bean 名称选择 {@link RedisTemplate}，未配置则回退到默认 Bean。</li>
 *   <li>使用 {@link IdempotentProperties.Redis#getKeyPrefix()} 作为键前缀进行幂等键生成。</li>
 * </ul>
 * <p><b>注意事项</b></p>
 * <ul>
 *   <li>与 {@link io.github.pangju666.framework.boot.autoconfigure.web.idempotent.IdempotentProperties.Type#REDIS} 配置配合使用。</li>
 *   <li>确保应用中存在可用的 {@link RedisTemplate} Bean；否则将无法创建验证器。</li>
 * </ul>
 * <p><b>配置示例（YAML）</b></p>
 * <pre>
 * {@code
 * pangju:
 *   web:
 *     idempotent:
 *       type: REDIS
 *       redis:
 *         redis-template-ref: customRedisTemplate
 *         key-prefix: idempotent
 * }
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
     * 注册基于 Redis 的幂等验证器。
     * <p>
     * 优先使用配置的 {@code redis-template-ref} 指定的 {@link RedisTemplate} Bean；
     * 未配置时回退到默认 {@link RedisTemplate}。
     * </p>
     *
     * @param properties  幂等属性（包含 {@link IdempotentProperties.Redis} 配置）
     * @param beanFactory 用于按名称或类型获取 {@link RedisTemplate} Bean
     * @return {@link RedisIdempotentValidator} 实例
     * @since 1.0.0
     */
	@SuppressWarnings("unchecked")
	@ConditionalOnMissingBean(IdempotentValidator.class)
	@Bean
	public RedisIdempotentValidator redisIdempotentValidator(IdempotentProperties properties, BeanFactory beanFactory) {
		RedisTemplate<String, Object> redisTemplate;
		if (StringUtils.hasText(properties.getRedis().getRedisTemplateRef())) {
			redisTemplate = beanFactory.getBean(properties.getRedis().getRedisTemplateRef(), RedisTemplate.class);
		} else {
			redisTemplate = beanFactory.getBean(RedisTemplate.class);
		}
		return new RedisIdempotentValidator(redisTemplate, properties.getRedis().getKeyPrefix());
	}
}
