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

package io.github.pangju666.framework.autoconfigure.web.log;

import io.github.pangju666.framework.autoconfigure.web.log.filter.WebLogFilter;
import io.github.pangju666.framework.autoconfigure.web.log.handler.WebLogHandler;
import io.github.pangju666.framework.autoconfigure.web.log.revceiver.WebLogReceiver;
import io.github.pangju666.framework.autoconfigure.web.log.sender.WebLogSender;
import io.github.pangju666.framework.autoconfigure.web.log.sender.impl.disruptor.DisruptorWebLogEventHandler;
import io.github.pangju666.framework.autoconfigure.web.log.sender.impl.disruptor.DisruptorWebLogSender;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collections;
import java.util.List;

/**
 * Web 日志自动配置类
 * <p>
 * 该类用于自动配置与 Web 日志收集和处理相关的组件，包括 Disruptor 日志事件处理器、
 * 日志发送器、日志过滤器等。根据配置和环境条件，动态注册所需的 Bean。
 * </p>
 *
 * <p>功能说明：</p>
 * <ul>
 *     <li>自动注册 Web 日志收集的核心组件，例如日志事件处理器、发送器等。</li>
 *     <li>支持基于 Disruptor 的高性能日志处理架构。</li>
 *     <li>注册全局日志过滤器 {@link WebLogFilter}，用于统一拦截与处理 Web 请求与响应日志。</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>统一日志收集和处理的 Web 应用。</li>
 *     <li>高吞吐量日志传递需求的应用（如基于 Disruptor 的日志队列）。</li>
 * </ul>
 *
 * <p>实现逻辑：</p>
 * <ul>
 *     <li>判断是否为 Web 应用环境，通过 {@link ConditionalOnWebApplication} 激活。</li>
 *     <li>根据配置动态注册不同的组件，例如：
 *         <ul>
 *             <li>注册 Disruptor 日志事件处理器 {@link DisruptorWebLogEventHandler}。</li>
 *             <li>注册 Disruptor 日志发送器 {@link DisruptorWebLogSender}。</li>
 *         </ul>
 *     </li>
 *     <li>添加日志过滤器 {@link WebLogFilter}，拦截所有 HTTP 请求以采集日志数据。</li>
 * </ul>
 *
 * <p>关键配置项：</p>
 * <pre>
 * pangju:
 *   web:
 *     log:
 *       enabled: true                 # 是否启用 Web 日志功能（默认启用）
 *       sender-type: DISRUPTOR        # 日志发送类型（默认使用 Disruptor）
 *       kafka:
 *         topic: web-log-topic        # Kafka 日志 Topic
 *       mongo:
 *         collection-prefix: web_log  # MongoDB 日志集合前缀
 * </pre>
 *
 * @author pangju666
 * @see WebLogProperties
 * @see WebLogFilter
 * @see DisruptorWebLogEventHandler
 * @see DisruptorWebLogSender
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.log", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(WebLogProperties.class)
public class WebLogAutoConfiguration {
	/**
	 * 注册 Disruptor 日志事件处理器
	 * <p>
	 * 当将日志发送类型配置为 `DISRUPTOR`，且启用 Web 日志功能时，
	 * 自动注册 {@link DisruptorWebLogEventHandler}，用于消费 Disruptor 队列内的日志事件。
	 * </p>
	 *
	 * @param webLogReceiver 日志接收器 {@link WebLogReceiver}
	 * @return Disruptor 日志事件处理器
	 * @since 1.0.0
	 */
	@ConditionalOnProperty(prefix = "pangju.web.log", name = "sender-type", havingValue = "DISRUPTOR", matchIfMissing = true)
	@ConditionalOnMissingBean(DisruptorWebLogEventHandler.class)
	@ConditionalOnBean(WebLogReceiver.class)
	@Bean
	public DisruptorWebLogEventHandler disruptorWebLogEventHandler(WebLogReceiver webLogReceiver) {
		return new DisruptorWebLogEventHandler(webLogReceiver);
	}

	/**
	 * 注册 Disruptor 日志发送器
	 * <p>
	 * 当将日志发送类型配置为 `DISRUPTOR` 且存在 {@link DisruptorWebLogEventHandler} 时，
	 * 自动注册 {@link DisruptorWebLogSender}，用于将日志事件发送至 Disruptor 队列。
	 * </p>
	 *
	 * @param properties    Web 日志属性配置 {@link WebLogProperties}
	 * @param eventHandler  Disruptor 日志事件处理器
	 * @return Disruptor 日志发送器
	 * @since 1.0.0
	 */
	@ConditionalOnProperty(prefix = "pangju.web.log", name = "sender-type", havingValue = "DISRUPTOR", matchIfMissing = true)
	@ConditionalOnBean(DisruptorWebLogEventHandler.class)
	@ConditionalOnMissingBean(WebLogSender.class)
	@Bean
	public DisruptorWebLogSender disruptorWebLogSender(WebLogProperties properties, DisruptorWebLogEventHandler eventHandler) {
		return new DisruptorWebLogSender(properties, eventHandler);
	}

	/**
	 * 注册全局 Web 日志过滤器
	 * <p>
	 * 注册 {@link WebLogFilter}，用于拦截 HTTP 请求并采集请求和响应日志。
	 * 仅当存在日志发送器 {@link WebLogSender} 且尚未注册其他日志过滤器时生效。
	 * </p>
	 *
	 * @param properties                  Web 日志属性配置 {@link WebLogProperties}
	 * @param webLogSender                日志发送器 {@link WebLogSender}
	 * @param webLogHandlers              日志处理器列表 {@link WebLogHandler}
	 * @param requestMappingHandlerMapping 请求映射处理器 {@link RequestMappingHandlerMapping}
	 * @return 用于注册日志过滤器的 {@link FilterRegistrationBean}
	 * @since 1.0.0
	 */
	@ConditionalOnBean({WebLogSender.class})
	@ConditionalOnMissingFilterBean
	@Bean
	public FilterRegistrationBean<WebLogFilter> webLogFilterRegistrationBean(WebLogProperties properties,
																			 WebLogSender webLogSender,
																			 List<WebLogHandler> webLogHandlers,
																			 RequestMappingHandlerMapping requestMappingHandlerMapping) {
		WebLogFilter webLogFilter = new WebLogFilter(properties, webLogSender, Collections.emptySet(),
			webLogHandlers, requestMappingHandlerMapping);
		FilterRegistrationBean<WebLogFilter> filterRegistrationBean = new FilterRegistrationBean<>(webLogFilter);
		filterRegistrationBean.addUrlPatterns("/*");
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 3);
		return filterRegistrationBean;
	}
}
