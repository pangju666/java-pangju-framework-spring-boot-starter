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
import io.github.pangju666.framework.boot.web.limit.enums.RateLimitScope;
import io.github.pangju666.framework.boot.web.limit.interceptor.RateLimitInterceptor;
import io.github.pangju666.framework.boot.web.limit.source.RateLimitSourceExtractor;
import io.github.pangju666.framework.web.servlet.utils.HttpRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.ServletRequestUtils;

/**
 * 基于IP地址的速率限制源提取器
 * <p>
 * 该类是{@link RateLimitSourceExtractor}接口的一个具体实现，
 * 用于从HTTP请求中提取客户端IP地址作为限流源标识。
 * 通过IP地址进行限流，不同的客户端IP会维护独立的限流配额。
 * </p>
 * <p>
 * 工作原理：
 * <p>
 * 使用{@link HttpRequestUtils#getIpAddress(HttpServletRequest)}方法从请求中获取
 * 客户端的真实IP地址。该方法会智能地处理以下场景：
 * <ul>
 *     <li>直接连接：获取Socket连接的远程地址</li>
 *     <li>代理连接：从X-Forwarded-For头获取原始客户端IP</li>
 *     <li>负载均衡器后：从相关头信息获取真实IP</li>
 *     <li>多级代理：处理多个代理层级的情况</li>
 * </ul>
 * </p>
 * </p>
 * <p>
 * 适用场景：
 * <ul>
 *     <li>API接口的DDoS防护 - 限制单个IP的请求频率</li>
 *     <li>公开API的速率限制 - 针对未认证用户的限流</li>
 *     <li>爬虫防护 - 检测和限制异常的IP请求模式</li>
 *     <li>公网应用的过载保护 - 防止单个IP过载系统</li>
 *     <li>内网应用的基本限流 - 内部系统的简单限流需求</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * // 方法级限流：每个IP每分钟最多100个请求
 * @GetMapping("/api/public-data")
 * @RateLimit(
 *     rate = 100,
 *     interval = 1,
 *     timeUnit = TimeUnit.MINUTES,
 *     scope = RateLimitScope.SOURCE,
 *     source = IpRateLimitSourceExtractor.class,
 *     message = "您的IP请求过于频繁，请稍后再试"
 * )
 * public ResponseEntity<?> getPublicData() {
 *     return ResponseEntity.ok("public data");
 * }
 *
 * // 类级限流：整个控制器按IP限流
 * @RestController
 * @RequestMapping("/api")
 * @RateLimit(
 *     rate = 500,
 *     interval = 1,
 *     timeUnit = TimeUnit.HOURS,
 *     scope = RateLimitScope.SOURCE,
 *     source = IpRateLimitSourceExtractor.class
 * )
 * public class PublicApiController {
 *     @GetMapping("/status")
 *     public ResponseEntity<?> getStatus() {
 *         return ResponseEntity.ok("ok");
 *     }
 *
 *     @GetMapping("/info")
 *     public ResponseEntity<?> getInfo() {
 *         return ResponseEntity.ok("info");
 *     }
 * }
 * }
 * </pre>
 * </p>
 * <p>
 * IP获取机制详解：
 * <p>
 * {@link HttpRequestUtils#getIpAddress}方法会按以下优先级获取IP：
 * <ol>
 *     <li>检查X-Forwarded-For头（代理链中的原始客户端IP）</li>
 *     <li>检查X-Real-IP头（Nginx代理设置的原始IP）</li>
 *     <li>检查CF-Connecting-IP头（Cloudflare的客户端IP）</li>
 *     <li>检查HTTP_X_FORWARDED_FOR环境变量</li>
 *     <li>获取HttpServletRequest的RemoteAddr属性</li>
 * </ol>
 * 这样可以在各种网络部署场景下准确获取客户端真实IP。
 * </p>
 * </p>
 * <p>
 * 限流键示例：
 * <p>
 * 当使用本提取器时，生成的限流键格式为：
 * <pre>
 * {@code
 * 例1：prefix="api", scope=SOURCE
 *     限流键：api_192.168.1.100
 *
 * 例2：prefix="", key="search", scope=SOURCE
 *     限流键：search_10.0.0.1
 *
 * 例3：prefix="/api/data", key="", scope=SOURCE
 *     限流键：/api/data_GET_172.16.0.5
 * }
 * </pre>
 * </p>
 * </p>
 * <p>
 * 与Spring容器的集成：
 * <p>
 * 该类被{@link RateLimiterAutoConfiguration}自动配置为Spring Bean，
 * 可以直接在{@link RateLimit#source()}注解中使用，或通过依赖注入获取。
 * </p>
 * </p>
 * <p>
 * 性能特性：
 * <ul>
 *     <li>轻量级 - 仅进行字符串操作，无复杂计算</li>
 *     <li>线程安全 - {@link HttpRequestUtils#getIpAddress}是线程安全的</li>
 *     <li>高效 - 直接获取请求属性，无I/O操作</li>
 *     <li>可缓存 - 相同IP的多次请求返回相同的标识</li>
 * </ul>
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *     <li>在代理环境下需要确保代理正确设置了转发头（X-Forwarded-For、X-Real-IP等）</li>
 *     <li>IP地址范围可能很大，如果应用面向全球可能导致大量不同的IP限流键</li>
 *     <li>IPv4地址范围：0.0.0.0 - 255.255.255.255，约43亿个</li>
 *     <li>IPv6地址数量更多，需要考虑存储开销</li>
 *     <li>不适合用于基于用户的精细化限流，用户级限流应使用用户ID提取器</li>
 * </ul>
 * </p>
 * <p>
 * 与其他组件的关系：
 * <ul>
 *     <li>由{@link RateLimitInterceptor}在生成限流键时调用</li>
 *     <li>由{@link RateLimit#source()}注解引用</li>
 *     <li>在{@link RateLimitScope#SOURCE}限流作用域下使用</li>
 *     <li>依赖{@link HttpRequestUtils#getIpAddress}获取IP地址</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see RateLimitSourceExtractor
 * @see RateLimit
 * @see RateLimitInterceptor
 * @see ServletRequestUtils
 * @since 1.0.0
 */
public class IpRateLimitSourceExtractor implements RateLimitSourceExtractor {
	/**
	 * 从HTTP请求中提取客户端IP地址
	 * <p>
	 * 使用{@link HttpRequestUtils#getIpAddress}方法智能地从请求中获取
	 * 客户端的真实IP地址，支持多种网络部署场景（直连、代理、负载均衡等）。
	 * </p>
	 * <p>
	 * 返回的IP地址会用作限流源标识，相同IP的请求会共享同一个限流配额。
	 * </p>
	 *
	 * @param request 当前的HTTP请求对象
	 * @return 提取出的客户端IP地址字符串，格式为标准的IP地址格式（IPv4或IPv6）。
	 * 不会返回null，无法获取IP时返回请求的RemoteAddr
	 */
	@Override
	public String getSource(HttpServletRequest request) {
		return HttpRequestUtils.getIpAddress(request);
	}
}
