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

package io.github.pangju666.framework.autoconfigure.web.log.sender.impl;

import io.github.pangju666.framework.autoconfigure.web.log.WebLogProperties;
import io.github.pangju666.framework.autoconfigure.web.log.model.WebLog;
import io.github.pangju666.framework.autoconfigure.web.log.sender.WebLogSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;

public class KafkaWebLogSender implements WebLogSender {
	private static final Logger log = LoggerFactory.getLogger(KafkaWebLogSender.class);

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final WebLogProperties properties;

	public KafkaWebLogSender(WebLogProperties properties, BeanFactory beanFactory) {
		if (StringUtils.hasText(properties.getKafka().getTemplateBeanName())) {
			this.kafkaTemplate = beanFactory.getBean(properties.getKafka().getTemplateBeanName(), KafkaTemplate.class);
		} else {
			this.kafkaTemplate = beanFactory.getBean(KafkaTemplate.class);
		}
		this.properties = properties;
	}

	@Override
	public void send(WebLog webLog) {
		try {
			kafkaTemplate.send(properties.getKafka().getTopic(), webLog);
		} catch (RuntimeException e) {
			log.error("接口请求信息发送至消息队列失败", e);
		}
	}
}
