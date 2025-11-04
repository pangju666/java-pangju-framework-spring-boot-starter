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

import io.github.pangju666.framework.boot.autoconfigure.web.signature.SignatureProperties;
import io.github.pangju666.framework.boot.web.crypto.resolver.EncryptRequestParamArgumentResolver;
import io.github.pangju666.framework.boot.web.limit.interceptor.RateLimitInterceptor;
import io.github.pangju666.framework.boot.web.limit.limiter.RateLimiter;
import io.github.pangju666.framework.boot.web.resolver.EnumRequestParamArgumentResolver;
import io.github.pangju666.framework.boot.web.signature.configuration.SignatureConfiguration;
import io.github.pangju666.framework.boot.web.signature.interceptor.SignatureInterceptor;
import io.github.pangju666.framework.boot.web.signature.storer.SignatureSecretKeyStorer;
import io.github.pangju666.framework.web.servlet.interceptor.BaseHttpInterceptor;
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

import java.util.List;
import java.util.Objects;

/**
 * Web MVC 自动配置类。
 * <p>
 * 提供用于增强 Web MVC 配置的功能，通过自动注册常用的拦截器和参数解析器，
 * 实现对请求和响应的统一拦截和处理。
 * 仅在 Servlet 类型的 Web 应用环境下生效。
 * </p>
 *
 * <p>配置条件：</p>
 * <ul>
 *     <li>当前是 Servlet 类型的 Web 应用。</li>
 *     <li>类路径中存在 {@code Servlet}、{@code DispatcherServlet} 和 {@code WebMvcConfigurer}。</li>
 * </ul>
 *
 * <p>主要功能：</p>
 * <ul>
 *     <li>注册请求参数解析器，如 {@link EnumRequestParamArgumentResolver} 和 {@link EncryptRequestParamArgumentResolver}。</li>
 *     <li>注册拦截器，如限流拦截器 {@link RateLimitInterceptor}、签名校验拦截器 {@link SignatureInterceptor}。</li>
 *     <li>支持通过扩展 {@link BaseHttpInterceptor} 动态配置自定义拦截器。</li>
 * </ul>
 *
 * <p>配置示例：</p>
 * <pre>
 * pangju:
 *   web:
 *     signature:
 *       secret-keys:
 *         app1: secretKey1
 *         app2: secretKey2
 * </pre>
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
	 * 自定义 HTTP 请求拦截器列表
	 * <p>
	 * 该属性用于存储由用户自定义的拦截器，实现不同的请求处理逻辑增强功能。
	 * 每个拦截器需要继承 {@link BaseHttpInterceptor}，并定义其拦截逻辑。
	 * 在 MVC 配置中会自动注册这些自定义拦截器。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private RateLimiter rateLimiter;
	/**
	 * 签名密钥存储器
	 * <p>
	 * 管理客户端与服务端之间的密钥配置，用于签名验证。存储器可以根据应用配置动态获取不同
	 * 客户端（如 appId）对应的密钥。该属性会用于 {@link SignatureInterceptor} 进行签名校验。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private SignatureSecretKeyStorer secretKeyStorer;
	/**
	 * 签名属性配置
	 * <p>
	 * 包含与签名校验相关的全局配置信息，例如密钥存储位置、签名算法类型等。
	 * 这些配置支持通过外部文件（如 application.yml）加载，以便灵活调整。
	 * 在注册拦截器时，会通过 {@link SignatureInterceptor} 使用该配置。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final SignatureConfiguration signatureConfiguration;

	/**
	 * 构造方法，初始化 Web MVC 配置。
	 * <p>
	 * 注入必要的依赖，包括自定义拦截器列表、限流器、签名属性和密钥存储器。
	 * </p>
	 *
	 * @param interceptors 自定义拦截器列表，用于扩展 HTTP 请求处理。
	 * @since 1.0.0
	 */
	public WebMvcAutoConfiguration(SignatureProperties signatureProperties, List<BaseHttpInterceptor> interceptors) {
		this.interceptors = interceptors;

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
	 * <p>
	 * 向 MVC 上下文中添加自定义的参数解析器，用于处理特定类型的请求参数，如：
	 * <ul>
	 *     <li>{@link EnumRequestParamArgumentResolver}：解析枚举类型请求参数。</li>
	 *     <li>{@link EncryptRequestParamArgumentResolver}：解析加密请求参数。</li>
	 * </ul>
	 * </p>
	 *
	 * @param resolvers 参数解析器列表。
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
	 * <p>
	 * 向 MVC 的拦截器注册表中添加常用的拦截器，包括：
	 * <ul>
	 *     <li>{@link RateLimitInterceptor}：基于限流规则的请求拦截器。</li>
	 *     <li>{@link SignatureInterceptor}：基于签名校验的请求拦截器。</li>
	 * </ul>
	 * 同时支持自定义拦截器（继承自 {@link BaseHttpInterceptor}）的动态注册。
	 * </p>
	 *
	 * @param registry 拦截器注册表。
	 * @since 1.0.0
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		if (Objects.nonNull(rateLimiter)) {
			RateLimitInterceptor rateLimitInterceptor = new RateLimitInterceptor(rateLimiter);
			registry.addInterceptor(rateLimitInterceptor)
				.addPathPatterns(rateLimitInterceptor.getPatterns())
				.excludePathPatterns(rateLimitInterceptor.getExcludePathPatterns());
		}

		if (Objects.nonNull(secretKeyStorer)) {
			SignatureInterceptor signatureInterceptor = new SignatureInterceptor(signatureConfiguration, secretKeyStorer);
			registry.addInterceptor(signatureInterceptor)
				.addPathPatterns(signatureInterceptor.getPatterns())
				.excludePathPatterns(signatureInterceptor.getExcludePathPatterns());
		}

		for (BaseHttpInterceptor interceptor : this.interceptors) {
			registry.addInterceptor(interceptor)
				.addPathPatterns(interceptor.getPatterns())
				.excludePathPatterns(interceptor.getExcludePathPatterns())
				.order(interceptor.getOrder());
		}
	}
}