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

package io.github.pangju666.framework.boot.autoconfigure.web.signature;

import io.github.pangju666.framework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import io.github.pangju666.framework.boot.web.configuration.SignatureConfiguration;
import io.github.pangju666.framework.boot.web.interceptor.SignatureInterceptor;
import io.github.pangju666.framework.boot.web.signature.SecretKeyStorer;
import io.github.pangju666.framework.boot.web.signature.impl.DefaultSecretKeyStorer;
import io.github.pangju666.framework.web.model.Result;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * 签名功能自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>在 Servlet Web 应用中加载签名相关配置并提供必要 Bean。</li>
 *   <li>按需提供默认密钥存储器与签名拦截器，拦截器的实际注册由 {@link WebMvcAutoConfiguration} 统一完成。</li>
 * </ul>
 *
 * <p><strong>条件</strong></p>
 * <ul>
 *   <li>仅在 Servlet 类型的 Web 应用：{@link ConditionalOnWebApplication @ConditionalOnWebApplication(type = SERVLET)}。</li>
 *   <li>类路径存在核心类：{@link Servlet}、{@link DispatcherServlet}、{@link Result}（{@link ConditionalOnClass}）。</li>
 *   <li>启用配置绑定：{@link EnableConfigurationProperties @EnableConfigurationProperties}({@link SignatureProperties}).</li>
 * </ul>
 *
 * <p><strong>行为</strong></p>
 * <ul>
 *   <li>当上下文中缺少 {@link SecretKeyStorer} 时，注册 {@link DefaultSecretKeyStorer}，从 {@link SignatureProperties#getSecretKeys()} 加载密钥映射。</li>
 *   <li>当存在 {@link SecretKeyStorer} 时，创建并暴露 {@link SignatureInterceptor} Bean（优先级 {@link Ordered#HIGHEST_PRECEDENCE} + 1），其配置来自 {@link SignatureProperties}。</li>
 * </ul>
 *
 * <p><strong>备注</strong></p>
 * <ul>
 *   <li>签名拦截器的包含/排除路径与最终注册顺序由拦截器与 MVC 配置共同决定。</li>
 *   <li>密钥通过应用标识符（AppId）映射获取，请避免在日志中泄露敏感信息。</li>
 * </ul>
 *
 * @author pangju666
 * @see SignatureProperties
 * @see DefaultSecretKeyStorer
 * @see SecretKeyStorer
 * @see SignatureInterceptor
 * @see WebMvcAutoConfiguration
 * @since 1.0.0
 */
@AutoConfiguration(before = WebMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, Result.class})
@EnableConfigurationProperties(SignatureProperties.class)
public class SignatureAutoConfiguration {
	/**
	 * 注册默认签名密钥存储器。
	 * <p>
	 * 如果 Spring 上下文中没有实现 {@link SecretKeyStorer} 的 Bean，则会自动注册
	 * {@link DefaultSecretKeyStorer} 实例，并从签名配置中加载密钥映射关系。
	 * </p>
	 *
	 * @param properties 签名功能的配置属性 {@link SignatureProperties}。
	 * @return 默认签名密钥存储器 {@link DefaultSecretKeyStorer} 实例。
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(SecretKeyStorer.class)
	@Bean
	public DefaultSecretKeyStorer defaultSignatureSecretKeyStorer(SignatureProperties properties) {
		return new DefaultSecretKeyStorer(properties.getSecretKeys());
	}

	/**
	 * 注册签名拦截器 Bean。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>根据 {@link SignatureProperties} 构建 {@link SignatureConfiguration}，包含头部与参数字段名。</li>
	 *   <li>依赖 {@link SecretKeyStorer} 提供按 AppId 获取密钥的能力。</li>
	 *   <li>拦截器优先级为 {@link Ordered#HIGHEST_PRECEDENCE} + 1；实际添加到 MVC 由 {@link WebMvcAutoConfiguration} 统一处理。</li>
	 * </ul>
	 *
	 * @param secretKeyStorer 密钥存储器（按应用标识符提供签名密钥）
	 * @param properties      签名配置属性（头部与参数字段名、密钥映射等）
	 * @return 签名拦截器实例
	 * @since 1.0.0
	 */
	@Order(Ordered.HIGHEST_PRECEDENCE + 1)
	@ConditionalOnBean(SecretKeyStorer.class)
	@Bean
	public SignatureInterceptor signatureInterceptor(SecretKeyStorer secretKeyStorer, SignatureProperties properties) {
		SignatureConfiguration signatureConfiguration = new SignatureConfiguration();
		signatureConfiguration.setSignatureHeaderName(properties.getSignatureHeaderName());
		signatureConfiguration.setAppIdHeaderName(properties.getAppIdHeaderName());
		signatureConfiguration.setTimestampHeaderName(properties.getTimestampHeaderName());
		signatureConfiguration.setSignatureParamName(properties.getSignatureParamName());
		signatureConfiguration.setAppIdParamName(properties.getAppIdParamName());
		return new SignatureInterceptor(signatureConfiguration, secretKeyStorer);
	}
}
