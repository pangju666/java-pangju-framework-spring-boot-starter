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

package io.github.pangju666.framework.boot.autoconfigure.web.cors;

import io.github.pangju666.framework.web.lang.WebConstants;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * 跨域（CORS）自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>在 Servlet Web 环境下自动注册 {@link CorsFilter}，统一启用跨域策略。</li>
 *   <li>默认允许任意来源、任意请求头与任意 HTTP 方法，并允许携带凭证。</li>
 * </ul>
 *
 * <p><strong>条件</strong></p>
 * <ul>
 *   <li>仅在 {@link ConditionalOnWebApplication.Type#SERVLET} 环境启用。</li>
 *   <li>类路径存在 {@link Servlet} 与 {@link DispatcherServlet}（{@link ConditionalOnClass}）。</li>
 *   <li>当前容器中不存在同类 {@link CorsFilter} Bean（{@link ConditionalOnMissingFilterBean}）。</li>
 * </ul>
 *
 * <p><strong>行为</strong></p>
 * <ul>
 *   <li>按 {@link WebConstants#ANT_ANY_PATH_PATTERN} 应用于所有路径。</li>
 *   <li>过滤器顺序：{@link Ordered#HIGHEST_PRECEDENCE} + 1。</li>
 *   <li>起步配置使用 {@link CorsConfiguration#addAllowedOriginPattern(String)} 通配来源（"*")。</li>
 * </ul>
 *
 * @author pangju666
 * @see CorsFilter
 * @see FilterRegistrationBean
 * @see UrlBasedCorsConfigurationSource
 * @see CorsConfiguration
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
public class CorsAutoConfiguration {
	/**
	 * 注册跨域（CORS）过滤器。
	 *
	 * <p><b>条件</b></p>
	 * <ul>
	 *   <li>当前容器中不存在同类 {@link CorsFilter} Bean（{@link ConditionalOnMissingFilterBean}）。</li>
	 * </ul>
	 *
	 * <p><b>行为</b></p>
	 * <ul>
	 *   <li>允许任意来源（Origin）、任意请求头与任意 HTTP 方法。</li>
	 *   <li>允许携带凭证（Credentials）。</li>
	 *   <li>按 {@link WebConstants#ANT_ANY_PATH_PATTERN} 注册到所有路径，顺序：{@link Ordered#HIGHEST_PRECEDENCE} + 1。</li>
	 * </ul>
	 *
	 * @return CORS 过滤器注册 Bean
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
		source.registerCorsConfiguration(WebConstants.ANT_ANY_PATH_PATTERN, config);

		FilterRegistrationBean<CorsFilter> filterRegistrationBean = new FilterRegistrationBean<>(new CorsFilter(source));
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
		return filterRegistrationBean;
	}
}
