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

import io.github.pangju666.framework.boot.web.log.model.WebLog;
import io.github.pangju666.framework.boot.web.log.receiver.WebLogReceiver;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Objects;

/**
 * Web 日志 Kafka 消费监听器。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>监听配置的 Kafka Topic，消费其中的 {@link WebLog} 消息。</li>
 *   <li>将消费到的日志委托给 {@link WebLogReceiver} 做进一步处理（如持久化/分析）。</li>
 * </ul>
 *
 * <p><b>行为</b></p>
 * <ul>
 *   <li>通过 {@link KafkaListener} 注解订阅 Topic：配置键 {@code pangju.web.log.kafka.topic}。</li>
 *   <li>当消息体为 {@code null} 时跳过处理；非空时交由接收器处理。</li>
 *   <li>在容器启用了手动应答模式（{@link org.springframework.kafka.listener.ContainerProperties.AckMode#MANUAL} 或 {@link org.springframework.kafka.listener.ContainerProperties.AckMode#MANUAL_IMMEDIATE}）时，存在非空的 {@link Acknowledgment} 参数，将在处理结束后调用 {@link Acknowledgment#acknowledge()} 进行手动提交。</li>
 * </ul>
 *
 * <p><b>约束</b></p>
 * <ul>
 *   <li>生产端与消费端的消息序列化/反序列化需与 {@link WebLog} 类型匹配。</li>
 * </ul>
 *
 * <p><b>配置示例</b></p>
 * <pre>
 * pangju:
 *   web:
 *     log:
 *       kafka:
 *         topic: weblog-topic
 * </pre>
 *
 * @author pangju666
 * @see WebLogReceiver
 * @see WebLog
 * @see KafkaListener
 * @since 1.0.0
 */
public class WebLogKafkaListener {
	/**
	 * Web 日志接收器
	 * <p>
	 * 定义用于接收和处理 Kafka 消费到的 Web 日志的业务逻辑。
	 * 实现类可以处理接收到的日志内容（如存储或分析）。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final WebLogReceiver receiver;

	/**
	 * 构造方法
	 * <p>
	 * 通过构造器注入 {@link WebLogReceiver}，用于处理消费的日志。
	 * </p>
	 *
	 * @param webLogReceiver 日志接收器，用于处理 Kafka 消息队列中的日志数据
	 * @since 1.0.0
	 */
	public WebLogKafkaListener(WebLogReceiver webLogReceiver) {
		this.receiver = webLogReceiver;
	}

	/**
	 * 监听 Kafka Topic 并消费 Web 日志。
	 *
	 * <p><b>行为</b></p>
	 * <ul>
	 *   <li>从 {@link ConsumerRecord} 读取日志对象，判空后交由 {@link WebLogReceiver} 处理。</li>
	 *   <li>当容器启用了手动应答模式（{@link ContainerProperties.AckMode#MANUAL} 或 {@link ContainerProperties.AckMode#MANUAL_IMMEDIATE}）时，在末尾调用 {@link Acknowledgment#acknowledge()} 进行手动应答。</li>
	 * </ul>
	 *
	 * <p><b>参数</b></p>
	 * <ul>
	 *   <li>{@code record} Kafka 消息记录。</li>
	 *   <li>{@code ack} 手动应答对象。</li>
	 * </ul>
	 *
	 * @since 1.0.0
	 */
	@KafkaListener(topics = "${pangju.web.log.kafka.topic}")
	public void listenRequestLog(ConsumerRecord<Object, Object> record, Acknowledgment ack) {
		if (Objects.nonNull(record.value()) && record.value() instanceof WebLog webLog) {
			receiver.receive(webLog);
		}
		if (Objects.nonNull(ack)) {
			ack.acknowledge();
		}
	}
}
