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
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Duration;

/**
 * 基于Resilience4j的速率限制器实现
 * <p>
 * 该类使用Resilience4j库提供的内存级限流实现。
 * 适合单机应用或对分布式一致性要求不高的场景。
 * 所有限流计数存储在应用内存中，不同应用实例的限流独立计算。
 * </p>
 * <p>
 * 实现特性：
 * <ul>
 *     <li>基于内存的限流 - 无需外部存储依赖</li>
 *     <li>低延迟 - 限流检查在内存中完成，响应快速</li>
 *     <li>自动管理 - 使用RateLimiterRegistry自动创建和缓存限流器</li>
 *     <li>动态配置 - 支持根据注解动态调整限流参数</li>
 *     <li>线程安全 - Resilience4j的实现是线程安全的</li>
 * </ul>
 * </p>
 * <p>
 * 限流算法：
 * <p>
 * 使用Resilience4j的令牌桶算法：
 * <ul>
 *     <li>根据时间窗口大小和限流速率计算令牌生成速率</li>
 *     <li>每个时间窗口开始时，将令牌数重置为限流速率</li>
 *     <li>每次请求尝试获取一个令牌</li>
 *     <li>如果有可用令牌，请求通过；否则请求被拒绝</li>
 * </ul>
 * </p>
 * </p>
 * <p>
 * 适用场景：
 * <ul>
 *     <li>单机应用 - 无需分布式协调</li>
 *     <li>内网应用 - 应用实例数量较少</li>
 *     <li>对性能要求高的场景 - 需要低延迟的限流检查</li>
 *     <li>不需要跨应用共享限流配额的场景</li>
 *     <li>开发和测试环境 - 无需依赖Redis等外部服务</li>
 * </ul>
 * </p>
 * <p>
 * 限制因素：
 * <ul>
 *     <li>不支持分布式限流 - 每个应用实例独立计数</li>
 *     <li>应用重启后计数被重置 - 无持久化存储</li>
 *     <li>内存占用 - 每个限流键都维护一个计数器</li>
 *     <li>无法在应用间共享限流配额</li>
 * </ul>
 * </p>
 * <p>
 * 配置激活条件：
 * <p>
 * 该实现在以下条件下被自动激活（通过Resilience4jRateLimiterConfiguration）：
 * <ul>
 *     <li>配置属性：pangju.web.rate-limit.type = RESILIENCE4J</li>
 *     <li>或不指定type属性时默认使用（RESILIENCE4J是默认值）</li>
 *     <li>Spring容器中不存在其他RateLimiter实现</li>
 * </ul>
 * </p>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * # 配置文件示例（application.yml）
 * pangju:
 *   web:
 *     rate-limit:
 *       type: RESILIENCE4J  # 指定使用Resilience4j实现
 *
 * // 控制器示例
 * @RestController
 * @RequestMapping("/api")
 * public class ApiController {
 *     // 全局限流：每秒最多10个请求
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
 *     // 基于IP的限流：每分钟最多100个请求
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
 * 性能特性：
 * <ul>
 *     <li>延迟：&lt; 1ms，仅内存操作</li>
 *     <li>吞吐量：支持高并发，取决于硬件</li>
 *     <li>内存占用：每个限流键约几KB</li>
 *     <li>CPU占用：低，主要是原子操作</li>
 * </ul>
 * </p>
 * <p>
 * 与其他实现的对比：
 * <ul>
 *     <li>vs RedissonRateLimiter：
 *         <ul>
 *             <li>性能：Resilience4j更快（无网络I/O）</li>
 *             <li>分布式：Redisson支持，Resilience4j不支持</li>
 *             <li>一致性：Resilience4j本地一致，Redisson全局一致</li>
 *             <li>依赖：Resilience4j无额外依赖，Redisson需要Redis</li>
 *         </ul>
 *     </li>
 * </ul>
 * </p>
 * <p>
 * 与其他组件的关系：
 * <ul>
 *     <li>由{@link RateLimitInterceptor}调用进行限流检查</li>
 *     <li>由Resilience4jRateLimiterConfiguration配置为Spring Bean</li>
 *     <li>使用{@link RateLimit}注解提供的配置参数</li>
 *     <li>使用RateLimiterRegistry管理RateLimiter实例</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see RateLimiter
 * @see RateLimitInterceptor
 * @see RateLimit
 * @see io.github.resilience4j.ratelimiter.RateLimiter
 * @see RateLimiterRegistry
 * @since 1.0.0
 */
public class Resilience4JRateLimiter implements RateLimiter {
	/**
	 * Resilience4j RateLimiter注册表
	 * <p>
	 * 用于创建和管理不同限流键对应的RateLimiter实例。
	 * 使用默认配置创建，会自动缓存已创建的限流器。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();

	/**
	 * 尝试获取限流令牌，检查请求是否被允许
	 * <p>
	 * 该方法根据{@link RateLimit}注解的配置参数，创建相应的限流器配置，
	 * 然后尝试从Resilience4j的限流器获取权限。
	 * </p>
	 * <p>
	 * 处理流程：
	 * </p>
	 * <ol>
	 *     <li>将注解中的时间单位和间隔转换为毫秒值（限流刷新周期）</li>
	 *     <li>创建RateLimiterConfig配置对象：
	 *         <ul>
	 *             <li>limitRefreshPeriod - 限流周期（时间窗口长度）</li>
	 *             <li>limitForPeriod - 周期内允许的最大请求数</li>
	 *             <li>timeoutDuration - 获取权限的超时时间（设为0表示不等待）</li>
	 *         </ul>
	 *     </li>
	 *     <li>从注册表获取或创建对应限流键的限流器</li>
	 *     <li>调用acquirePermission()尝试获取令牌</li>
	 *     <li>返回获取结果：成功返回true，失败返回false</li>
	 * </ol>
	 * <p>
	 * 配置说明：
	 * <ul>
	 *     <li>timeoutDuration设为Duration.ZERO表示立即返回，不阻塞等待</li>
	 *     <li>limitRefreshPeriod是限流的时间窗口大小，过期后令牌数重置</li>
	 *     <li>limitForPeriod是每个时间窗口内允许通过的最大请求数</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 限流器缓存：
	 * <p>
	 * RateLimiterRegistry会自动缓存已创建的限流器实例，相同的限流键会复用
	 * 同一个限流器实例，确保限流计数的准确性。
	 * </p>
	 * </p>
	 * <p>
	 * 性能特性：
	 * <ul>
	 *     <li>单次调用延迟：&lt; 1ms</li>
	 *     <li>支持高并发：Resilience4j内部使用原子操作保证线程安全</li>
	 *     <li>无阻塞：timeoutDuration=0确保非阻塞操作</li>
	 * </ul>
	 * </p>
	 *
	 * @param key 限流键，唯一标识一个限流规则。同一键会使用同一个限流器实例
	 * @param annotation {@link RateLimit}注解，提供限流参数：
	 *                   <ul>
	 *                       <li>interval - 时间间隔数值</li>
	 *                       <li>timeUnit - 时间单位（秒、分、小时等）</li>
	 *                       <li>rate - 时间窗口内允许的最大请求数</li>
	 *                   </ul>
	 * @param request 当前的HTTP请求对象（此实现未使用）
	 * @return 如果成功获取令牌（请求未超限）返回true，否则返回false
	 */
	@Override
	public boolean tryAcquire(String key, RateLimit annotation, HttpServletRequest request) {
		long refreshMillis = annotation.timeUnit().toMillis(annotation.interval());
		RateLimiterConfig config = RateLimiterConfig.custom()
			.limitRefreshPeriod(Duration.ofMillis(refreshMillis))
			.limitForPeriod(annotation.rate())
			.timeoutDuration(Duration.ZERO)
			.build();
		return rateLimiterRegistry.rateLimiter(key, config).acquirePermission();
	}
}
