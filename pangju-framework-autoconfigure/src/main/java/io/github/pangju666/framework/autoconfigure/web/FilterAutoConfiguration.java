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

package io.github.pangju666.framework.autoconfigure.web;

import io.github.pangju666.framework.autoconfigure.web.exception.HttpExceptionInfoFilter;
import io.github.pangju666.framework.autoconfigure.web.exception.HttpExceptionInfoProperties;
import io.github.pangju666.framework.web.filter.ContentCachingWrapperHttpRequestFilter;
import io.github.pangju666.framework.web.pool.WebConstants;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.Collections;

/**
 * Web过滤器自动配置类
 * <p>
 * 该配置类用于在Spring Boot应用启动时自动注册和配置Web应用中常用的过滤器，包括：
 * <ul>
 *     <li>跨域资源共享(CORS)过滤器 - 用于处理跨域请求</li>
 *     <li>内容缓存包装过滤器 - 用于缓存请求和响应内容以支持多次读取</li>
 *     <li>Http异常信息过滤器 - 用于获取Http异常类型列表和Http异常列表</li>
 * </ul>
 * </p>
 * <p>
 * 配置特性：
 * <ul>
 *     <li>仅在Servlet Web应用环境中生效</li>
 *     <li>通过条件注解自动判断是否注册对应过滤器</li>
 *     <li>支持用户自定义过滤器时的优雅降级</li>
 *     <li>过滤器执行顺序经过优化设计，确保正确的执行流程</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see ContentCachingWrapperHttpRequestFilter
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@EnableConfigurationProperties(HttpExceptionInfoProperties.class)
public class FilterAutoConfiguration {
	/**
	 * 配置跨域资源共享(CORS)过滤器
	 * <p>
	 * 创建并注册一个CORS过滤器，用于处理跨域HTTP请求。该过滤器配置允许：
	 * <ul>
	 *     <li>所有来源（Origin）的请求</li>
	 *     <li>所有HTTP头信息</li>
	 *     <li>所有HTTP方法（GET、POST、PUT、DELETE等）</li>
	 *     <li>发送凭证信息（Credentials）的跨域请求</li>
	 * </ul>
	 * 过滤器的执行优先级设置为{@link Ordered#HIGHEST_PRECEDENCE} + 1，确保在大多数其他过滤器之前执行。
	 * </p>
	 * <p>
	 * 条件激活：
	 * <ul>
	 *     <li>类路径中存在{@link org.springframework.web.filter.CorsFilter}类</li>
	 *     <li>Spring容器中不存在该类型的过滤器bean</li>
	 * </ul>
	 * </p>
	 *
	 * @return CORS过滤器的注册Bean，包含过滤器实例和执行顺序配置
	 * @since 1.0.0
	 */
	@ConditionalOnMissingFilterBean
	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOriginPattern("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration(WebConstants.ANY_PATH_PATTERN, config);

		FilterRegistrationBean<CorsFilter> filterRegistrationBean = new FilterRegistrationBean<>(new CorsFilter(source));
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
		return filterRegistrationBean;
	}

	/**
	 * 配置内容缓存包装过滤器
	 * <p>
	 * 创建并注册一个内容缓存包装过滤器，用于缓存HTTP请求和响应的内容体。
	 * 该过滤器通过包装ServletRequest和ServletResponse对象，使得请求和响应的内容流可以被多次读取，
	 * 这对以下场景非常有用：
	 * <ul>
	 *     <li>记录完整的HTTP请求和响应日志</li>
	 *     <li>审计敏感操作的详细信息</li>
	 *     <li>在多个处理层之间共享请求/响应内容</li>
	 *     <li>实现请求/响应的持久化存储</li>
	 * </ul>
	 * 过滤器应用于所有URL路径（/*），执行优先级设置为{@link Ordered#HIGHEST_PRECEDENCE} + 2，
	 * 确保在CORS过滤器之后但在业务处理之前执行。
	 * </p>
	 * <p>
	 * 条件激活：
	 * <ul>
	 *     <li>类路径中存在{@link ContentCachingWrapperHttpRequestFilter}类</li>
	 *     <li>Spring容器中不存在该类型的过滤器bean</li>
	 * </ul>
	 * </p>
	 *
	 * @return 内容缓存包装过滤器的注册Bean，包含过滤器实例、URL模式和执行顺序配置
	 * @since 1.0.0
	 */
	@ConditionalOnClass(ContentCachingWrapperHttpRequestFilter.class)
	@ConditionalOnMissingFilterBean
	@Bean
	public FilterRegistrationBean<ContentCachingWrapperHttpRequestFilter> contentCachingWrapperHttpRequestFilterRegistrationBean() {
		ContentCachingWrapperHttpRequestFilter contentCachingWrapperFilter = new ContentCachingWrapperHttpRequestFilter(Collections.emptySet());
		FilterRegistrationBean<ContentCachingWrapperHttpRequestFilter> filterRegistrationBean = new FilterRegistrationBean<>(contentCachingWrapperFilter);
		filterRegistrationBean.addUrlPatterns("/*");
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
		return filterRegistrationBean;
	}

	/**
	 * 配置 HttpExceptionInfoFilter 过滤器。
	 * <p>
	 * 该过滤器用于提供 HTTP 异常信息的访问端点，包括：
	 * <ul>
	 *     <li>异常类型列表：返回系统中已定义的所有异常类型。</li>
	 *     <li>异常信息列表：返回所有使用 {@link io.github.pangju666.framework.web.annotation.HttpException}
	 *     注解标注的异常类信息。</li>
	 * </ul>
	 * 配置通过 {@link HttpExceptionInfoProperties} 提供，允许自定义异常访问的路径和扫描的包范围，
	 * 从而灵活扩展异常信息统计。
	 * </p>
	 * <p>
	 * 过滤器的执行优先级设置为 {@link Ordered#HIGHEST_PRECEDENCE} + 3，确保在主要业务逻辑之前拦截并处理请求。
	 * </p>
	 *
	 * @param properties {@link HttpExceptionInfoProperties} 配置类，用于指定过滤器的异常访问路径、扫描包范围等
	 * @return 注册完成的 {@link FilterRegistrationBean} 实例，包含过滤器实例及其配置
	 * @since 1.0.0
	 */
	@ConditionalOnBooleanProperty(prefix = "pangju.web.exception.info", name = "enabled", matchIfMissing = true)
	@ConditionalOnMissingFilterBean
	@Bean
	public FilterRegistrationBean<HttpExceptionInfoFilter> httpExceptionInfoFilterFilterRegistrationBean(HttpExceptionInfoProperties properties) {
		HttpExceptionInfoFilter httpExceptionInfoFilter = new HttpExceptionInfoFilter(
			properties.getRequestPath().getTypes(), properties.getRequestPath().getList(),
			properties.getPackages());
		FilterRegistrationBean<HttpExceptionInfoFilter> filterRegistrationBean = new FilterRegistrationBean<>(httpExceptionInfoFilter);
		filterRegistrationBean.addUrlPatterns(properties.getRequestPath().getTypes(), properties.getRequestPath().getList());
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 3);
		return filterRegistrationBean;
	}
}
