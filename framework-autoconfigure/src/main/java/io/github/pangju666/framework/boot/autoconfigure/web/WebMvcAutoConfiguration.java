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
import io.github.pangju666.framework.boot.autoconfigure.web.signature.SignatureProperties;
import io.github.pangju666.framework.boot.web.limit.interceptor.RateLimitInterceptor;
import io.github.pangju666.framework.boot.web.limit.limiter.RateLimiter;
import io.github.pangju666.framework.boot.web.log.interceptor.WebLogInterceptor;
import io.github.pangju666.framework.boot.web.resolver.EncryptRequestParamArgumentResolver;
import io.github.pangju666.framework.boot.web.resolver.EnumRequestParamArgumentResolver;
import io.github.pangju666.framework.boot.web.signature.configuration.SignatureConfiguration;
import io.github.pangju666.framework.boot.web.signature.interceptor.SignatureInterceptor;
import io.github.pangju666.framework.boot.web.signature.storer.SignatureSecretKeyStorer;
import io.github.pangju666.framework.web.servlet.BaseHttpInterceptor;
import jakarta.servlet.Servlet;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Web MVC 自动配置。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>增强 Spring MVC：注册常用拦截器与参数解析器，实现统一的请求/响应处理。</li>
 *   <li>支持扩展拦截器（{@link BaseHttpInterceptor}）的动态注册，便于按需增强。</li>
 * </ul>
 *
 * <p><b>条件</b></p>
 * <ul>
 *   <li>仅在 Servlet Web 环境：{@link ConditionalOnWebApplication @ConditionalOnWebApplication(type = SERVLET)}。</li>
 *   <li>必须存在核心类：{@link Servlet}、{@link DispatcherServlet}、{@link WebMvcConfigurer}。</li>
 * </ul>
 *
 * <p><b>行为</b></p>
 * <ul>
 *   <li>参数解析：注册枚举解析器与（按需）加密参数解析器。</li>
 *   <li>拦截器：在具备依赖时注册签名校验与限流拦截器；仅当启用 Web 日志功能时注册 Web 日志拦截器并应用排除路径（拦截器构造不携带排除路径，统一由注册表的 {@code excludePathPatterns} 应用）。</li>
 *   <li>自定义：遍历并注册用户扩展的 {@link BaseHttpInterceptor}，按其定义的顺序执行。</li>
 * </ul>
 *
 * <p><b>配置示例（application.yml）</b></p>
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
 * <p><b>备注</b></p>
 * <ul>
 *   <li>加密参数解析器按需注册：仅当类路径存在所需加密 Key 类型时才启用。</li>
 *   <li>仅当 {@link WebLogProperties#isEnabled()} 为 {@code true} 时注册 Web 日志拦截器；排除路径来自 {@link WebLogProperties#getExcludePathPatterns()}，并通过注册表的 {@code excludePathPatterns} 应用。</li>
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
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
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
     * 签名属性配置。
     *
     * <p>包含与签名校验相关的全局设置（如算法、校验策略等），用于 {@link SignatureInterceptor}。</p>
     *
     * @since 1.0.0
     */
    private final SignatureConfiguration signatureConfiguration;
    /**
     * 限流器。
     *
     * <p>提供请求限流能力，当存在时启用 {@link RateLimitInterceptor} 以按规则限制访问。</p>
     *
     * @since 1.0.0
     */
    private RateLimiter rateLimiter;
    /**
     * 签名密钥存储器。
     *
     * <p>管理客户端与服务端之间的密钥，用于签名验证；供 {@link SignatureInterceptor} 使用。</p>
     *
     * @since 1.0.0
     */
    private SignatureSecretKeyStorer secretKeyStorer;

    /**
     * Web 日志属性配置。
     *
     * <p>提供 Web 日志采集范围与排除路径等设置，供 {@link WebLogInterceptor} 使用。</p>
     *
     * @since 1.0.0
     */
    private final WebLogProperties webLogProperties;

    /**
     * 构造方法，初始化 Web MVC 配置。
     *
     * <p><b>行为</b></p>
     * <ul>
     *   <li>将外部 {@link SignatureProperties} 拷贝为运行时 {@link SignatureConfiguration}。</li>
     *   <li>保存 Web 日志属性与处理器集合，用于配置拦截器。</li>
     * </ul>
     *
     * @param signatureProperties 签名属性配置（外部化配置源）
     * @param webLogProperties    Web 日志属性配置
     * @param interceptors        自定义 HTTP 拦截器列表
     * @since 1.0.0
     */
    public WebMvcAutoConfiguration(SignatureProperties signatureProperties, WebLogProperties webLogProperties,
                                   List<BaseHttpInterceptor> interceptors) {
		this.interceptors = interceptors;
		this.webLogProperties = webLogProperties;

		SignatureConfiguration signatureConfiguration = new SignatureConfiguration();
		BeanUtils.copyProperties(signatureProperties, signatureConfiguration);
		this.signatureConfiguration = signatureConfiguration;
	}

	@Autowired(required = false)
	public void setRateLimiter(RateLimiter rateLimiter) {
		this.rateLimiter = rateLimiter;
	}

	@Autowired(required = false)
	public void setSecretKeyStorer(SignatureSecretKeyStorer secretKeyStorer) {
		this.secretKeyStorer = secretKeyStorer;
	}

    /**
     * 注册请求参数解析器。
     *
     * <p><b>行为</b></p>
     * <ul>
     *   <li>始终注册枚举参数解析器 {@link EnumRequestParamArgumentResolver}。</li>
     *   <li>按需注册加密参数解析器 {@link EncryptRequestParamArgumentResolver}（类路径存在所需加密 Key 时）。</li>
     * </ul>
     *
     * @param resolvers 参数解析器集合（由 MVC 框架注入）
     * @since 1.0.0
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new EnumRequestParamArgumentResolver());
		try {
			Class.forName("io.github.pangju666.commons.crypto.key.RSAKey");
			resolvers.add(new EncryptRequestParamArgumentResolver());
		} catch (ClassNotFoundException ignored) {
		}
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
		if (Objects.nonNull(secretKeyStorer)) {
			SignatureInterceptor signatureInterceptor = new SignatureInterceptor(signatureConfiguration, secretKeyStorer);
			registry.addInterceptor(signatureInterceptor)
				.addPathPatterns(signatureInterceptor.getPatterns())
				.excludePathPatterns(signatureInterceptor.getExcludePathPatterns());
		}

		if (Objects.nonNull(rateLimiter)) {
			RateLimitInterceptor rateLimitInterceptor = new RateLimitInterceptor(rateLimiter);
			registry.addInterceptor(rateLimitInterceptor)
				.addPathPatterns(rateLimitInterceptor.getPatterns())
				.excludePathPatterns(rateLimitInterceptor.getExcludePathPatterns());
		}

        // 仅当开启 Web 日志功能时注册拦截器；排除路径由注册表应用
        if (webLogProperties.isEnabled()) {
            WebLogInterceptor webLogInterceptor = new WebLogInterceptor(Collections.emptySet());
            registry.addInterceptor(webLogInterceptor)
                .addPathPatterns(webLogInterceptor.getPatterns())
                .excludePathPatterns(List.copyOf(webLogProperties.getExcludePathPatterns()));
	   }

		for (BaseHttpInterceptor interceptor : this.interceptors) {
			registry.addInterceptor(interceptor)
				.addPathPatterns(interceptor.getPatterns())
				.excludePathPatterns(interceptor.getExcludePathPatterns())
				.order(interceptor.getOrder());
		}
	}
}