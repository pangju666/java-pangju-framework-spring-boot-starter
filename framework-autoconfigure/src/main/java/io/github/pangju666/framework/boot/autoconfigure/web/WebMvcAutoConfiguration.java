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

import io.github.pangju666.framework.boot.web.resolver.EnumRequestParamArgumentResolver;
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
 *   <li>增强 Spring MVC：注册常用参数解析器与用户自定义拦截器。</li>
 *   <li>支持扩展拦截器（{@link BaseHttpInterceptor}）的动态注册，按需增强请求处理能力。</li>
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
 *   <li>参数解析：始终注册 {@link EnumRequestParamArgumentResolver}，并追加由构造方法注入的外部参数解析器集合。</li>
 *   <li>拦截器：遍历并注册通过依赖注入提供的 {@link BaseHttpInterceptor}，应用其包含/排除路径与执行顺序（{@link BaseHttpInterceptor#getOrder()}）。</li>
 * </ul>
 *
 * @author pangju666
 * @see WebMvcConfigurer
 * @see BaseHttpInterceptor
 * @see EnumRequestParamArgumentResolver
 * @see HandlerMethodArgumentResolver
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
     * @param interceptors 自定义 HTTP 拦截器列表
     * @param resolvers    外部参数解析器列表
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
     *   <li>遍历并注册通过依赖注入提供的 {@link BaseHttpInterceptor}。</li>
     *   <li>应用拦截器自身的包含路径（{@link BaseHttpInterceptor#getPatterns()}）与排除路径（{@link BaseHttpInterceptor#getExcludePathPatterns()}）。</li>
     *   <li>按拦截器定义的顺序（{@link BaseHttpInterceptor#getOrder()}）进行注册。</li>
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
