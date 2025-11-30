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

import io.github.pangju666.framework.boot.autoconfigure.web.log.WebLogProperties;
import io.github.pangju666.framework.boot.web.crypto.EncryptRequestParamArgumentResolver;
import io.github.pangju666.framework.boot.web.limit.interceptor.RateLimitInterceptor;
import io.github.pangju666.framework.boot.web.log.interceptor.WebLogInterceptor;
import io.github.pangju666.framework.boot.web.resolver.EnumRequestParamArgumentResolver;
import io.github.pangju666.framework.boot.web.signature.interceptor.SignatureInterceptor;
import io.github.pangju666.framework.web.servlet.BaseHttpInterceptor;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>增强 Spring MVC：注册常用拦截器与参数解析器，实现统一的请求/响应处理。</li>
 *   <li>支持扩展拦截器（{@link BaseHttpInterceptor}）的动态注册，便于按需增强。</li>
 * </ul>
 *
 * <p><strong>条件</strong></p>
 * <ul>
 *   <li>仅在 Servlet Web 环境：{@link ConditionalOnWebApplication @ConditionalOnWebApplication(type = SERVLET)}。</li>
 *   <li>必须存在核心类：{@link Servlet}、{@link DispatcherServlet}、{@link WebMvcConfigurer}（{@link ConditionalOnClass}）。</li>
 * </ul>
 *
 * <p><strong>行为</strong></p>
 * <ul>
 *   <li>参数解析：始终注册 {@link EnumRequestParamArgumentResolver}，并追加由构造方法注入的外部解析器集合（例如加密参数解析器）。</li>
 *   <li>拦截器：在具备依赖时注册签名校验与限流拦截器；仅当启用 Web 日志功能时注册 Web 日志拦截器并应用排除路径（拦截器构造不携带排除路径，统一由注册表的 {@code excludePathPatterns} 应用）。</li>
 *   <li>自定义：遍历并注册用户扩展的 {@link BaseHttpInterceptor}，按其定义的 {@link BaseHttpInterceptor#getOrder()} 顺序。</li>
 * </ul>
 *
 * <p><strong>配置示例（application.yml）</strong></p>
 * <pre>
 * pangju:
 *   web:
 *     signature:
 *       secret-keys:
 *         app1: secretKey1
 *         app2: secretKey2
 *     log:
 *       enabled: true
 *       exclude-path-patterns:
 *         - /actuator/**
 *         - /swagger-ui/**
 * </pre>
 *
 * <p><strong>备注</strong></p>
 * <ul>
 *   <li>内置拦截器（签名/限流/日志）按注册表默认顺序执行；用户扩展拦截器通过其 {@code order} 控制执行顺序。</li>
 *   <li>Web 日志拦截器的排除路径来自 {@link WebLogProperties#getExcludePathPatterns()}，并由注册表应用。</li>
 * </ul>
 *
 * @author pangju666
 * @see WebMvcConfigurer
 * @see RateLimitInterceptor
 * @see SignatureInterceptor
 * @see EnumRequestParamArgumentResolver
 * @see EncryptRequestParamArgumentResolver
 * @since 1.0.0
 */
@AutoConfiguration(after = org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class, BaseHttpInterceptor.class})
public class WebMvcAutoConfiguration implements WebMvcConfigurer {
	/**
	 * 自定义 HTTP 请求拦截器列表
	 * <p>
	 * 该属性用于存储由用户自定义的拦截器，实现不同的请求处理逻辑增强功能。
	 * 每个拦截器需要继承 {@link BaseHttpInterceptor}，并定义其拦截逻辑。
	 * 在 MVC 配置中会自动注册这些自定义拦截器。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final List<BaseHttpInterceptor> interceptors;
	/**
	 * 外部参数解析器集合。
	 *
	 * <p>通过构造方法注入的解析器列表，将在
	 * {@link #addArgumentResolvers(List)} 中按序追加到 MVC 的参数解析器链，
	 * 用于扩展如加密参数、特殊类型解析等能力。</p>
	 *
	 * @since 1.0.0
	 */
	private final List<HandlerMethodArgumentResolver> resolvers;

    /**
     * 构造方法，初始化 Web MVC 配置。
     *
     * @param interceptors        自定义 HTTP 拦截器列表
     * @since 1.0.0
     */
    public WebMvcAutoConfiguration(List<BaseHttpInterceptor> interceptors, List<HandlerMethodArgumentResolver> resolvers) {
		this.interceptors = interceptors;
		this.resolvers = resolvers;
	}

    /**
     * 注册请求参数解析器。
     *
     * <p><b>流程</b>：注册枚举解析器 -> 追加外部解析器集合（通过构造方法注入，例如加密参数解析器）。</p>
     *
     * @param resolvers 参数解析器集合（由 MVC 框架注入）
     * @since 1.0.0
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new EnumRequestParamArgumentResolver());
		resolvers.addAll(this.resolvers);
	}

    /**
     * 注册 HTTP 请求拦截器。
     *
     * <p><b>行为</b></p>
     * <ul>
     *   <li>在存在密钥存储器时注册 {@link SignatureInterceptor}（应用其自身的包含/排除路径）。</li>
     *   <li>在存在限流器时注册 {@link RateLimitInterceptor}（应用其自身的包含/排除路径）。</li>
     *   <li>注册 {@link WebLogInterceptor}，并使用 {@link WebLogProperties#getExcludePathPatterns()} 作为排除路径。</li>
     *   <li>遍历并注册用户扩展的 {@link BaseHttpInterceptor}，按其定义的 {@link BaseHttpInterceptor#getOrder()} 顺序。</li>
     * </ul>
     *
     * @param registry MVC 拦截器注册表
     * @since 1.0.0
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
		for (BaseHttpInterceptor interceptor : this.interceptors) {
			registry.addInterceptor(interceptor)
				.addPathPatterns(interceptor.getPatterns())
				.excludePathPatterns(interceptor.getExcludePathPatterns())
				.order(interceptor.getOrder());
		}
	}
}
