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
import io.github.pangju666.framework.boot.web.log.handler.MediaTypeBodyHandler;
import io.github.pangju666.framework.boot.web.log.handler.WebLogHandler;
import io.github.pangju666.framework.boot.web.log.handler.impl.JsonBodyHandler;
import io.github.pangju666.framework.boot.web.log.handler.impl.TextBodyHandler;
import io.github.pangju666.framework.boot.web.log.interceptor.WebLogInterceptor;
import io.github.pangju666.framework.boot.web.log.sender.WebLogSender;
import io.github.pangju666.framework.web.lang.WebConstants;
import io.github.pangju666.framework.web.model.Result;
import jakarta.servlet.Servlet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
 *   <li>提供默认媒体类型处理器 Bean：{@link JsonBodyHandler}、{@link TextBodyHandler}，仅在缺失时注入（{@link ConditionalOnMissingBean}），用户可通过自定义同名 Bean 覆盖或扩展。</li>
 *   <li>将属性中的可接受媒体类型字符串安全解析为 {@link MediaType} 并去重，遇到非法值（{@link InvalidMediaTypeException}）将忽略并跳过，结果写入 {@link WebLogConfiguration}。</li>
 *   <li>过滤器解析请求/响应体时，按注入的 {@link MediaTypeBodyHandler} 列表顺序选择首个支持的处理器并在匹配后停止继续尝试（首匹配语义）。</li>
 *   <li>注入的 {@link WebLogHandler} 列表在过滤链出栈后按顺序执行以增强日志；单个处理器异常将被记录，不影响后续处理或响应。</li>
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
 * @see WebLogFilter
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class, Result.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.log", name = "enabled")
@Import({Slf4jReceiverConfiguration.class, MongoReceiverConfiguration.class, DisruptorSenderConfiguration.class, KafkaSenderConfiguration.class})
@EnableConfigurationProperties(WebLogProperties.class)
public class WebLogAutoConfiguration {
	/**
	 * 默认 JSON 媒体类型处理器。
	 *
	 * <p>仅在容器中不存在同类型 Bean 时注册（{@link ConditionalOnMissingBean}）。
	 * 在 {@link WebLogFilter} 中将与其它 {@link MediaTypeBodyHandler} 一同参与选择，
	 * 采用“按列表顺序首个支持即使用并停止”的语义。</p>
	 *
	 * @return 默认的 {@link JsonBodyHandler}
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(JsonBodyHandler.class)
	@Bean
	public JsonBodyHandler jsonBodyHandler() {
		return new JsonBodyHandler();
	}

	/**
	 * 默认文本媒体类型处理器。
	 *
	 * <p>仅在容器中不存在同类型 Bean 时注册（{@link ConditionalOnMissingBean}）。
	 * 在 {@link WebLogFilter} 中将与其它 {@link MediaTypeBodyHandler} 一同参与选择，
	 * 采用“按列表顺序首个支持即使用并停止”的语义”。</p>
	 *
	 * @return 默认的 {@link TextBodyHandler}
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(TextBodyHandler.class)
	@Bean
	public TextBodyHandler textBodyHandler() {
		return new TextBodyHandler();
	}

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
	 *   <li>解析并转换请求/响应的可接受媒体类型为 {@link MediaType} 列表：忽略非法值（{@link InvalidMediaTypeException}）、去重并写入配置。</li>
	 *   <li>创建 {@link WebLogFilter}，传入配置、发送器、{@code excludePathPatterns}、注入的 {@link MediaTypeBodyHandler} 列表与 {@link WebLogHandler} 列表。</li>
	 *   <li>过滤器将按处理器列表顺序选择首个支持的处理器并在成功解析后停止继续尝试（首匹配）。</li>
	 *   <li>注册为 {@link FilterRegistrationBean}，应用所有 URL，设置优先级。</li>
	 * </ul>
	 *
	 * @param properties     Web 日志属性配置
	 * @param webLogSender   日志发送器
	 * @param webLogHandlers 过滤链结束后按顺序执行的日志增强处理器列表（允许为空）
	 * @param bodyHandlers   媒体类型处理器列表（按顺序参与选择），允许为空
	 * @return 用于注册日志过滤器的注册 Bean
	 * @since 1.0.0
	 */
	@ConditionalOnBean(WebLogSender.class)
	@Bean
	public FilterRegistrationBean<WebLogFilter> webLogFilterRegistrationBean(WebLogProperties properties,
																			 WebLogSender webLogSender,
																			 List<WebLogHandler> webLogHandlers,
																			 List<MediaTypeBodyHandler> bodyHandlers) {
		WebLogConfiguration configuration = new WebLogConfiguration();
		BeanUtils.copyProperties(properties, configuration);

		List<MediaType> requestMediaTypes = Collections.emptyList();
		if (!CollectionUtils.isEmpty(properties.getRequest().getAcceptableMediaTypes())) {
			requestMediaTypes = properties.getRequest().getAcceptableMediaTypes()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(mediaType -> {
					try {
						return MediaType.parseMediaType(mediaType);
					} catch (InvalidMediaTypeException e) {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.distinct()
				.toList();
		}
		configuration.getRequest().setAcceptableMediaTypes(requestMediaTypes);

		List<MediaType> responseMediaTypes = Collections.emptyList();
		if (!CollectionUtils.isEmpty(properties.getResponse().getAcceptableMediaTypes())) {
			responseMediaTypes = properties.getResponse().getAcceptableMediaTypes()
				.stream()
				.filter(StringUtils::isNotBlank)
				.map(mediaType -> {
					try {
						return MediaType.parseMediaType(mediaType);
					} catch (InvalidMediaTypeException e) {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.distinct()
				.toList();
		}
		configuration.getResponse().setAcceptableMediaTypes(responseMediaTypes);

		WebLogFilter webLogFilter = new WebLogFilter(configuration, webLogSender, properties.getExcludePathPatterns(),
			bodyHandlers, webLogHandlers);
		FilterRegistrationBean<WebLogFilter> filterRegistrationBean = new FilterRegistrationBean<>(webLogFilter);
		filterRegistrationBean.addUrlPatterns(WebConstants.FILTER_ANY_URL_PATTERN);
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
		return filterRegistrationBean;
	}

	@Order(Ordered.HIGHEST_PRECEDENCE + 3)
	@Bean
	public WebLogInterceptor webLogInterceptor(WebLogProperties properties) {
		return new WebLogInterceptor(properties.getExcludePathPatterns());
	}
}
