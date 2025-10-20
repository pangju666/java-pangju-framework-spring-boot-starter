package io.github.pangju666.framework.autoconfigure.web.log.sender.impl;

import io.github.pangju666.framework.autoconfigure.web.log.WebLog;
import io.github.pangju666.framework.autoconfigure.web.log.WebLogProperties;
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
