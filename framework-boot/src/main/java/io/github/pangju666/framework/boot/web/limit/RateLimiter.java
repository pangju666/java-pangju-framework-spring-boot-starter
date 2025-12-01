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

package io.github.pangju666.framework.boot.web.limit;

import io.github.pangju666.framework.boot.web.annotation.RateLimit;
import io.github.pangju666.framework.boot.web.exception.RateLimitException;
import io.github.pangju666.framework.boot.web.interceptor.RateLimitInterceptor;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 速率限制器接口。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>定义限流检查的统一契约，用于判定请求是否允许通过。</li>
 *   <li>由 {@link RateLimitInterceptor} 生成限流键后调用 {@link #tryAcquire} 执行检查。</li>
 * </ul>
 *
 * <p><strong>典型实现</strong></p>
 * <ul>
 *   <li>内存实现（如 Resilience4j）：适合单机应用。</li>
 *   <li>Redis 实现（如 Redisson）：适合分布式应用。</li>
 * </ul>
 *
 * @author pangju666
 * @see RateLimit
 * @see RateLimitInterceptor
 * @see RateLimitException
 * @since 1.0.0
 */
public interface RateLimiter {
    /**
     * 执行限流检查，判定请求是否允许通过。
     *
     * <p>实现需依据 {@link RateLimit} 配置与当前计数进行判断：未超限返回 {@code true}
     * 并更新计数；已超限返回 {@code false}。</p>
     *
     * @param key 限流键，唯一标识一个限流维度
     * @param annotation 限流配置参数（速率、时间窗口、作用域等）
     * @param request 当前 HTTP 请求对象
     * @return 未超限返回 {@code true}；已超限返回 {@code false}
     * @throws RuntimeException 实现可在存储不可用等严重错误时抛出运行时异常
     * @since 1.0.0
     */
	boolean tryAcquire(String key, RateLimit annotation, HttpServletRequest request);
}
