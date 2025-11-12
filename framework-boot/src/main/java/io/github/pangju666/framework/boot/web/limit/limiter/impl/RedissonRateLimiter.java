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
import io.github.pangju666.framework.boot.web.limit.interceptor.RateLimitInterceptor;
import io.github.pangju666.framework.boot.web.limit.limiter.RateLimiter;
import io.github.pangju666.framework.web.exception.base.ServerException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * 基于 Redisson 的分布式限流器实现。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>使用 Redis 支持的分布式限流，所有应用实例共享同一配额。</li>
 *   <li>适用于分布式与微服务场景，对全局一致性要求较高。</li>
 * </ul>
 *
 * <p><strong>行为</strong></p>
 * <ul>
 *   <li>按配置前缀与限流键组合生成 Redis 键。</li>
 *   <li>首次访问时初始化 {@link RRateLimiter}，后续复用同一实例。</li>
 * </ul>
 *
 * <p><strong>注意</strong></p>
 * <ul>
 *   <li>依赖可用的 Redis 与 {@link RedissonClient}。</li>
 *   <li>网络与 Redis 性能会影响限流检查时延。</li>
 * </ul>
 *
 * @author pangju666
 * @see RateLimiter
 * @see RateLimitInterceptor
 * @see RateLimit
 * @see org.redisson.api.RRateLimiter
 * @since 1.0.0
 */
public class RedissonRateLimiter implements RateLimiter {
	/**
	 * Redis 键路径分隔符。
	 *
	 * @since 1.0.0
	 */
	private static final String REDIS_PATH_DELIMITER = ":";

	/**
	 * Redisson 客户端，用于与 Redis 通信并管理限流器。
	 *
	 * @since 1.0.0
	 */
	private final RedissonClient redissonClient;
	/**
	 * 限流键前缀，用于在 Redis 中进行命名空间隔离（例如：prefix:key）。
	 *
	 * @since 1.0.0
	 */
	private final String keyPrefix;

	public RedissonRateLimiter(RedissonClient redissonClient, String keyPrefix) {
		this.redissonClient = redissonClient;
		this.keyPrefix = keyPrefix;
	}

	/**
	 * 根据注解参数获取或初始化分布式限流器并尝试获取令牌。
	 *
	 * <p>
	 * 说明：当 {@code interval < 1} 时直接允许通过；采用非阻塞方式尝试获取 1 个令牌。
	 * </p>
	 *
	 * @param key        限流键，唯一标识一个限流规则
	 * @param annotation 限流参数：时间窗口（interval + timeUnit）与速率（rate）
	 * @param request    当前 HTTP 请求对象（此实现未使用）
	 * @return 成功获取令牌返回 {@code true}，否则返回 {@code false}
	 * @throws ServerException 限流器初始化失败时抛出
	 * @since 1.0.0
	 */
	@Override
	public boolean tryAcquire(String key, RateLimit annotation, HttpServletRequest request) {
		if (annotation.interval() < 1) {
			return true;
		}
		String rateLimitKey = key;
		if (StringUtils.isNotBlank(keyPrefix)) {
			rateLimitKey = keyPrefix + REDIS_PATH_DELIMITER + rateLimitKey;
		}
		RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimitKey);
		if (!rateLimiter.isExists()) {
			initRateLimiter(rateLimiter, rateLimitKey, annotation);
		}
		return rateLimiter.tryAcquire(1);
	}

	/**
	 * 初始化 Redisson 分布式限流器。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>幂等：若键已存在对应限流器则直接返回。</li>
	 *   <li>依据 {@link RateLimit#timeUnit()} 与 {@link RateLimit#interval()} 计算窗口大小。</li>
	 *   <li>使用 {@link RateType#OVERALL}，将每个时间窗口的许可数设置为 {@link RateLimit#rate()}。</li>
	 * </ul>
	 *
	 * <p><strong>并发</strong></p>
	 * <ul>
	 *   <li>通过原子 {@code trySetRate} 与存在性检查容忍并发初始化，避免竞态。</li>
	 * </ul>
	 *
	 * @param rateLimiter  Redisson 限流器实例
	 * @param rateLimitKey Redis 中的限流键（可能包含前缀）
	 * @param annotation   限流参数：速率（rate）、时间窗口（interval + timeUnit）
	 * @throws ServerException 初始化失败时抛出
	 * @since 1.0.0
	 */
	protected void initRateLimiter(RRateLimiter rateLimiter, String rateLimitKey, RateLimit annotation) {
		// 已存在则直接返回（幂等）
		if (rateLimiter.isExists()) {
			return;
		}
		boolean created = switch (annotation.timeUnit()) {
			case DAYS ->
				rateLimiter.trySetRate(RateType.OVERALL, annotation.rate(), Duration.ofDays(annotation.interval()));
			case HOURS ->
				rateLimiter.trySetRate(RateType.OVERALL, annotation.rate(), Duration.ofHours(annotation.interval()));
			case MINUTES ->
				rateLimiter.trySetRate(RateType.OVERALL, annotation.rate(), Duration.ofMinutes(annotation.interval()));
			case SECONDS ->
				rateLimiter.trySetRate(RateType.OVERALL, annotation.rate(), Duration.ofSeconds(annotation.interval()));
			case MILLISECONDS ->
				rateLimiter.trySetRate(RateType.OVERALL, annotation.rate(), Duration.ofMillis(annotation.interval()));
			case MICROSECONDS ->
				rateLimiter.trySetRate(RateType.OVERALL, annotation.rate(), Duration.of(annotation.interval(), ChronoUnit.MICROS));
			case NANOSECONDS ->
				rateLimiter.trySetRate(RateType.OVERALL, annotation.rate(), Duration.ofNanos(annotation.interval()));
		};
		// 并发容错：若本次未创建且仍不可用，则视为初始化失败
		if (!created && !rateLimiter.isExists()) {
			throw new ServerException("redisson速率限制器初始化失败，key：%s".formatted(rateLimitKey));
		}
	}
}
