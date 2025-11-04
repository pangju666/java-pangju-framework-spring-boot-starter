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
import io.github.pangju666.framework.boot.web.log.revceiver.WebLogReceiver;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Objects;

/**
 * Web 日志 Kafka 消费监听器
 * <p>
 * 该类用于监听指定的 Kafka Topic，消费其中发布的 {@link WebLog} 日志数据。
 * 消费后的日志数据将通过 {@link WebLogReceiver} 进行业务处理（如持久化存储或分析处理）。
 * </p>
 *
 * <p>功能说明：</p>
 * <ul>
 *     <li>通过 {@code @KafkaListener} 注解监听 Kafka 日志 Topic。</li>
 *     <li>消费 Kafka 消息队列中的日志记录并调用 {@link WebLogReceiver} 处理日志。</li>
 *     <li>支持手动应答机制（Acknowledgment），确保消费的消息可以精确处理。</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>适用于基于 Kafka 的分布式日志收集和处理架构。</li>
 *     <li>可将日志数据从 Kafka 消息队列消费后，存储至持久化系统或提交分析平台。</li>
 * </ul>
 *
 * <p>实现逻辑：</p>
 * <ul>
 *     <li>监听配置的 Topic（通过配置项 {@code pangju.web.log.kafka.topic}）。</li>
 *     <li>从 Kafka 消息获取日志记录 {@link WebLog}，并交由 {@link WebLogReceiver} 进行处理。</li>
 *     <li>手动确认消息处理完成后，调用 {@link Acknowledgment#acknowledge()} 以应答成功消费。</li>
 * </ul>
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
	 * 监听 Kafka Topic，消费 Web 日志记录
	 * <p>
	 * 被 {@link KafkaListener} 注解标记的方法会监听配置的 Kafka 日志 Topic，处理其中的日志数据。
	 * 消费的消息会调用接收器 {@link WebLogReceiver#receive(WebLog)} 进行日志处理。
	 * </p>
	 *
	 * @param record Kafka 消息记录，包含日志数据 {@link WebLog}
	 * @param ack    手动应答对象，用于确认消息消费已完成
	 * @since 1.0.0
	 */
	@KafkaListener(topics = "${pangju.web.log.kafka.topic}")
	public void listenRequestLog(ConsumerRecord<String, WebLog> record, Acknowledgment ack) {
		WebLog webLog = record.value();
		if (Objects.nonNull(webLog)) {
			receiver.receive(webLog);
		}
		ack.acknowledge();
	}
}
