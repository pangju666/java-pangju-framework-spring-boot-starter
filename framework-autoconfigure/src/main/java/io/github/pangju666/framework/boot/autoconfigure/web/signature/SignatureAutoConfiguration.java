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
import io.github.pangju666.framework.boot.web.signature.storer.SignatureSecretKeyStorer;
import io.github.pangju666.framework.boot.web.signature.storer.impl.DefaultSignatureSecretKeyStorer;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * 签名功能的自动配置类。
 * <p>
 * 该类提供签名校验功能的自动化配置，主要作用是初始化签名密钥存储器，并加载签名功能相关的配置项。
 * 仅在 Servlet 类型的 Web 应用环境下生效。
 * </p>
 *
 * <p>配置条件：</p>
 * <ul>
 *     <li>当前是 Servlet 类型的 Web 应用。</li>
 *     <li>类路径中存在 {@code Servlet} 和 {@code DispatcherServlet}。</li>
 * </ul>
 *
 * <p>主要功能：</p>
 * <ul>
 *     <li>加载用户定义的签名配置 {@link SignatureProperties}，从配置文件提取签名字段名称及密钥信息。</li>
 *     <li>注册默认签名密钥存储器 {@link DefaultSignatureSecretKeyStorer}，用于根据应用 ID 动态加载签名密钥。</li>
 * </ul>
 *
 * @author pangju666
 * @see SignatureProperties
 * @see DefaultSignatureSecretKeyStorer
 * @see SignatureSecretKeyStorer
 * @since 1.0.0
 */
@AutoConfiguration(before = WebMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@EnableConfigurationProperties(SignatureProperties.class)
public class SignatureAutoConfiguration {
	/**
	 * 注册默认签名密钥存储器。
	 * <p>
	 * 如果 Spring 上下文中没有实现 {@link SignatureSecretKeyStorer} 的 Bean，则会自动注册
	 * {@link DefaultSignatureSecretKeyStorer} 实例，并从签名配置中加载密钥映射关系。
	 * </p>
	 *
	 * @param properties 签名功能的配置属性 {@link SignatureProperties}。
	 * @return 默认签名密钥存储器 {@link DefaultSignatureSecretKeyStorer} 实例。
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(SignatureSecretKeyStorer.class)
	@Bean
	public DefaultSignatureSecretKeyStorer defaultSignatureSecretKeyStorer(SignatureProperties properties) {
		return new DefaultSignatureSecretKeyStorer(properties.getSecretKeys());
	}
}
