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

package io.github.pangju666.framework.autoconfigure.web.limit;

import io.github.pangju666.framework.autoconfigure.web.WebMvcAutoConfiguration;
import io.github.pangju666.framework.autoconfigure.web.limit.config.RedissonRateLimiterConfiguration;
import io.github.pangju666.framework.autoconfigure.web.limit.config.Resilience4jRateLimiterConfiguration;
import io.github.pangju666.framework.autoconfigure.web.limit.enums.RateLimitScope;
import io.github.pangju666.framework.autoconfigure.web.limit.interceptor.RateLimitInterceptor;
import io.github.pangju666.framework.autoconfigure.web.limit.limiter.RateLimiter;
import io.github.pangju666.framework.autoconfigure.web.limit.source.impl.IpRateLimitSourceExtractor;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * 速率限制器自动配置类
 * <p>
 * 该配置类是速率限制功能的核心自动配置入口。
 * 负责在Spring Boot应用启动时自动配置限流功能的所有必要组件。
 * </p>
 * <p>
 * 主要职责：
 * <ul>
 *     <li>启用和配置限流属性</li>
 *     <li>导入并选择合适的限流器实现配置</li>
 *     <li>创建请求源提取器Bean</li>
 *     <li>确保在Web MVC配置之前加载</li>
 * </ul>
 * </p>
 * <p>
 * 生效条件（必须全部满足）：
 * <ul>
 *     <li>应用类型为Servlet Web应用</li>
 *     <li>类路径中存在Servlet和DispatcherServlet类</li>
 *     <li>在WebMvcAutoConfiguration配置之前加载</li>
 * </ul>
 * </p>
 * <p>
 * 配置加载顺序：
 * <ol>
 *     <li>启用RateLimitProperties配置属性绑定</li>
 *     <li>导入Resilience4jRateLimiterConfiguration和RedissonRateLimiterConfiguration</li>
 *     <li>根据配置属性选择合适的限流器实现</li>
 *     <li>创建IpRateLimitSourceExtractor Bean</li>
 *     <li>在Web MVC配置前完成所有限流相关配置</li>
 * </ol>
 * </p>
 * <p>
 * 支持的限流实现方式：
 * <ul>
 *     <li>Resilience4j - 内存级限流，适合单机应用</li>
 *     <li>Redisson - 分布式限流，适合分布式应用</li>
 * </ul>
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * {@code
 * # 使用Resilience4j实现（内存限流）
 * pangju:
 *   web:
 *     rate-limit:
 *       type: RESILIENCE4J  # 默认值
 *
 * # 使用Redisson实现（分布式限流）
 * pangju:
 *   web:
 *     rate-limit:
 *       type: REDISSON
 *       redisson:
 *         redisson-client-bean-name: redissonClient
 *         key-prefix: my-app-rate-limit
 * }
 * </pre>
 * </p>
 * <p>
 * 组件依赖关系：
 * <ul>
 *     <li>RateLimitProperties - 配置属性类</li>
 *     <li>RateLimiter - 限流器接口</li>
 *     <li>RateLimitInterceptor - 限流拦截器</li>
 *     <li>IpRateLimitSourceExtractor - IP源提取器</li>
 *     <li>Resilience4jRateLimiterConfiguration - Resilience4j配置</li>
 *     <li>RedissonRateLimiterConfiguration - Redisson配置</li>
 * </ul>
 * </p>
 * <p>
 * 导入的配置类说明：
 * <ul>
 *     <li>Resilience4jRateLimiterConfiguration - 内存级限流实现配置</li>
 *     <li>RedissonRateLimiterConfiguration - 分布式限流实现配置</li>
 * </ul>
 * 这两个配置会根据type属性的值和类路径中是否存在相关类来决定是否加载。
 * </p>
 * <p>
 * 工作流程：
 * <ol>
 *     <li>Spring Boot启动时检查条件注解</li>
 *     <li>如果是Servlet Web应用且包含必要的类，加载此配置</li>
 *     <li>启用RateLimitProperties配置属性绑定</li>
 *     <li>导入两个限流器实现配置类</li>
 *     <li>根据配置的type属性，其中一个配置会被激活</li>
 *     <li>创建IpRateLimitSourceExtractor Bean</li>
 *     <li>Web MVC配置加载前，限流配置已完成</li>
 * </ol>
 * </p>
 * <p>
 * 执行优先级：
 * <p>
 * {@code @AutoConfiguration(before = WebMvcAutoConfiguration.class)}确保该配置
 * 在WebMvcAutoConfiguration之前加载，这很重要，因为限流拦截器需要注册到Web MVC的拦截器链中。
 * </p>
 * </p>
 * <p>
 * 与其他自动配置的关系：
 * <ul>
 *     <li>在WebMvcAutoConfiguration之前加载，以便限流拦截器能被正确注册</li>
 *     <li>与Redis自动配置无依赖关系（Redisson实现时会使用Redis）</li>
 *     <li>与Jackson自动配置无关，但都在Web MVC上下文中</li>
 * </ul>
 * </p>
 * <p>
 * 扩展点：
 * <ul>
 *     <li>可以自定义实现RateLimiter接口来提供新的限流实现</li>
 *     <li>可以自定义实现RateLimitSourceExtractor接口来提供新的源提取策略</li>
 *     <li>可以通过配置属性自定义限流行为</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see RateLimitProperties
 * @see RateLimiter
 * @see RateLimitInterceptor
 * @see IpRateLimitSourceExtractor
 * @see Resilience4jRateLimiterConfiguration
 * @see RedissonRateLimiterConfiguration
 * @since 1.0.0
 */
@AutoConfiguration(before = WebMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@EnableConfigurationProperties(RateLimitProperties.class)
@Import({Resilience4jRateLimiterConfiguration.class, RedissonRateLimiterConfiguration.class})
public class RateLimiterAutoConfiguration {
	/**
	 * 创建基于IP地址的速率限制源提取器Bean
	 * <p>
	 * 该Bean提供基于客户端IP地址的请求源识别功能。
	 * 当限流使用{@link RateLimitScope#SOURCE}作用域时，
	 * 该提取器会从请求中提取客户端IP作为限流源标识。
	 * </p>
	 * <p>
	 * 提取器功能：
	 * <ul>
	 *     <li>从HTTP请求中获取客户端真实IP地址</li>
	 *     <li>支持直连和代理场景</li>
	 *     <li>支持负载均衡器后的IP识别</li>
	 *     <li>为每个IP维护独立的限流配额</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 使用示例：
	 * <pre>
	 * {@code
	 * @RateLimit(
	 *     rate = 100,
	 *     interval = 1,
	 *     timeUnit = TimeUnit.MINUTES,
	 *     scope = RateLimitScope.SOURCE,
	 *     source = IpRateLimitSourceExtractor.class
	 * )
	 * public ResponseEntity<?> api() {
	 *     return ResponseEntity.ok("ok");
	 * }
	 * }
	 * </pre>
	 * </p>
	 * <p>
	 * 该Bean生命周期：
	 * <ol>
	 *     <li>应用启动时由此配置类自动创建</li>
	 *     <li>被RateLimitInterceptor在生成限流键时使用</li>
	 *     <li>应用关闭时自动销毁</li>
	 * </ol>
	 * </p>
	 *
	 * @return 新创建的IpRateLimitSourceExtractor实例
	 * @see IpRateLimitSourceExtractor
	 * @see RateLimitScope
	 * @since 1.0.0
	 */
	@Bean
	public IpRateLimitSourceExtractor ipRateLimitSourceExtractor() {
		return new IpRateLimitSourceExtractor();
	}
}
