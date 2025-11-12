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

package io.github.pangju666.framework.boot.web.limit.source.impl;

import io.github.pangju666.framework.boot.web.limit.annotation.RateLimit;
import io.github.pangju666.framework.boot.web.limit.interceptor.RateLimitInterceptor;
import io.github.pangju666.framework.boot.web.limit.source.RateLimitSourceExtractor;
import io.github.pangju666.framework.web.servlet.utils.HttpRequestUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 基于 IP 的限流源提取器。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>实现 {@link RateLimitSourceExtractor}，返回客户端 IP 作为限流源。</li>
 *   <li>使用 {@link HttpRequestUtils#getIpAddress(HttpServletRequest)} 获取真实 IP，兼容代理与负载均衡。</li>
 * </ul>
 *
 * <p><strong>用法示例</strong></p>
 * <pre><code>
 * &#64;RateLimit(scope = RateLimitScope.SOURCE, source = IpRateLimitSourceExtractor.class)
 * </code></pre>
 *
 * <p><strong>注意</strong></p>
 * <ul>
 *   <li>确保代理正确设置转发头（如 X-Forwarded-For、X-Real-IP）。</li>
 *   <li>不适合用户级限流；用户限流应使用用户标识提取器。</li>
 * </ul>
 *
 * @author pangju666
 * @see RateLimitSourceExtractor
 * @see RateLimit
 * @see RateLimitInterceptor
 * @since 1.0.0
 */
public class IpRateLimitSourceExtractor implements RateLimitSourceExtractor {
    /**
     * 返回客户端 IP 作为限流源标识。
     *
     * <p>通过 {@link HttpRequestUtils#getIpAddress(HttpServletRequest)} 获取真实 IP；
     * 无法获取时回退为 {@code request.getRemoteAddr()}。</p>
     *
     * @param request 当前 HTTP 请求
     * @return 客户端 IP 字符串（IPv4/IPv6），不为 {@code null}
     */
	@Override
	public String getSource(HttpServletRequest request) {
		return HttpRequestUtils.getIpAddress(request);
	}
}
