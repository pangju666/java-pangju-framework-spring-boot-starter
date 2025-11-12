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

package io.github.pangju666.framework.boot.autoconfigure.web.log;

import io.github.pangju666.framework.boot.web.log.configuration.WebLogConfiguration;
import io.github.pangju666.framework.boot.web.log.filter.WebLogFilter;
import io.github.pangju666.framework.boot.web.log.interceptor.WebLogInterceptor;
import io.github.pangju666.framework.boot.web.log.sender.WebLogSender;
import io.github.pangju666.framework.web.lang.WebConstants;
import jakarta.servlet.Servlet;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 日志自动配置。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>装配 Web 日志相关组件，统一采集 HTTP 请求/响应日志并通过所选通道发送或落库。</li>
 *   <li>在满足条件时注册全局日志过滤器 {@link WebLogFilter}；引入各通道的自动配置。</li>
 * </ul>
 *
 * <p><b>条件</b></p>
 * <ul>
 *   <li>仅在 Servlet Web 环境下：{@link ConditionalOnWebApplication @ConditionalOnWebApplication(type = SERVLET)}。</li>
 *   <li>必须存在核心 Web 相关类：{@link Servlet}、{@link DispatcherServlet}、{@link WebMvcConfigurer}。</li>
 *   <li>功能开关：{@code pangju.web.log.enabled=true}。</li>
 * </ul>
 *
 * <p><b>行为</b></p>
 * <ul>
 *   <li>导入通道自动配置：{@link DisruptorSenderConfiguration}、{@link KafkaSenderConfiguration}、{@link MongoReceiverConfiguration}。</li>
 *   <li>在存在 {@link WebLogSender} 且未注册其他日志过滤器时，注册 {@link WebLogFilter}。</li>
 *   <li>过滤器应用所有 URL（{@link WebConstants#FILTER_ANY_URL_PATTERN}），优先级为最高优先级 + 2。</li>
 *   <li>支持按路径模式排除采集：读取 {@link WebLogProperties#getExcludePathPatterns()}。</li>
 * </ul>
 *
 * <p><b>配置</b></p>
 * <ul>
 *   <li>属性前缀：{@code pangju.web.log}。</li>
 *   <li>关键项：{@code enabled}、{@code sender-type}、Kafka/Mongo 目标配置、{@code exclude-path-patterns}。</li>
 * </ul>
 *
 * <p><b>示例（application.yml）</b></p>
 * <pre>
 * pangju:
 *   web:
 *     log:
 *       enabled: true
 *       sender-type: DISRUPTOR
 *       exclude-path-patterns:
 *         - /actuator/**
 *         - /swagger-ui/**
 *         - /v3/api-docs/**
 * </pre>
 *
 * <p><b>备注</b></p>
 * <ul>
 *   <li>此类本身不创建具体的发送/接收实现，相关 Bean 由被导入的自动配置提供。</li>
 *   <li>属性键采用 kebab-case（如 {@code exclude-path-patterns}）。</li>
 * </ul>
 *
 * @author pangju666
 * @see WebLogProperties
 * @see WebLogInterceptor
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.log", name = "enabled")
@Import({DisruptorSenderConfiguration.class, KafkaSenderConfiguration.class, MongoReceiverConfiguration.class})
@EnableConfigurationProperties(WebLogProperties.class)
public class WebLogAutoConfiguration {
	/**
	 * 注册全局 Web 日志过滤器。
	 *
	 * <p><b>条件</b></p>
	 * <ul>
	 *   <li>存在日志发送器 {@link WebLogSender}。</li>
	 *   <li>当前应用尚未注册其他 {@link WebLogFilter}。</li>
	 * </ul>
	 *
	 * <p><b>行为</b></p>
	 * <ul>
	 *   <li>将 {@link WebLogProperties} 拷贝为 {@link WebLogConfiguration}。</li>
	 *   <li>创建 {@link WebLogFilter}，传入配置、发送器与 {@code excludePathPatterns}。</li>
	 *   <li>注册为 {@link FilterRegistrationBean}，应用所有 URL，设置优先级。</li>
	 * </ul>
	 *
	 * @param properties   Web 日志属性配置
	 * @param webLogSender 日志发送器
	 * @return 用于注册日志过滤器的注册 Bean
	 * @since 1.0.0
	 */
	@ConditionalOnBean({WebLogSender.class})
	@ConditionalOnMissingFilterBean
	@Bean
	public FilterRegistrationBean<WebLogFilter> webLogFilterRegistrationBean(WebLogProperties properties,
																			 WebLogSender webLogSender) {
		WebLogConfiguration configuration = new WebLogConfiguration();
		BeanUtils.copyProperties(properties, configuration);

		WebLogFilter webLogFilter = new WebLogFilter(configuration, webLogSender, properties.getExcludePathPatterns());
		FilterRegistrationBean<WebLogFilter> filterRegistrationBean = new FilterRegistrationBean<>(webLogFilter);
		filterRegistrationBean.addUrlPatterns(WebConstants.FILTER_ANY_URL_PATTERN);
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
		return filterRegistrationBean;
	}
}
