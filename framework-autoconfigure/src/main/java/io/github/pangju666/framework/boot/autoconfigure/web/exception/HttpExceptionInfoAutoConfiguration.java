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

package io.github.pangju666.framework.boot.autoconfigure.web.exception;

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
import org.springframework.web.servlet.DispatcherServlet;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP 异常信息过滤器自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>自动注册 {@link HttpExceptionInfoFilter}，提供“异常类型汇总/异常列表”端点。</li>
 *   <li>通过条件注解避免与用户自定义过滤器冲突，保持优雅降级。</li>
 * </ul>
 *
 * <p><strong>条件</strong></p>
 * <ul>
 *   <li>仅在 Servlet Web 环境下：{@link ConditionalOnWebApplication @ConditionalOnWebApplication(type = SERVLET)}。</li>
 *   <li>类路径存在核心 Web 类：{@link Servlet}、{@link DispatcherServlet}（{@link ConditionalOnClass}）。</li>
 *   <li>启用配置属性绑定：{@link HttpExceptionInfoProperties}。</li>
 * </ul>
 *
 * <p><strong>行为</strong></p>
 * <ul>
 *   <li>按 {@link HttpExceptionInfoProperties.Path#getTypes()} 与 {@link HttpExceptionInfoProperties.Path#getList()} 注册 URL 模式。</li>
 *   <li>聚合默认扫描包与用户配置包 {@link HttpExceptionInfoProperties#getScanPackages()}。</li>
 *   <li>过滤器顺序：{@link Ordered#HIGHEST_PRECEDENCE} + 3。</li>
 * </ul>
 *
 * <p><strong>配置</strong></p>
 * <ul>
 *   <li>前缀：{@code pangju.web.exception.statistics}。</li>
 *   <li>关键项：{@code enabled}、{@code endpoints.types}、{@code endpoints.list}、{@code packages}。</li>
 * </ul>
 *
 * <p><strong>示例（application.yml）</strong></p>
 * <pre>
 * pangju:
 *   web:
 *     exception:
 *       statistics:
 *         enabled: true
 *         endpoints:
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
public class HttpExceptionInfoAutoConfiguration {
	/**
	 * 注册 HTTP 异常信息过滤器。
	 *
     * <p><b>条件</b></p>
     * <ul>
     *   <li>配置开启：{@code pangju.web.exception.statistics.enabled=true}（{@link ConditionalOnBooleanProperty}）。</li>
     *   <li>当前容器中不存在同类过滤器 Bean（{@link ConditionalOnMissingFilterBean}）。</li>
     * </ul>
	 *
	 * <p><b>行为</b></p>
	 * <ul>
	 *   <li>聚合默认扫描包与用户配置包 {@link HttpExceptionInfoProperties#getScanPackages()}。</li>
	 *   <li>使用 {@link HttpExceptionInfoProperties.Path#getTypes()} 与 {@link HttpExceptionInfoProperties.Path#getList()} 构建端点。</li>
	 *   <li>按这两个端点路径注册 URL 模式，顺序：{@link Ordered#HIGHEST_PRECEDENCE} + 3。</li>
	 * </ul>
	 *
	 * @param properties 异常信息属性配置
	 * @return 过滤器注册 Bean
	 * @since 1.0.0
	 */
	@ConditionalOnBooleanProperty(prefix = "pangju.web.exception.statistics", name = "enabled", matchIfMissing = true)
	@ConditionalOnMissingFilterBean
	@Bean
	public FilterRegistrationBean<HttpExceptionInfoFilter> httpExceptionInfoFilterFilterRegistrationBean(HttpExceptionInfoProperties properties) {
		Assert.hasText(properties.getEndpoints().getTypes(), "异常类型汇总接口路径不可为空");
		Assert.hasText(properties.getEndpoints().getList(), "异常列表查询接口路径不可为空");

		List<String> packages = new ArrayList<>(3);
		packages.add("io.github.pangju666.framework.boot.web.idempotent.exception");
		packages.add("io.github.pangju666.framework.boot.web.limit.exception");
		if (!CollectionUtils.isEmpty(properties.getScanPackages())) {
			packages.addAll(properties.getScanPackages());
		}

		HttpExceptionInfoFilter httpExceptionInfoFilter = new HttpExceptionInfoFilter(
			properties.getEndpoints().getTypes(), properties.getEndpoints().getList(), packages);
		FilterRegistrationBean<HttpExceptionInfoFilter> filterRegistrationBean = new FilterRegistrationBean<>(httpExceptionInfoFilter);
		filterRegistrationBean.addUrlPatterns(properties.getEndpoints().getTypes(), properties.getEndpoints().getList());
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 3);
		return filterRegistrationBean;
	}
}
