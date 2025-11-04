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

package io.github.pangju666.framework.boot.autoconfigure.web.log.config;

import io.github.pangju666.framework.boot.autoconfigure.web.log.WebLogProperties;
import io.github.pangju666.framework.boot.web.log.revceiver.WebLogReceiver;
import io.github.pangju666.framework.boot.web.log.sender.WebLogSender;
import io.github.pangju666.framework.boot.web.log.sender.impl.kafka.KafkaWebLogSender;
import io.github.pangju666.framework.boot.web.log.sender.impl.kafka.WebLogKafkaListener;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;

/**
 * 基于 Kafka 的 Web 日志发送器自动配置类
 * <p>
 * 该类用于自动配置基于 Kafka 的 Web 日志发送和消费组件，包括 {@link KafkaWebLogSender} 和 {@link WebLogKafkaListener}。
 * 根据配置动态注册所需的组件，支持使用 Kafka 作为日志传输的媒介。
 * </p>
 *
 * <p>功能说明：</p>
 * <ul>
 *     <li>自动注册 Kafka 日志发送器 {@link KafkaWebLogSender}，实现日志传递到 Kafka Topic。</li>
 *     <li>自动注册 Kafka 消费监听器 {@link WebLogKafkaListener}，从 Kafka Topic 中消费日志并处理。</li>
 *     <li>与 Spring Kafka 集成，便捷地实现分布式日志收集与处理。</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>需要统一将 Web 日志通过 Kafka 传递至集中式日志存储或分析平台。</li>
 *     <li>需要分布式日志消费实现，如基于 Topic 的日志分组和分区消费。</li>
 * </ul>
 *
 * <p>实现逻辑：</p>
 * <ul>
 *     <li>依赖 Spring Boot 的 Kafka 自动配置 {@link KafkaAutoConfiguration}，并在其完成后加载。</li>
 *     <li>仅在配置项 {@code pangju.web.log.enabled=true} 且 {@code sender-type=KAFKA} 时生效。</li>
 *     <li>支持动态注入 KafkaTemplate 和指定的 Topic 配置。</li>
 * </ul>
 *
 * <p>关键配置项：</p>
 * <pre>
 * pangju:
 *   web:
 *     log:
 *       enabled: true                # 启用 Web 日志功能
 *       sender-type: KAFKA           # 日志发送类型：KAFKA
 *       kafka:
 *         topic: web-log-topic       # 指定 Kafka Topic，用于存储 Web 日志
 *         kafkaTemplateBeanName: ""  # 可选，指定自定义的 KafkaTemplate Bean 名称
 * </pre>
 *
 * @author pangju666
 * @see WebLogSender
 * @see KafkaWebLogSender
 * @see WebLogKafkaListener
 * @since 1.0.0
 */
@AutoConfiguration(after = KafkaAutoConfiguration.class)
@ConditionalOnClass({KafkaTemplate.class})
@ConditionalOnProperty(prefix = "pangju.web.log", name = "sender-type", havingValue = "KAFKA")
public class KafkaSenderConfiguration {
	/**
	 * 注册 Kafka 日志发送器
	 * <p>
	 * 当配置的日志发送类型为 `KAFKA` 且项目中存在 {@link KafkaTemplate} 时，
	 * 自动注册 {@link KafkaWebLogSender}。
	 * </p>
	 *
	 * @param properties  Web 日志属性配置 {@link WebLogProperties}
	 * @param beanFactory Spring Bean 工厂，用于动态注入 KafkaTemplate 实例
	 * @return Kafka 日志发送器
	 * @since 1.0.0
	 */
	@SuppressWarnings("unchecked")
	@ConditionalOnMissingBean(WebLogSender.class)
	@ConditionalOnBean(KafkaTemplate.class)
	@Bean
	public KafkaWebLogSender kafkaWebLogSender(WebLogProperties properties, BeanFactory beanFactory) {
		KafkaTemplate<String, Object> kafkaTemplate;
		if (StringUtils.hasText(properties.getKafka().getKafkaTemplateBeanName())) {
			kafkaTemplate = beanFactory.getBean(properties.getKafka().getKafkaTemplateBeanName(), KafkaTemplate.class);
		} else {
			kafkaTemplate = beanFactory.getBean(KafkaTemplate.class);
		}
		return new KafkaWebLogSender(kafkaTemplate, properties.getKafka().getTopic());
	}

	/**
	 * 注册 Kafka 日志消费监听器
	 * <p>
	 * 当配置项中指定了有效的 Kafka Topic（`pangju.web.log.kafka.topic`）并存在 WebLogReceiver 实现时，
	 * 自动注册 {@link WebLogKafkaListener}，用于从 Kafka Topic 中消费日志数据并交由 {@link WebLogReceiver} 处理。
	 * </p>
	 *
	 * @param webLogReceiver Web 日志接收器 {@link WebLogReceiver}
	 * @return Kafka 日志消费监听器
	 * @since 1.0.0
	 */
	@ConditionalOnProperty(prefix = "pangju.web.log", name = "kafka.topic")
	@ConditionalOnBean(WebLogReceiver.class)
	@Bean
	public WebLogKafkaListener webLogKafkaListener(WebLogReceiver webLogReceiver) {
		return new WebLogKafkaListener(webLogReceiver);
	}
}
