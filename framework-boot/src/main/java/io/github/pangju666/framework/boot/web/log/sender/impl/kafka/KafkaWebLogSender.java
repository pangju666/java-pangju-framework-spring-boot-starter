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

package io.github.pangju666.framework.boot.web.log.sender.impl.kafka;

import io.github.pangju666.framework.boot.web.log.model.WebLog;
import io.github.pangju666.framework.boot.web.log.sender.WebLogSender;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * 基于 Kafka 的 Web 日志发送器。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>{@link WebLogSender} 的实现，使用 Spring Kafka 提供的 {@link KafkaTemplate} 异步发送 {@link WebLog} 到指定 Topic。</li>
 *   <li>适合将 Web 日志投入消息管道以供集中存储或实时分析。</li>
 * </ul>
 *
 * <p><b>行为</b></p>
 * <ul>
 *   <li>调用 {@link KafkaTemplate#send(String, Object)} 进行异步发送；返回的 Future 不在当前实现中处理。</li>
 *   <li>主题名称通过构造函数注入；序列化器需在 {@code KafkaTemplate} 中预先配置以支持 {@code WebLog} 序列化。</li>
 * </ul>
 *
 * <p><b>注意事项</b></p>
 * <ul>
 *   <li>该实现不包含重试、确认回调或失败落盘等可靠性策略；如需保证投递可靠性，请在外部配置回调或拦截器。</li>
 *   <li>确保 {@code topic} 非空且对应的 Kafka 主题已创建，避免发送失败。</li>
 * </ul>
 *
 * @author pangju666
 * @see WebLogSender
 * @see WebLog
 * @see KafkaTemplate
 * @since 1.0.0
 */
public class KafkaWebLogSender implements WebLogSender {
	private final KafkaTemplate<Object, Object> kafkaTemplate;
	private final String topic;

	public KafkaWebLogSender(KafkaTemplate<Object, Object> kafkaTemplate, String topic) {
		this.kafkaTemplate = kafkaTemplate;
		this.topic = topic;
	}

	/**
	 * 发送 Web 日志到 Kafka。
	 *
	 * @param webLog 待发送的 Web 日志数据
	 */
	@Override
	public void send(WebLog webLog) {
		kafkaTemplate.send(topic, webLog);
	}
}
