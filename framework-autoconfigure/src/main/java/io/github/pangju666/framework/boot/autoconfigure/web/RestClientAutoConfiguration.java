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

import io.github.pangju666.framework.web.client.BufferingResponseInterceptor;
import io.github.pangju666.framework.web.client.RestRequestBuilder;
import io.github.pangju666.framework.web.model.Result;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/**
 * RestClient 自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>在应用启动时自动创建并配置 {@link RestClient} Bean。</li>
 *   <li>通过注入的 {@link RestClient.Builder} 构建可用的客户端实例。</li>
 * </ul>
 *
 * <p><strong>加载顺序</strong></p>
 * <ul>
 *   <li>在 {@code org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration} 之后加载，确保框架默认配置先完成。</li>
 * </ul>
 *
 * <p><strong>生效条件</strong></p>
 * <ul>
 *   <li>Classpath 中存在 {@link RestClient} 类（Spring Framework 6.1+）。</li>
 *   <li>容器中存在 {@link RestClient.Builder} Bean。</li>
 *   <li>容器中不存在已定义的 {@link RestClient} Bean。</li>
 * </ul>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>统一提供 HTTP 客户端访问方式，简化 REST API 调用。</li>
 *   <li>注册 {@link BufferingResponseInterceptor} 作为首个响应拦截器，便于多次读取响应内容。</li>
 * </ul>
 *
 * @author pangju666
 * @see RestClient
 * @see RestRequestBuilder
 * @since 1.0.0
 */
@AutoConfiguration(after = org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class)
@ConditionalOnClass({RestClient.class, BufferingResponseInterceptor.class, Result.class})
public class RestClientAutoConfiguration {
    /**
     * 创建并注册 {@link RestClient} Bean。
     *
     * <p><strong>行为描述</strong></p>
     * <ul>
     *   <li>基于注入的 {@link RestClient.Builder} 构建客户端实例。</li>
     *   <li>将 {@link BufferingResponseInterceptor} 置于响应拦截器链首位，支持重复读取响应体。</li>
     * </ul>
     *
     * <p><strong>执行条件</strong></p>
     * <ul>
     *   <li>存在 {@link RestClient.Builder} Bean。</li>
     *   <li>容器中不存在已定义的 {@link RestClient} Bean。</li>
     * </ul>
     *
     * @param builder Spring 提供的 RestClient 构建器
     * @return 配置完成的 RestClient 实例
     * @see RestClient.Builder
     * @since 1.0.0
     */
	@ConditionalOnBean(RestClient.Builder.class)
	@ConditionalOnMissingBean(RestClient.class)
	@Bean
	public RestClient restClient(RestClient.Builder builder) {
		return builder.requestInterceptors(interceptors ->
			interceptors.add(0, new BufferingResponseInterceptor()))
			.build();
	}
}
