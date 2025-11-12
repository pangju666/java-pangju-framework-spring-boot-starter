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
import io.github.pangju666.framework.boot.web.limit.limiter.impl.Resilience4JRateLimiter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j 限流器自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>在满足条件时自动注册基于 Resilience4j 的本地 {@link RateLimiter} 实现。</li>
 * </ul>
 *
 * <p><strong>激活条件</strong></p>
 * <ul>
 *   <li>类路径存在 {@code io.github.resilience4j.ratelimiter.RateLimiter}（{@link org.springframework.boot.autoconfigure.condition.ConditionalOnClass}）。</li>
 *   <li>配置属性 {@code pangju.web.rate-limit.type = RESILIENCE4J} 或未指定（{@link org.springframework.boot.autoconfigure.condition.ConditionalOnProperty}）。</li>
 *   <li>容器中不存在其他 {@link RateLimiter} Bean（{@link org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean}）。</li>
 * </ul>
 *
 * <p><strong>行为</strong></p>
 * <ul>
 *   <li>注册 {@link Resilience4JRateLimiter}，使用内存存储，低延迟。</li>
 *   <li>不依赖外部服务，适用于单机与开发测试环境。</li>
 * </ul>
 *
 * <p><strong>注意</strong></p>
 * <ul>
 *   <li>{@code @Configuration(proxyBeanMethods = false)} 禁用 Bean 方法代理以提升性能。</li>
 *   <li>与 Redisson 分布式限流配置互斥，依据 {@code type} 选择。</li>
 *   <li>不提供分布式一致性或跨实例共享配额。</li>
 * </ul>
 *
 * @author pangju666
 * @see Resilience4JRateLimiter
 * @see RateLimiter
 * @see RateLimitProperties
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(io.github.resilience4j.ratelimiter.RateLimiter.class)
@ConditionalOnProperty(prefix = "pangju.web.rate-limit", value = "type", havingValue = "RESILIENCE4J", matchIfMissing = true)
public class Resilience4jRateLimiterConfiguration {
    /**
     * 创建并注册 {@link Resilience4JRateLimiter} Bean。
     *
     * <p><strong>激活</strong></p>
     * <ul>
     *   <li>{@link ConditionalOnMissingBean}：不存在其他 {@link RateLimiter} Bean。</li>
     * </ul>
     *
     * <p><strong>行为</strong></p>
     * <ul>
     *   <li>提供本地内存速率限制器，无需外部依赖。</li>
     * </ul>
     *
     * @return 初始化完成的 {@link Resilience4JRateLimiter}
     * @since 1.0.0
     */
	@ConditionalOnMissingBean(RateLimiter.class)
	@Bean
	public Resilience4JRateLimiter resilience4JRateLimiter() {
		return new Resilience4JRateLimiter();
	}
}
