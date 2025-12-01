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

package io.github.pangju666.framework.boot.autoconfigure.web.limit;

import io.github.pangju666.framework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import io.github.pangju666.framework.boot.web.interceptor.RateLimitInterceptor;
import io.github.pangju666.framework.boot.web.limit.RateLimiter;
import io.github.pangju666.framework.boot.web.limit.impl.IpRateLimitSourceExtractor;
import io.github.pangju666.framework.web.exception.base.BaseHttpException;
import io.github.pangju666.framework.web.model.Result;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * 速率限制器自动配置
 * <p>
 * 提供限流功能的自动配置入口：启用属性绑定、导入限流器实现配置，
 * 并注册基于 IP 的请求源提取器。优先于 Web MVC 自动配置执行。
 * </p>
 * <p><b>生效条件</b></p>
 * <ul>
 *   <li>Servlet Web 应用（{@link ConditionalOnWebApplication}，类型为 {@code SERVLET}）</li>
 *   <li>类路径存在 {@link jakarta.servlet.Servlet} 与 {@link DispatcherServlet}</li>
 *   <li>在 {@link WebMvcAutoConfiguration} 之前加载（{@code @AutoConfiguration(before = ...)}）</li>
 * </ul>
 * <p><b>行为说明</b></p>
 * <ul>
 *   <li>启用 {@link RateLimitProperties} 属性绑定</li>
 *   <li>导入 {@link Resilience4jRateLimiterConfiguration} 与 {@link RedissonRateLimiterConfiguration}</li>
 *   <li>实际生效的限流实现由各自配置类的条件与 {@code type} 属性共同决定</li>
 *   <li>注册 {@link IpRateLimitSourceExtractor}，供 {@link RateLimitInterceptor} 在 {@code SOURCE} 作用域下使用</li>
 * </ul>
 * <p><b>注意事项</b></p>
 * <ul>
 *   <li>{@code @RateLimit(source = ...)} 指定的类必须是 Spring Bean，拦截器从容器中获取</li>
 *   <li>当限流注解的 {@code interval < 1} 时，具体实现通常视为“不启用限流”（非阻塞），详见各实现类 Javadoc</li>
 * </ul>
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
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, Result.class, BaseHttpException.class})
@EnableConfigurationProperties(RateLimitProperties.class)
@Import({Resilience4jRateLimiterConfiguration.class, RedissonRateLimiterConfiguration.class})
class RateLimiterAutoConfiguration {
	/**
	 * 注册基于 IP 的限流源提取器
	 *
	 * @return {@link IpRateLimitSourceExtractor} 实例
	 * @see IpRateLimitSourceExtractor
	 * @since 1.0.0
	 */
	@Bean
	public IpRateLimitSourceExtractor ipRateLimitSourceExtractor() {
		return new IpRateLimitSourceExtractor();
	}

	@Order(Ordered.HIGHEST_PRECEDENCE + 2)
	@ConditionalOnBean(RateLimiter.class)
	@Bean
	public RateLimitInterceptor rateLimitInterceptor(RateLimiter rateLimiter) {
		return new RateLimitInterceptor(rateLimiter);
	}
}
