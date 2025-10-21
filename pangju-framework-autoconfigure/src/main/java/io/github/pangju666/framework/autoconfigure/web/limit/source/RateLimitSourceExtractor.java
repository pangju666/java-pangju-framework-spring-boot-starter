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

package io.github.pangju666.framework.autoconfigure.web.limit.source;

import io.github.pangju666.framework.autoconfigure.web.limit.annotation.RateLimit;
import io.github.pangju666.framework.autoconfigure.web.limit.enums.RateLimitScope;
import io.github.pangju666.framework.autoconfigure.web.limit.interceptor.RateLimitInterceptor;
import io.github.pangju666.framework.autoconfigure.web.limit.source.impl.IpRateLimitSourceExtractor;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 速率限制源提取器接口
 * <p>
 * 该接口定义了从HTTP请求中提取源标识信息的契约。
 * 在基于源的限流（{@link RateLimitScope#SOURCE}）中，
 * 需要根据请求的源信息为不同的源维护独立的限流计数。
 * </p>
 * <p>
 * 主要用途：
 * <ul>
 *     <li>提取请求的源标识，如IP地址、用户ID、API密钥等</li>
 *     <li>为每个源维护独立的限流配额</li>
 *     <li>支持灵活的源定义策略</li>
 *     <li>支持多租户场景中的租户级限流</li>
 * </ul>
 * </p>
 * <p>
 * 内置实现：
 * <ul>
 *     <li>{@link IpRateLimitSourceExtractor} - 基于客户端IP地址的源提取</li>
 * </ul>
 * </p>
 * <p>
 * 自定义实现示例：
 * <pre>
 * {@code
 * // 基于用户ID的源提取器
 * public class UserIdRateLimitSourceExtractor implements RateLimitSourceExtractor {
 *     @Override
 *     public String getSource(HttpServletRequest request) {
 *         // 从JWT令牌或会话中提取用户ID
 *         String userId = (String) request.getAttribute("userId");
 *         return userId != null ? userId : request.getRemoteAddr();
 *     }
 * }
 *
 * // 基于API密钥的源提取器
 * public class ApiKeyRateLimitSourceExtractor implements RateLimitSourceExtractor {
 *     @Override
 *     public String getSource(HttpServletRequest request) {
 *         return request.getHeader("X-API-Key");
 *     }
 * }
 *
 * // 基于租户ID的源提取器
 * public class TenantIdRateLimitSourceExtractor implements RateLimitSourceExtractor {
 *     @Override
 *     public String getSource(HttpServletRequest request) {
 *         return request.getHeader("X-Tenant-ID");
 *     }
 * }
 * }
 * </pre>
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *     <li>IP级限流 - 按客户端IP地址分别限制请求频率</li>
 *     <li>用户级限流 - 按登录用户分别限制请求频率</li>
 *     <li>API密钥级限流 - 按API密钥分别限制请求频率</li>
 *     <li>租户级限流 - 在SaaS应用中按租户分别限制请求频率</li>
 *     <li>会话级限流 - 按会话ID分别限制请求频率</li>
 * </ul>
 * </p>
 * <p>
 * 工作流程：
 * <ol>
 *     <li>{@link RateLimitInterceptor}在拦截到标记了{@link RateLimit}注解的请求时</li>
 *     <li>如果{@link RateLimit#scope()}为{@link RateLimitScope#SOURCE}</li>
 *     <li>使用{@link RateLimit#source()}指定的源提取器实现</li>
 *     <li>调用本接口的{@link #getSource}方法提取源标识</li>
 *     <li>将源标识组合到限流键中，为不同的源维护独立计数</li>
 * </ol>
 * </p>
 * <p>
 * 与其他组件的关系：
 * <ul>
 *     <li>由{@link RateLimitInterceptor}在生成限流键时调用</li>
 *     <li>由{@link RateLimit#source()}指定具体的实现类</li>
 *     <li>与{@link RateLimitScope#SOURCE}一起工作</li>
 * </ul>
 * </p>
 * <p>
 * 最佳实践：
 * <ul>
 *     <li>源提取器应该能被Spring容器管理，或能通过无参构造器实例化</li>
 *     <li>源提取器应该是线程安全的，因为会被多个请求线程并发调用</li>
 *     <li>源提取器应该返回非空、稳定的标识字符串</li>
 *     <li>源提取器应该性能高效，避免复杂的计算或I/O操作</li>
 *     <li>源标识应该具有足够的唯一性，避免过度的限流聚合</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see RateLimit
 * @see RateLimitScope
 * @see RateLimitInterceptor
 * @see IpRateLimitSourceExtractor
 * @since 1.0.0
 */
public interface RateLimitSourceExtractor {
	/**
	 * 从HTTP请求中提取源标识信息
	 * <p>
	 * 该方法根据HTTP请求提取出一个源标识字符串。
	 * 在基于源的限流中，相同源的请求会共享同一个限流配额，
	 * 不同源的请求会维护独立的限流配额。
	 * </p>
	 * <p>
	 * 提取策略完全由实现类自定义，可以基于：
	 * <ul>
	 *     <li>客户端IP地址（来自请求头或Socket地址）</li>
	 *     <li>用户身份（来自JWT令牌、会话或请求属性）</li>
	 *     <li>API密钥（来自请求头或查询参数）</li>
	 *     <li>租户ID（来自请求头或上下文）</li>
	 *     <li>设备ID或客户端标识（来自请求头）</li>
	 *     <li>任何其他能唯一标识请求源的信息</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 实现要求：
	 * <ul>
	 *     <li>必须返回非null的字符串</li>
	 *     <li>应该返回稳定的标识，相同源的多个请求应该返回相同的标识</li>
	 *     <li>应该避免返回可能变化的标识（如时间戳），以确保限流效果</li>
	 *     <li>应该是幂等的，多次调用应该产生相同的结果</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 使用示例：
	 * <pre>
	 * {@code
	 * // IP地址提取
	 * public String getSource(HttpServletRequest request) {
	 *     return request.getRemoteAddr();
	 * }
	 *
	 * // 用户ID提取
	 * public String getSource(HttpServletRequest request) {
	 *     User user = (User) request.getAttribute("user");
	 *     return user != null ? String.valueOf(user.getId()) : request.getRemoteAddr();
	 * }
	 *
	 * // API密钥提取
	 * public String getSource(HttpServletRequest request) {
	 *     String apiKey = request.getHeader("X-API-Key");
	 *     return apiKey != null ? apiKey : "anonymous";
	 * }
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param request 当前的HTTP请求对象，包含请求的所有信息
	 * @return 提取出的源标识字符串，不应该为null。
	 * 该标识将被用于生成限流键，相同的标识会共享限流配额
	 * @since 1.0.0
	 */
	String getSource(HttpServletRequest request);
}
