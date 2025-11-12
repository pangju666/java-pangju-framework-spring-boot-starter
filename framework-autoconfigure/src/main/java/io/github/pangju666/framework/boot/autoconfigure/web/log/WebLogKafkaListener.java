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
import io.github.pangju666.framework.boot.web.log.revceiver.WebLogReceiver;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
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
 *   <li>当消息体为 {@code null} 时跳过处理，但仍进行应答；非空时调用接收器处理。</li>
 *   <li>处理完成后调用 {@link Acknowledgment#acknowledge()} 手动应答。</li>
 * </ul>
 *
 * <p><b>约束</b></p>
 * <ul>
 *   <li>容器需配置为手动应答模式（例如 {@code AckMode.MANUAL/ MANUAL_IMMEDIATE}）。</li>
 *   <li>生产端与消费端的消息序列化/反序列化需与 {@link WebLog} 类型匹配。</li>
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
     * 监听 Kafka Topic 并消费 Web 日志。
     *
     * <p><b>行为</b></p>
     * <ul>
     *   <li>从 {@link ConsumerRecord} 读取日志对象，判空后交由 {@link WebLogReceiver} 处理。</li>
     *   <li>无论消息体是否为空，都会在末尾调用 {@link Acknowledgment#acknowledge()} 进行手动应答。</li>
     * </ul>
     *
     * <p><b>参数</b></p>
     * <ul>
     *   <li>{@code record} Kafka 消息记录，键为 {@code String}，值为 {@link WebLog}。</li>
     *   <li>{@code ack} 手动应答对象；需将容器 Ack 模式配置为手动。</li>
     * </ul>
     *
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
