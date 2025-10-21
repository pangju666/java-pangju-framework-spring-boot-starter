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

package io.github.pangju666.framework.autoconfigure.web.log.config;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import io.github.pangju666.framework.autoconfigure.web.log.WebLogProperties;
import io.github.pangju666.framework.autoconfigure.web.log.revceiver.WebLogReceiver;
import io.github.pangju666.framework.autoconfigure.web.log.sender.WebLogSender;
import io.github.pangju666.framework.autoconfigure.web.log.sender.impl.disruptor.DisruptorWebLogEventHandler;
import io.github.pangju666.framework.autoconfigure.web.log.sender.impl.disruptor.DisruptorWebLogSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DisruptorSenderConfiguration
 * <p>
 * 本类为基于 Disruptor 的日志发送器配置类。在 Web 日志功能启用，并且配置的日志发送类型为 `DISRUPTOR` 时，
 * 自动装配 Disruptor 日志相关的组件，例如日志事件处理器和日志发送器。
 * </p>
 *
 * <h3>功能概述</h3>
 * <ul>
 *     <li>注册 {@link DisruptorWebLogEventHandler}：用于从 Disruptor 队列中消费日志事件并进行处理。</li>
 *     <li>注册 {@link DisruptorWebLogSender}：用于将日志事件发送至 Disruptor 队列。</li>
 * </ul>
 *
 * <h3>使用条件</h3>
 * <ul>
 *     <li>类路径中存在 Disruptor 相关库：{@link Disruptor} 和 {@link EventHandler}。</li>
 *     <li>配置属性 <code>pangju.web.log.sender-type</code> 设置为 `DISRUPTOR` (默认为 `DISRUPTOR`)。</li>
 *     <li>Spring 容器中存在 {@link WebLogReceiver} (日志接收器)。</li>
 * </ul>
 *
 * <h3>相关配置</h3>
 * 本配置类会根据以下条件判断是否加载：
 * <ul>
 *     <li>{@link ConditionalOnClass}：类路径中必须存在 Disruptor 相关类。</li>
 *     <li>{@link ConditionalOnProperty}：配置属性 `pangju.web.log.sender-type` 的值必须为 `DISRUPTOR`。</li>
 *     <li>{@link ConditionalOnBean} 和 {@link ConditionalOnMissingBean}：确保组件之间的依赖关系与唯一性。</li>
 * </ul>
 *
 * @author pangju666
 * @see Disruptor
 * @see EventHandler
 * @see DisruptorWebLogEventHandler
 * @see DisruptorWebLogSender
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Disruptor.class})
@ConditionalOnProperty(prefix = "pangju.web.log", name = "sender-type", havingValue = "DISRUPTOR", matchIfMissing = true)
public class DisruptorSenderConfiguration {
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
	@ConditionalOnBean(DisruptorWebLogEventHandler.class)
	@ConditionalOnMissingBean(WebLogSender.class)
	@Bean
	public DisruptorWebLogSender disruptorWebLogSender(WebLogProperties properties, DisruptorWebLogEventHandler eventHandler) {
		return new DisruptorWebLogSender(properties, eventHandler);
	}
}
