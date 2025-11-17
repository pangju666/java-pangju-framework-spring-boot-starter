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

package io.github.pangju666.framework.boot.autoconfigure.web;

import io.github.pangju666.framework.boot.autoconfigure.web.exception.HttpExceptionInfoProperties;
import io.github.pangju666.framework.web.lang.WebConstants;
import io.github.pangju666.framework.web.model.Result;
import io.github.pangju666.framework.web.servlet.filter.HttpExceptionInfoFilter;
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
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.ArrayList;
import java.util.List;

/**
 * Web 过滤器自动配置。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>在满足条件时自动注册常用过滤器：跨域（CORS）与 HTTP 异常信息。</li>
 *   <li>通过条件注解避免与用户自定义过滤器冲突，保持优雅降级。</li>
 * </ul>
 *
 * <p><b>条件</b></p>
 * <ul>
 *   <li>仅在 Servlet Web 环境下：{@link ConditionalOnWebApplication @ConditionalOnWebApplication(type = SERVLET)}。</li>
 *   <li>必须存在核心 Web 类：{@link Servlet}、{@link DispatcherServlet}。</li>
 *   <li>异常信息过滤器需启用：{@code pangju.web.exception.info.enabled=true}（默认启用）。</li>
 * </ul>
 *
 * <p><b>行为</b></p>
 * <ul>
 *   <li>注册全局 CORS 过滤器，允许任意来源、请求头、方法，并携带凭证；顺序为最高优先级 + 1。</li>
 *   <li>注册 HTTP 异常信息过滤器，提供“异常类型列表/异常列表”端点；顺序为最高优先级 + 3。</li>
 * </ul>
 *
 * <p><b>配置</b></p>
 * <ul>
 *   <li>异常信息配置前缀：{@code pangju.web.exception.info}。</li>
 *   <li>关键项：{@code enabled}、{@code request-path.types}、{@code request-path.list}、{@code packages}。</li>
 * </ul>
 *
 * <p><b>示例（application.yml）</b></p>
 * <pre>
 * pangju:
 *   web:
 *     exception:
 *       info:
 *         enabled: true
 *         request-path:
 *           types: /exception/types
 *           list: /exception/list
 *         packages:
 *           - com.example.app.common.exception
 *           - com.example.app.feature.exception
 * </pre>
 *
 * @author pangju666
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, Result.class})
@EnableConfigurationProperties(HttpExceptionInfoProperties.class)
public class FilterAutoConfiguration {
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

	/**
	 * 注册 HTTP 异常信息过滤器。
	 *
	 * <p><b>条件</b></p>
	 * <ul>
	 *   <li>配置开启：{@code pangju.web.exception.info.enabled=true}（{@link ConditionalOnBooleanProperty}）。</li>
	 *   <li>当前容器中不存在同类过滤器 Bean（{@link ConditionalOnMissingFilterBean}）。</li>
	 * </ul>
	 *
	 * <p><b>行为</b></p>
	 * <ul>
	 *   <li>聚合默认扫描包与用户配置包 {@link HttpExceptionInfoProperties#getPackages()}。</li>
	 *   <li>使用 {@link HttpExceptionInfoProperties.Path#getTypes()} 与 {@link HttpExceptionInfoProperties.Path#getList()} 构建端点。</li>
	 *   <li>按这两个端点路径注册 URL 模式，顺序：{@link Ordered#HIGHEST_PRECEDENCE} + 3。</li>
	 * </ul>
	 *
	 * @param properties 异常信息属性配置
	 * @return 过滤器注册 Bean
	 * @since 1.0.0
	 */
	@ConditionalOnBooleanProperty(prefix = "pangju.web.exception.info", name = "enabled", matchIfMissing = true)
	@ConditionalOnMissingFilterBean
	@Bean
	public FilterRegistrationBean<HttpExceptionInfoFilter> httpExceptionInfoFilterFilterRegistrationBean(HttpExceptionInfoProperties properties) {
		Assert.hasText(properties.getRequestPath().getTypes(), "异常类型汇总接口路径不可为空");
		Assert.hasText(properties.getRequestPath().getList(), "异常列表查询接口路径不可为空");

		List<String> packages = new ArrayList<>(3);
		packages.add("io.github.pangju666.framework.boot.web.idempotent.exception");
		packages.add("io.github.pangju666.framework.boot.web.limit.exception");
		if (!CollectionUtils.isEmpty(properties.getPackages())) {
			packages.addAll(properties.getPackages());
		}
		HttpExceptionInfoFilter httpExceptionInfoFilter = new HttpExceptionInfoFilter(
			properties.getRequestPath().getTypes(), properties.getRequestPath().getList(), packages);
		FilterRegistrationBean<HttpExceptionInfoFilter> filterRegistrationBean = new FilterRegistrationBean<>(httpExceptionInfoFilter);
		filterRegistrationBean.addUrlPatterns(properties.getRequestPath().getTypes(), properties.getRequestPath().getList());
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 3);
		return filterRegistrationBean;
	}
}
