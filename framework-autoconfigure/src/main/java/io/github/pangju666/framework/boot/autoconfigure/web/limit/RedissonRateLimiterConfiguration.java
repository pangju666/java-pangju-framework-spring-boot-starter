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

package io.github.pangju666.framework.boot.autoconfigure.web.limit;

import io.github.pangju666.framework.boot.web.limit.limiter.RateLimiter;
import io.github.pangju666.framework.boot.web.limit.limiter.impl.RedissonRateLimiter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Redisson 限流器自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>在满足条件时自动注册基于 Redisson 的分布式 {@link RateLimiter} 实现。</li>
 * </ul>
 *
 * <p><strong>激活条件</strong></p>
 * <ul>
 *   <li>类路径存在 {@link RedissonClient}（{@link org.springframework.boot.autoconfigure.condition.ConditionalOnClass}）。</li>
 *   <li>配置属性 {@code pangju.web.rate-limit.type = REDISSON}（{@link org.springframework.boot.autoconfigure.condition.ConditionalOnProperty}）。</li>
 *   <li>容器中不存在其他 {@link RateLimiter} Bean（{@link org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean}）。</li>
 * </ul>
 *
 * <p><strong>行为</strong></p>
 * <ul>
 *   <li>注册 {@link RedissonRateLimiter}，以 Redis 作为存储，实现分布式共享配额。</li>
 *   <li>从容器获取 {@link RedissonClient}（可按配置指定 Bean 名称或使用默认）。</li>
 *   <li>传入 Redis 键前缀以实现命名空间隔离。</li>
 * </ul>
 *
 * <p><strong>注意</strong></p>
 * <ul>
 *   <li>{@code @Configuration(proxyBeanMethods = false)} 禁用 Bean 方法代理以提升性能。</li>
 *   <li>与 Resilience4J 本地限流配置互斥，依据 {@code type} 选择。</li>
 *   <li>不设置 TTL；键生命周期由 Redisson/Redis 管理。</li>
 *   <li>需正确配置 Redis 与 Redisson 客户端。</li>
 * </ul>
 *
 * @author pangju666
 * @see RedissonRateLimiter
 * @see RateLimiter
 * @see RateLimitProperties
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({RedissonClient.class})
@ConditionalOnProperty(prefix = "pangju.web.rate-limit", value = "type", havingValue = "REDISSON")
class RedissonRateLimiterConfiguration {
    /**
     * 创建并注册 {@link RedissonRateLimiter} Bean。
     *
     * <p><strong>激活</strong></p>
     * <ul>
     *   <li>{@link ConditionalOnMissingBean}：不存在其他 {@link RateLimiter} Bean。</li>
     * </ul>
     *
     * <p><strong>RedissonClient 获取策略</strong></p>
     * <ul>
     *   <li>若配置指定客户端 Bean 名称，则按名获取。</li>
     *   <li>否则使用容器中的默认 {@link RedissonClient}。</li>
     * </ul>
     *
     * <p><strong>行为</strong></p>
     * <ul>
     *   <li>将 Redis 键前缀传入限流器以实现命名空间隔离。</li>
     * </ul>
     *
     * @param properties  限流配置属性
     * @param beanFactory Spring Bean 工厂
     * @return 初始化完成的 {@link RedissonRateLimiter}
     * @since 1.0.0
     */
	@ConditionalOnMissingBean(RateLimiter.class)
	@Bean
	public RedissonRateLimiter redissonRateLimiter(RateLimitProperties properties, BeanFactory beanFactory) {
		RedissonClient redissonClient;
		if (StringUtils.hasText(properties.getRedisson().getRedissonClientRef())) {
			redissonClient = beanFactory.getBean(properties.getRedisson().getRedissonClientRef(), RedissonClient.class);
		} else {
			redissonClient = beanFactory.getBean(RedissonClient.class);
		}
		return new RedissonRateLimiter(redissonClient, properties.getRedisson().getKeyPrefix());
	}
}
