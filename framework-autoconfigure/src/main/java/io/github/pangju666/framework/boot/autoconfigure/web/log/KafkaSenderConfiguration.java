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

import io.github.pangju666.framework.boot.web.log.receiver.WebLogReceiver;
import io.github.pangju666.framework.boot.web.log.sender.WebLogSender;
import io.github.pangju666.framework.boot.web.log.sender.impl.kafka.KafkaWebLogSender;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;

/**
 * 基于 Kafka 的 Web 日志发送器自动配置。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>在选择 Kafka 作为日志发送通道时，自动注册生产端与消费端组件。</li>
 *   <li>与 Spring Kafka 集成，支持分布式日志采集与处理。</li>
 * </ul>
 *
 * <p><b>条件</b></p>
 * <ul>
 *   <li>在 {@link KafkaAutoConfiguration} 之后加载。</li>
 *   <li>类路径存在 {@link KafkaTemplate}。</li>
 *   <li>属性 {@code pangju.web.log.sender-type} 为 {@code KAFKA}（大小写敏感）。</li>
 * </ul>
 *
 * <p><b>注册的 Bean</b></p>
 * <ul>
 *   <li>{@link KafkaWebLogSender}：从容器中获取 {@link KafkaTemplate}，将日志发送至配置的 Topic。</li>
 *   <li>{@link WebLogKafkaListener}：订阅配置的 Topic，消费 {@link io.github.pangju666.framework.boot.web.log.model.WebLog} 并交由接收器处理。</li>
 * </ul>
 *
 * <p><b>关键配置</b></p>
 * <pre>
 * pangju:
 *   web:
 *     log:
 *       enabled: true
 *       sender-type: KAFKA
 *       kafka:
 *         topic: web-log-topic
 *         kafka-template-ref: myKafkaTemplate  # 可选，指定 KafkaTemplate Bean 名称
 * </pre>
 *
 * <p><b>说明</b></p>
 * <ul>
 *   <li>{@code havingValue = "KAFKA"} 为字符串匹配，需使用常量大写值。</li>
 *   <li>若未指定 {@code kafka-template-ref}，将回退获取容器默认的 {@link KafkaTemplate}。</li>
 * </ul>
 *
 * @author pangju666
 * @see WebLogSender
 * @see KafkaWebLogSender
 * @see WebLogKafkaListener
 * @since 1.0.0
 */
@AutoConfiguration(after = KafkaAutoConfiguration.class)
@ConditionalOnClass({KafkaTemplate.class})
@ConditionalOnProperty(prefix = "pangju.web.log", name = "kafka.topic")
@ConditionalOnProperty(prefix = "pangju.web.log", name = "sender-type", havingValue = "KAFKA")
class KafkaSenderConfiguration {
	/**
	 * 注册 Kafka 日志发送器。
	 *
	 * <p><b>条件</b></p>
	 * <ul>
	 *   <li>容器中存在 {@link KafkaTemplate}。</li>
	 *   <li>当前未存在 {@link WebLogSender} Bean。</li>
	 *   <li>已配置有效的 Topic（{@code pangju.web.log.kafka.topic}）。</li>
	 * </ul>
	 *
	 * <p><b>行为</b></p>
	 * <ul>
	 *   <li>根据 {@code kafka-template-ref} 指定的 Bean 名称优先获取 {@link KafkaTemplate}；否则回退到默认 Bean。</li>
	 *   <li>使用配置的 Topic 创建 {@link KafkaWebLogSender}。</li>
	 * </ul>
	 *
	 * <p><b>说明</b></p>
	 * <ul>
	 *   <li>若未指定 {@code kafka-template-ref}，将使用容器默认的 {@link KafkaTemplate}。</li>
	 * </ul>
	 *
	 * @param properties  Web 日志属性配置
	 * @param beanFactory BeanFactory，用于按名称或类型获取 {@link KafkaTemplate}
	 * @return 发送器实例
	 * @since 1.0.0
	 */
	@SuppressWarnings("unchecked")
	@ConditionalOnMissingBean(WebLogSender.class)
	@ConditionalOnBean(KafkaTemplate.class)
	@Bean
	public KafkaWebLogSender kafkaWebLogSender(WebLogProperties properties, BeanFactory beanFactory) {
		KafkaTemplate<Object, Object> kafkaTemplate;
		if (StringUtils.hasText(properties.getKafka().getKafkaTemplateRef())) {
			kafkaTemplate = beanFactory.getBean(properties.getKafka().getKafkaTemplateRef(), KafkaTemplate.class);
		} else {
			kafkaTemplate = beanFactory.getBean(KafkaTemplate.class);
		}
		return new KafkaWebLogSender(kafkaTemplate, properties.getKafka().getTopic());
	}

    /**
     * 注册 Kafka 日志消费监听器。
     *
     * <p><b>条件</b></p>
     * <ul>
     *   <li>配置存在有效的 Topic（{@code pangju.web.log.kafka.topic}）。</li>
     *   <li>容器中存在 {@link WebLogReceiver}和{@link KafkaWebLogSender}。</li>
     * </ul>
     *
     * <p><b>行为</b></p>
     * <ul>
     *   <li>创建 {@link WebLogKafkaListener}，用于订阅并消费日志消息后委托接收器处理。</li>
     * </ul>
     *
     * @param webLogReceiver 日志接收器
     * @return 监听器实例
     * @since 1.0.0
     */
	@ConditionalOnBean({WebLogReceiver.class, KafkaWebLogSender.class})
	@Bean
	public WebLogKafkaListener webLogKafkaListener(WebLogReceiver webLogReceiver) {
		return new WebLogKafkaListener(webLogReceiver);
	}
}
