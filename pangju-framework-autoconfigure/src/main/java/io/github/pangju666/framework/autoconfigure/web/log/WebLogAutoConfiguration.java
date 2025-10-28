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

import io.github.pangju666.framework.autoconfigure.web.log.config.DisruptorSenderConfiguration;
import io.github.pangju666.framework.autoconfigure.web.log.config.KafkaSenderConfiguration;
import io.github.pangju666.framework.autoconfigure.web.log.config.MongoReceiverConfiguration;
import io.github.pangju666.framework.autoconfigure.web.log.filter.WebLogFilter;
import io.github.pangju666.framework.autoconfigure.web.log.handler.WebLogHandler;
import io.github.pangju666.framework.autoconfigure.web.log.sender.WebLogSender;
import io.github.pangju666.framework.autoconfigure.web.log.sender.impl.disruptor.DisruptorWebLogEventHandler;
import io.github.pangju666.framework.autoconfigure.web.log.sender.impl.disruptor.DisruptorWebLogSender;
import jakarta.servlet.Servlet;
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
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collections;
import java.util.List;

/**
 * WebLogAutoConfiguration
 * <p>
 * 该类为 Web 日志功能的核心自动配置类，主要用于自动装配 Web 日志相关的组件，简化日志收集与处理的配置流程。
 * 它支持多种日志处理方案，如基于 Disruptor 的高性能处理、发送到 Kafka 或存储到 MongoDB。
 * </p>
 *
 * <h3>核心功能</h3>
 * <ul>
 *     <li>自动注册日志事件处理器与发送器，例如：
 *         <ul>
 *             <li>Disruptor 日志事件处理器 {@link DisruptorWebLogEventHandler} 和发送器 {@link DisruptorWebLogSender}。</li>
 *             <li>Kafka 日志发送处理器。</li>
 *             <li>MongoDB 日志接收处理器。</li>
 *         </ul>
 *     </li>
 *     <li>注册全局日志拦截过滤器 {@link WebLogFilter}，实现统一 HTTP 请求与响应日志的采集。</li>
 *     <li>支持条件化装配，通过 Spring 的条件注解动态注入所需组件。</li>
 * </ul>
 *
 * <h3>常见使用场景</h3>
 * <ul>
 *     <li>需要统一日志集中采集和分析的 Web 项目。</li>
 *     <li>需要高吞吐量日志处理能力的项目（如使用 Disruptor 实现低延迟的日志传递）。</li>
 *     <li>支持分布式日志传递，借助 Kafka 或 MongoDB 实现日志的持久化与异步处理。</li>
 * </ul>
 *
 * <h3>核心配置说明</h3>
 * <p>通过以下配置项定义日志功能：</p>
 * <pre>
 * pangju:
 *   web:
 *     log:
 *       enabled: true                 # 是否启用 Web 日志功能，默认启用
 *       sender-type: DISRUPTOR        # 日志发送模式，可选值为 "DISRUPTOR"、"KAFKA"、"MONGO"，默认使用 Disruptor
 *       kafka:
 *         topic: web-log-topic        # Kafka 日志主题配置
 *       mongo:
 *         collection-prefix: web_log  # MongoDB 日志集合前缀配置
 * </pre>
 *
 * <h3>装配逻辑</h3>
 * <ul>
 *     <li>通过 {@link ConditionalOnWebApplication} 注解确保仅在 Web 环境中生效。</li>
 *     <li>基于配置动态注册特定类型的日志组件，如 Disruptor、Kafka 或 MongoDB。</li>
 *     <li>通过 {@link ConditionalOnBooleanProperty} 注解判断日志功能是否启用。</li>
 * </ul>
 *
 * <h3>依赖自动装配的相关组件</h3>
 * 包含以下依赖模块的自动配置逻辑：
 * <ul>
 *     <li>{@link DisruptorSenderConfiguration}：基于 Disruptor 的日志发送器配置。</li>
 *     <li>{@link KafkaSenderConfiguration}：Kafka 日志发送配置。</li>
 *     <li>{@link MongoReceiverConfiguration}：MongoDB 日志接收配置。</li>
 * </ul>
 *
 * @author pangju666
 * @see WebLogProperties
 * @see WebLogFilter
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.log", name = "enabled", matchIfMissing = true)
@Import({DisruptorSenderConfiguration.class, KafkaSenderConfiguration.class, MongoReceiverConfiguration.class})
@EnableConfigurationProperties(WebLogProperties.class)
public class WebLogAutoConfiguration {
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
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 4);
		return filterRegistrationBean;
	}
}
