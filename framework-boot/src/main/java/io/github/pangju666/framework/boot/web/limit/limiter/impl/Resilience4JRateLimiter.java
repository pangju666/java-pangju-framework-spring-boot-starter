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

package io.github.pangju666.framework.boot.web.limit.limiter.impl;

import io.github.pangju666.framework.boot.web.limit.annotation.RateLimit;
import io.github.pangju666.framework.boot.web.limit.limiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Duration;

/**
 * 基于 Resilience4j 的限流器实现。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>使用 Resilience4j 提供的内存限流，适合单机或对一致性要求不高的场景。</li>
 *   <li>通过 {@link RateLimiterRegistry} 为不同限流键创建并缓存限流器实例。</li>
 * </ul>
 *
 * <p><strong>行为</strong></p>
 * <ul>
 *   <li>依据 {@link RateLimit} 注解参数构建 {@link RateLimiterConfig}。</li>
 *   <li>调用 Resilience4j 限流器的 {@code acquirePermission()} 非阻塞获取权限。</li>
 * </ul>
 *
 * <p><strong>注意</strong></p>
 * <ul>
 *   <li>不提供分布式一致性；不同应用实例的计数相互独立。</li>
 *   <li>应用重启后计数重置；未做持久化。</li>
 *   <li>同一键的限流器在注册表中配置不可自动更新；如需变更配置，请使用新键或在外部重建。</li>
 * </ul>
 *
 * @author pangju666
 * @see RateLimiter
 * @see RateLimit
 * @see RateLimiterRegistry
 * @since 1.0.0
 */
public class Resilience4JRateLimiter implements RateLimiter {
	/**
	 * Resilience4j 限流器注册表，用于按键创建与缓存限流器实例。
	 *
	 * @since 1.0.0
	 */
	private final RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();

	/**
	 * 根据注解参数构建配置并尝试获取权限，判定请求是否允许通过。
	 *
	 * <p>
	 * 说明：刷新周期统一为毫秒；当 {@code interval < 1} 时直接允许通过；
	 * 超时为 0 以保持非阻塞判定。
	 * </p>
	 *
	 * @param key        限流键，唯一标识一个限流维度
	 * @param annotation 限流参数：时间窗口（interval + timeUnit）与速率（rate）
	 * @param request    当前 HTTP 请求对象（此实现未使用）
	 * @return 获取到权限返回 {@code true}；未获取到返回 {@code false}
	 * @since 1.0.0
	 */
	@Override
	public boolean tryAcquire(String key, RateLimit annotation, HttpServletRequest request) {
		if (annotation.interval() < 1) {
			return true;
		}
		RateLimiterConfig config = RateLimiterConfig.custom()
			.limitRefreshPeriod(Duration.ofMillis(annotation.timeUnit().toMillis(annotation.interval())))
			.limitForPeriod(annotation.rate())
			.timeoutDuration(Duration.ZERO)
			.build();
		return rateLimiterRegistry.rateLimiter(key, config).acquirePermission();
	}
}
