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

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import io.github.pangju666.framework.boot.web.log.revceiver.WebLogReceiver;
import io.github.pangju666.framework.boot.web.log.sender.WebLogSender;
import io.github.pangju666.framework.boot.web.log.sender.impl.disruptor.DisruptorWebLogEventHandler;
import io.github.pangju666.framework.boot.web.log.sender.impl.disruptor.DisruptorWebLogSender;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 基于 Disruptor 的 Web 日志发送器自动配置。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>当启用 Web 日志并选择 Disruptor 作为发送通道时，自动注册相关处理与发送组件。</li>
 *   <li>提供高吞吐、低延迟的日志事件传递能力，缓冲区大小由配置项决定。</li>
 * </ul>
 *
 * <p><b>条件</b></p>
 * <ul>
 *   <li>类路径存在 Disruptor 相关库：{@link Disruptor}、{@link EventHandler}。</li>
 *   <li>属性 {@code pangju.web.log.sender-type} 的值为 {@code DISRUPTOR}，或未显式配置（默认启用）。</li>
 *   <li>容器中存在 {@link WebLogReceiver}（日志接收器）。</li>
 * </ul>
 *
 * <p><b>注册的 Bean</b></p>
 * <ul>
 *   <li>{@link DisruptorWebLogEventHandler}：消费队列中的日志事件并调用接收器处理。</li>
 *   <li>{@link DisruptorWebLogSender}：将日志事件发布到 Disruptor 队列，缓冲区大小来自 {@link WebLogProperties.Disruptor#getBufferSize()}。</li>
 * </ul>
 *
 * <p><b>配置</b></p>
 * <ul>
 *   <li>{@code pangju.web.log.sender-type}：取值需与枚举常量一致（建议使用 {@code DISRUPTOR} 大写）。</li>
 *   <li>{@code pangju.web.log.disruptor.buffer-size}：环形缓冲区大小，影响吞吐与内存占用。</li>
 * </ul>
 *
 * <p><b>说明</b></p>
 * <ul>
 *   <li>{@link ConditionalOnProperty} 为字符串匹配，大小写敏感；为确保命中，请使用常量 {@code DISRUPTOR}。</li>
 *   <li>若未配置 {@code sender-type}，因 {@code matchIfMissing=true} 将默认启用 Disruptor 发送链路。</li>
 * </ul>
 *
 * @author pangju666
 * @see WebLogProperties
 * @see Disruptor
 * @see EventHandler
 * @see DisruptorWebLogEventHandler
 * @see DisruptorWebLogSender
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Disruptor.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.log", name = "enabled")
@ConditionalOnProperty(prefix = "pangju.web.log", name = "sender-type", havingValue = "DISRUPTOR", matchIfMissing = true)
class DisruptorSenderConfiguration {
    /**
     * 注册 Disruptor 日志事件处理器。
     *
     * <p><b>条件</b></p>
     * <ul>
     *   <li>容器中存在 {@link WebLogReceiver}。</li>
     *   <li>当前未存在 {@link DisruptorWebLogEventHandler} Bean。</li>
     * </ul>
     *
     * <p><b>行为</b></p>
     * <ul>
     *   <li>创建事件处理器，用于从队列中消费日志事件并委托至接收器处理。</li>
     * </ul>
     *
     * @param webLogReceiver 日志接收器
     * @return 事件处理器实例
     * @since 1.0.0
     */
	@ConditionalOnMissingBean(DisruptorWebLogEventHandler.class)
	@ConditionalOnBean(WebLogReceiver.class)
	@Bean
	public DisruptorWebLogEventHandler disruptorWebLogEventHandler(WebLogReceiver webLogReceiver) {
		return new DisruptorWebLogEventHandler(webLogReceiver);
	}

    /**
     * 注册 Disruptor 日志发送器。
     *
     * <p><b>条件</b></p>
     * <ul>
     *   <li>容器中存在 {@link DisruptorWebLogEventHandler}。</li>
     *   <li>当前未存在 {@link WebLogSender} Bean。</li>
     * </ul>
     *
     * <p><b>行为</b></p>
     * <ul>
     *   <li>根据 {@link WebLogProperties.Disruptor#getBufferSize()} 创建发送器并绑定事件处理器。</li>
     * </ul>
     *
     * @param properties   Web 日志属性配置
     * @param eventHandler 事件处理器
     * @return 发送器实例
     * @since 1.0.0
     */
	@ConditionalOnBean(DisruptorWebLogEventHandler.class)
	@ConditionalOnMissingBean(WebLogSender.class)
	@Bean
	public DisruptorWebLogSender disruptorWebLogSender(WebLogProperties properties, DisruptorWebLogEventHandler eventHandler) {
		return new DisruptorWebLogSender(properties.getDisruptor().getBufferSize(), eventHandler);
	}
}
