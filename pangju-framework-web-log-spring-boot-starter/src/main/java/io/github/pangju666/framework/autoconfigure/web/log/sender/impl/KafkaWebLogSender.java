package io.github.pangju666.framework.autoconfigure.web.log.sender.impl;

import io.github.pangju666.framework.autoconfigure.web.log.model.WebLog;
import io.github.pangju666.framework.autoconfigure.web.log.properties.WebLogProperties;
import io.github.pangju666.framework.autoconfigure.web.log.sender.WebLogSender;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;

public class KafkaWebLogSender implements WebLogSender {
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
		kafkaTemplate.send(properties.getKafka().getTopic(), webLog);
	}
}
