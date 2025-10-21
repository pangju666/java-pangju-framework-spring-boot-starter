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

import io.github.pangju666.framework.autoconfigure.web.log.revceiver.WebLogReceiver;
import io.github.pangju666.framework.autoconfigure.web.log.sender.WebLogSender;
import io.github.pangju666.framework.autoconfigure.web.log.sender.impl.kafka.KafkaWebLogSender;
import io.github.pangju666.framework.autoconfigure.web.log.sender.impl.kafka.WebLogKafkaListener;
import jakarta.servlet.Servlet;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration(before = WebLogAutoConfiguration.class, after = KafkaAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class, KafkaTemplate.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.log", name = "enabled", matchIfMissing = true)
@ConditionalOnProperty(prefix = "pangju.web.log", name = "sender-type", havingValue = "KAFKA")
public class KafkaSenderAutoConfiguration {
	@ConditionalOnMissingBean(WebLogSender.class)
	@ConditionalOnBean(KafkaTemplate.class)
	@Bean
	public KafkaWebLogSender kafkaWebLogSender(WebLogProperties properties, BeanFactory beanFactory) {
		return new KafkaWebLogSender(properties, beanFactory);
	}

	@ConditionalOnProperty(prefix = "pangju.web.log", name = "kafka.topic")
	@ConditionalOnBean(WebLogReceiver.class)
	@Bean
	public WebLogKafkaListener webLogKafkaListener(WebLogReceiver webLogReceiver) {
		return new WebLogKafkaListener(webLogReceiver);
	}
}
