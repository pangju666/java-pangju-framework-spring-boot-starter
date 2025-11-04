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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * 基于 Kafka 的 Web 日志发送器
 * <p>
 * 该类是 {@link WebLogSender} 的实现，使用 Spring Kafka 提供的 {@link KafkaTemplate}，
 * 将采集到的 {@link WebLog} 日志数据发送到 Kafka 消息队列的指定 Topic 中。
 * </p>
 *
 * <p>功能说明：</p>
 * <ul>
 *     <li>支持使用 Kafka 作为日志中间件，提供日志的分布式异步传输能力。</li>
 *     <li>根据 {@link WebLogProperties} 配置动态选择 KafkaTemplate 和目标 Topic。</li>
 *     <li>在发送过程中若出现异常，会记录错误日志以便排查问题。</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>适用于日志分布式消费场景，例如日志集中存储于数据库或 Kafka 接入的实时分析平台（如 Elasticsearch, Logstash, Kibana, 即 ELK）。</li>
 *     <li>需要高可靠性和扩展性的日志传输处理场景。</li>
 * </ul>
 *
 * <p>实现逻辑：</p>
 * <ul>
 *     <li>通过 {@link BeanFactory} 注入或动态选择 KafkaTemplate 实例。</li>
 *     <li>从配置 {@link WebLogProperties} 中获取目标 Kafka Topic 和相关参数。</li>
 *     <li>调用 {@link KafkaTemplate#send(String, Object)} 将日志数据发送至目标 Topic。</li>
 * </ul>
 *
 * @author pangju666
 * @see WebLogSender
 * @see WebLog
 * @see KafkaTemplate
 * @since 1.0.0
 */
public class KafkaWebLogSender implements WebLogSender {
	private static final Logger log = LoggerFactory.getLogger(KafkaWebLogSender.class);

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final String topic;

	public KafkaWebLogSender(KafkaTemplate<String, Object> kafkaTemplate, String topic) {
		this.kafkaTemplate = kafkaTemplate;
		this.topic = topic;
	}

	/**
	 * 发送 Web 日志至 Kafka
	 * <p>
	 * 利用 KafkaTemplate 的 {@link KafkaTemplate#send(String, Object)} 方法，
	 * 将当前收集的日志推送至 Kafka 消息队列的指定 Topic 中。
	 * 若操作失败，会捕获异常并记录错误日志。
	 * </p>
	 *
	 * @param webLog 待发送的 Web 日志数据 {@link WebLog}
	 */
	@Override
	public void send(WebLog webLog) {
		try {
			kafkaTemplate.send(topic, webLog);
		} catch (RuntimeException e) {
			log.error("接口请求信息发送至Kafka消息队列失败", e);
		}
	}
}
