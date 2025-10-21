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

package io.github.pangju666.framework.autoconfigure.web.limit.limiter;

import io.github.pangju666.framework.autoconfigure.web.limit.annotation.RateLimit;
import io.github.pangju666.framework.autoconfigure.web.limit.exception.RateLimitException;
import io.github.pangju666.framework.autoconfigure.web.limit.interceptor.RateLimitInterceptor;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 速率限制器接口
 * <p>
 * 该接口定义了速率限制的核心契约，用于检查和控制请求是否超过限流阈值。
 * 实现类负责维护限流计数，判断请求是否被允许通过。
 * </p>
 * <p>
 * 主要职责：
 * <ul>
 *     <li>维护每个限流键的请求计数</li>
 *     <li>管理时间窗口和计数重置</li>
 *     <li>判断请求是否超过限流阈值</li>
 *     <li>返回限流检查结果</li>
 * </ul>
 * </p>
 * <p>
 * 实现方式：
 * <p>
 * 框架提供了多种实现方式，可根据部署场景选择：
 * <ul>
 *     <li>Resilience4j实现 - 基于内存的限流，适合单机应用</li>
 *     <li>Redisson实现 - 基于Redis的分布式限流，适合分布式应用</li>
 * </ul>
 * </p>
 * </p>
 * <p>
 * 工作流程：
 * <ol>
 *     <li>{@link RateLimitInterceptor}生成限流键</li>
 *     <li>调用本接口的{@link #tryAcquire}方法进行检查</li>
 *     <li>如果返回true，请求被允许通过</li>
 *     <li>如果返回false，请求被拦截，抛出{@link RateLimitException}</li>
 * </ol>
 * </p>
 * <p>
 * 限流算法支持：
 * <ul>
 *     <li>令牌桶算法 - 通过令牌管理请求速率</li>
 *     <li>滑动窗口算法 - 通过时间窗口内的计数限制速率</li>
 *     <li>其他自定义算法 - 通过实现本接口支持</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *     <li>API接口流量控制 - 防止单个接口被滥用</li>
 *     <li>全局流量管理 - 全局范围内的限流</li>
 *     <li>基于源的限流 - 按IP、用户等维度的限流</li>
 *     <li>多级限流 - 不同端点的不同限流策略</li>
 *     <li>分布式系统限流 - 跨多个应用实例的协调限流</li>
 * </ul>
 * </p>
 * <p>
 * 性能考虑：
 * <ul>
 *     <li>Resilience4j实现：低延迟，适合高并发场景，无网络I/O</li>
 *     <li>Redisson实现：分布式一致性，轻微延迟，支持跨应用共享</li>
 *     <li>两种实现都支持高并发访问</li>
 * </ul>
 * </p>
 * <p>
 * 与其他组件的关系：
 * <ul>
 *     <li>由{@link RateLimitInterceptor}调用进行限流检查</li>
 *     <li>使用{@link RateLimit}注解提供的配置参数</li>
 *     <li>如果检查失败则触发{@link RateLimitException}</li>
 * </ul>
 * </p>
 * <p>
 * 自定义实现建议：
 * <ul>
 *     <li>实现应该是线程安全的，支持高并发调用</li>
 *     <li>应该支持分布式环境（如果需要）</li>
 *     <li>应该能够处理时间窗口管理</li>
 *     <li>应该提供合理的性能特性</li>
 *     <li>应该支持动态配置更新</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see RateLimit
 * @see RateLimitInterceptor
 * @see RateLimitException
 * @since 1.0.0
 */
public interface RateLimiter {
	/**
	 * 尝试获取限流令牌，检查请求是否被允许
	 * <p>
	 * 该方法是限流检查的核心方法，根据限流配置和当前计数判断请求是否超过限制。
	 * </p>
	 * <p>
	 * 工作逻辑：
	 * <ol>
	 *     <li>根据限流键从存储中获取当前计数（内存或Redis）</li>
	 *     <li>检查当前计数是否已达到或超过限流阈值</li>
	 *     <li>如果未超限，增加计数并返回true（请求被允许）</li>
	 *     <li>如果已超限，不增加计数，返回false（请求被拒绝）</li>
	 *     <li>处理时间窗口过期后的计数重置</li>
	 * </ol>
	 * </p>
	 * <p>
	 * 限流配置的应用：
	 * <ul>
	 *     <li>使用{@link RateLimit#interval()}和{@link RateLimit#timeUnit()}确定时间窗口长度</li>
	 *     <li>使用{@link RateLimit#rate()}确定窗口内允许的最大请求数</li>
	 *     <li>根据{@link RateLimit#scope()}决定是全局限流还是源级限流</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 时间窗口管理：
	 * <p>
	 * 实现应该能够正确处理以下情况：
	 * <ul>
	 *     <li>窗口内的请求计数累加</li>
	 *     <li>窗口过期后的计数自动重置</li>
	 *     <li>跨越窗口边界的请求处理</li>
	 *     <li>分布式环境下的时间同步问题（如使用Redis）</li>
	 * </ul>
	 * </p>
	 * </p>
	 * <p>
	 * 返回值说明：
	 * <ul>
	 *     <li>true - 请求未超限，允许通过。计数已增加，等待下一个请求</li>
	 *     <li>false - 请求已超限，应被拒绝。计数未增加</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 异常处理：
	 * <p>
	 * 实现应该能够处理以下异常情况：
	 * <ul>
	 *     <li>存储服务不可用（Redis宕机等）</li>
	 *     <li>时间同步问题</li>
	 *     <li>并发访问冲突</li>
	 * </ul>
	 * 对于严重错误，应该抛出相应的异常而不是返回false，让上层决定是否拒绝请求。
	 * </p>
	 * </p>
	 * <p>
	 * 实现示例（伪代码）：
	 * <pre>
	 * {@code
	 * public boolean tryAcquire(String key, RateLimit annotation, HttpServletRequest request) {
	 *     // 获取限流配置
	 *     int rate = annotation.rate();
	 *     long windowSize = annotation.interval() * getTimeUnitMillis(annotation.timeUnit());
	 *
	 *     // 从存储获取当前计数
	 *     Counter counter = getCounter(key);
	 *
	 *     // 检查时间窗口是否过期
	 *     if (counter.isExpired(windowSize)) {
	 *         counter.reset();
	 *     }
	 *
	 *     // 检查是否超限
	 *     if (counter.getCount() >= rate) {
	 *         return false;
	 *     }
	 *
	 *     // 增加计数
	 *     counter.increment();
	 *     return true;
	 * }
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param key        限流键，唯一标识一个限流规则。同一个键会共享限流计数
	 * @param annotation {@link RateLimit}注解，包含限流配置参数（速率、时间窗口、作用域等）
	 * @param request    当前的HTTP请求对象，可用于获取额外的上下文信息
	 * @return 如果请求被允许（未超限）则返回true，如果请求被拒绝（已超限）则返回false
	 * @throws RuntimeException 如果限流检查过程中发生不可恢复的错误（如存储服务不可用），
	 *                          实现可以抛出运行时异常
	 * @since 1.0.0
	 */
	boolean tryAcquire(String key, RateLimit annotation, HttpServletRequest request);
}
