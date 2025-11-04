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
 * 基于Redisson的分布式速率限制器实现
 * <p>
 * 该类使用Redisson库提供的Redis支持的分布式限流实现。
 * 适合分布式应用或对全局限流一致性要求高的场景。
 * 所有限流计数存储在Redis中，所有应用实例共享同一个限流配额。
 * </p>
 * <p>
 * 实现特性：
 * <ul>
 *     <li>分布式限流 - 多个应用实例共享同一限流配额</li>
 *     <li>全局一致性 - 限流计数在Redis中集中管理</li>
 *     <li>自动初始化 - 第一次访问时自动创建和配置限流器</li>
 *     <li>支持多种时间单位 - 从纳秒到天级别的灵活配置</li>
 *     <li>自动过期 - 限流键可配置自动过期时间</li>
 *     <li>原子操作 - 利用Redis的原子性保证正确性</li>
 * </ul>
 * </p>
 * <p>
 * 限流算法：
 * <p>
 * 使用Redisson的令牌桶算法实现（基于Redis Lua脚本）：
 * <ul>
 *     <li>在Redis中维护一个限流器对象，存储当前令牌数和最后更新时间</li>
 *     <li>当请求到达时，根据经过的时间计算应该生成的令牌数</li>
 *     <li>如果有可用令牌，扣除一个令牌并返回成功</li>
 *     <li>如果没有可用令牌，返回失败</li>
 *     <li>所有操作通过Lua脚本在Redis中原子执行</li>
 * </ul>
 * </p>
 * </p>
 * <p>
 * 适用场景：
 * <ul>
 *     <li>分布式应用 - 多个应用实例需要共享限流配额</li>
 *     <li>微服务架构 - 跨服务的全局限流控制</li>
 *     <li>云原生应用 - 容器化应用的动态扩缩容</li>
 *     <li>SaaS多租户应用 - 按租户的全局限流</li>
 *     <li>需要全局一致性的场景 - 防止单个实例突破总体限流</li>
 * </ul>
 * </p>
 * <p>
 * 依赖关系：
 * <ul>
 *     <li>Redis服务 - 必须有可用的Redis实例</li>
 *     <li>Redisson客户端 - Spring容器中必须有RedissonClient Bean</li>
 *     <li>可选的自定义Redisson客户端名称 - 通过配置指定</li>
 * </ul>
 * </p>
 * <p>
 * 配置激活条件：
 * <p>
 * 该实现在以下条件下被自动激活（通过RedissonRateLimiterConfiguration）：
 * <ul>
 *     <li>配置属性：pangju.web.rate-limit.type = REDISSON</li>
 *     <li>Spring容器中存在RedissonClient Bean</li>
 *     <li>Spring容器中不存在其他RateLimiter实现</li>
 * </ul>
 * </p>
 * <p>
 * Redisson客户端配置：
 * <ul>
 *     <li>如果指定了pangju.web.rate-limit.redisson.redisson-client-bean-name，使用指定的Bean</li>
 *     <li>否则使用容器中的默认RedissonClient Bean</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * # 配置文件示例（application.yml）
 * pangju:
 *   web:
 *     rate-limit:
 *       type: REDISSON  # 指定使用Redisson实现
 *       redisson:
 *         redisson-client-bean-name: redissonClient  # 可选，指定Redisson客户端Bean名称
 *         key-prefix: my-app-rate-limit  # Redis键前缀
 *
 * # Redis配置示例
 * spring:
 *   data:
 *     redis:
 *       url: redis://localhost:6379
 *
 * // 控制器示例
 * @RestController
 * @RequestMapping("/api")
 * public class ApiController {
 *     // 全局限流：每秒最多10个请求（所有实例共享）
 *     @GetMapping("/data")
 *     @RateLimit(
 *         rate = 10,
 *         interval = 1,
 *         timeUnit = TimeUnit.SECONDS,
 *         scope = RateLimitScope.GLOBAL
 *     )
 *     public ResponseEntity<?> getData() {
 *         return ResponseEntity.ok("data");
 *     }
 *
 *     // 基于IP的全局限流：每分钟最多100个请求/IP
 *     @PostMapping("/submit")
 *     @RateLimit(
 *         rate = 100,
 *         interval = 1,
 *         timeUnit = TimeUnit.MINUTES,
 *         scope = RateLimitScope.SOURCE,
 *         source = IpRateLimitSourceExtractor.class
 *     )
 *     public ResponseEntity<?> submit(@RequestBody Data data) {
 *         return ResponseEntity.ok("submitted");
 *     }
 * }
 * }
 * </pre>
 * </p>
 * <p>
 * Redis键格式：
 * <p>
 * 限流键在Redis中的存储格式为：
 * <pre>
 * {@code
 * 例1：key-prefix="my-app", 限流键="api:data"
 *     Redis键：my-app:api:data
 *
 * 例2：key-prefix="", 限流键="/api/data_GET_192.168.1.1"
 *     Redis键：/api/data_GET_192.168.1.1
 *
 * 例3：key-prefix不指定，使用默认前缀"rate-limit"
 *     Redis键：rate-limit:api:submit:192.168.1.1
 * }
 * </pre>
 * </p>
 * </p>
 * <p>
 * 性能特性：
 * <ul>
 *     <li>延迟：通常 1-5ms（取决于网络和Redis性能）</li>
 *     <li>吞吐量：支持高并发，限制为Redis性能</li>
 *     <li>一致性：全局强一致性，所有实例共享配额</li>
 *     <li>可靠性：依赖Redis的持久化和高可用配置</li>
 * </ul>
 * </p>
 * <p>
 * 与其他实现的对比：
 * <ul>
 *     <li>vs Resilience4JRateLimiter：
 *         <ul>
 *             <li>性能：Resilience4j更快（无网络I/O）</li>
 *             <li>分布式：Redisson支持，Resilience4j不支持</li>
 *             <li>一致性：Redisson全局一致，Resilience4j本地一致</li>
 *             <li>依赖：Redisson需要Redis，Resilience4j无额外依赖</li>
 *             <li>应用：Redisson适合分布式，Resilience4j适合单机</li>
 *         </ul>
 *     </li>
 * </ul>
 * </p>
 * <p>
 * 与其他组件的关系：
 * <ul>
 *     <li>由{@link RateLimitInterceptor}调用进行限流检查</li>
 *     <li>由RedissonRateLimiterConfiguration配置为Spring Bean</li>
 *     <li>使用{@link RateLimit}注解提供的配置参数</li>
 *     <li>使用RedissonClient与Redis通信</li>
 *     <li>使用RateLimitProperties获取配置信息</li>
 * </ul>
 * </p>
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
	 * Redis键路径分隔符
	 * <p>
	 * 用于在组合限流键前缀和限流键时作为分隔符
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private static final String REDIS_PATH_DELIMITER = ":";

	/**
	 * Redisson客户端
	 * <p>
	 * 用于与Redis通信，创建和管理限流器
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final RedissonClient redissonClient;
	private final String keyPrefix;

	public RedissonRateLimiter(RedissonClient redissonClient, String keyPrefix) {
		this.redissonClient = redissonClient;
		this.keyPrefix = keyPrefix;
	}

	/**
	 * 尝试获取限流令牌，检查请求是否被允许
	 * <p>
	 * 该方法使用Redis中的分布式限流器进行全局限流检查。
	 * 所有应用实例共享同一个限流配额。
	 * </p>
	 * <p>
	 * 处理流程：
	 * </p>
	 * <ol>
	 *     <li>根据配置的前缀组装最终的Redis键</li>
	 *     <li>从Redis获取或创建对应键的限流器</li>
	 *     <li>检查限流器是否已初始化：
	 *         <ul>
	 *             <li>如果未初始化，调用initRateLimiter进行初始化</li>
	 *             <li>设置限流键的过期时间（值为时间窗口大小）</li>
	 *             <li>如果初始化失败，抛出ServerException</li>
	 *         </ul>
	 *     </li>
	 *     <li>调用限流器的tryAcquire(1)方法尝试获取一个令牌</li>
	 *     <li>返回尝试结果</li>
	 * </ol>
	 * </p>
	 * <p>
	 * Redis键组装规则：
	 * <ul>
	 *     <li>如果配置了key-prefix，使用 key-prefix:原始键</li>
	 *     <li>否则直接使用原始键</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 过期时间管理：
	 * <p>
	 * 限流键的过期时间设置为时间窗口大小（interval * timeUnit），
	 * 这样可以在一个完整的时间窗口后自动清理Redis中的限流数据，
	 * 防止Redis无限增长。
	 * </p>
	 * </p>
	 * <p>
	 * 分布式一致性：
	 * <p>
	 * 通过Redis的原子操作和Lua脚本保证多个应用实例的限流一致性，
	 * 确保所有实例共享同一个限流配额。
	 * </p>
	 * </p>
	 *
	 * @param key 限流键，唯一标识一个限流规则
	 * @param annotation {@link RateLimit}注解，提供限流参数：
	 *                   <ul>
	 *                       <li>interval - 时间间隔数值</li>
	 *                       <li>timeUnit - 时间单位</li>
	 *                       <li>rate - 时间窗口内允许的最大请求数</li>
	 *                   </ul>
	 * @param request 当前的HTTP请求对象（此实现未使用）
	 * @return 如果成功获取令牌返回true，否则返回false
	 * @throws ServerException 当限流器初始化失败时抛出
	 */
	@Override
	public boolean tryAcquire(String key, RateLimit annotation, HttpServletRequest request) {
		String rateLimitKey = key;
		if (StringUtils.isNotBlank(keyPrefix)) {
			rateLimitKey = keyPrefix + REDIS_PATH_DELIMITER + rateLimitKey;
		}
		RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimitKey);
		if (!rateLimiter.isExists()) {
			if (!initRateLimiter(rateLimiter, annotation)) {
				throw new ServerException("redisson速率限制器初始化失败，key：%s".formatted(rateLimitKey));
			}
			Duration expireDuration = Duration.ofMillis(annotation.timeUnit().toMillis(annotation.interval()));
			redissonClient.getBucket(rateLimitKey).expire(expireDuration);
		}
		return rateLimiter.tryAcquire(1);
	}

	/**
	 * 初始化Redisson限流器
	 * <p>
	 * 根据注解中的时间单位和其他参数设置限流器的速率限制。
	 * 该方法只在限流器第一次使用时调用，随后的调用复用同一个限流器实例。
	 * </p>
	 * <p>
	 * 时间单位处理：
	 * <p>
	 * 根据注解中的timeUnit属性转换为对应的Duration对象，然后调用
	 * Redisson的trySetRate方法设置限流速率。支持的时间单位包括：
	 * <ul>
	 *     <li>DAYS - 天级限流</li>
	 *     <li>HOURS - 小时级限流</li>
	 *     <li>MINUTES - 分钟级限流</li>
	 *     <li>SECONDS - 秒级限流</li>
	 *     <li>MILLISECONDS - 毫秒级限流</li>
	 *     <li>MICROSECONDS - 微秒级限流</li>
	 *     <li>NANOSECONDS - 纳秒级限流</li>
	 * </ul>
	 * </p>
	 * </p>
	 * <p>
	 * RateType说明：
	 * <p>
	 * 使用RateType.OVERALL表示全局速率限制，即对所有请求进行统一的速率限制。
	 * </p>
	 * </p>
	 * <p>
	 * 返回值说明：
	 * <p>
	 * trySetRate在限流器已存在时返回false，首次设置时返回true。
	 * 这在分布式环境中很重要，多个实例同时初始化时只有一个会成功。
	 * </p>
	 * </p>
	 *
	 * @param rateLimiter Redisson限流器实例
	 * @param rateLimit {@link RateLimit}注解，提供速率限制参数：
	 *                  <ul>
	 *                      <li>rate - 限流速率（请求数）</li>
	 *                      <li>interval - 时间间隔数值</li>
	 *                      <li>timeUnit - 时间单位</li>
	 *                  </ul>
	 * @return 初始化成功返回true，限流器已存在或设置失败返回false
	 * @since 1.0.0
	 */
	private boolean initRateLimiter(RRateLimiter rateLimiter, RateLimit rateLimit) {
		return switch (rateLimit.timeUnit()) {
			case DAYS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), Duration.ofDays(rateLimit.interval()));
			case HOURS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), Duration.ofHours(rateLimit.interval()));
			case MINUTES ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), Duration.ofMinutes(rateLimit.interval()));
			case SECONDS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), Duration.ofSeconds(rateLimit.interval()));
			case MILLISECONDS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), Duration.ofMillis(rateLimit.interval()));
			case MICROSECONDS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), Duration.of(rateLimit.interval(), ChronoUnit.NANOS));
			case NANOSECONDS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), Duration.ofNanos(rateLimit.interval()));
		};
	}
}
