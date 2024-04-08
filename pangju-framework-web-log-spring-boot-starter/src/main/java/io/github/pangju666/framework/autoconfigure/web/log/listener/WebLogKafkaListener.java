package io.github.pangju666.framework.autoconfigure.web.log.listener;

import io.github.pangju666.framework.autoconfigure.web.log.model.WebLog;
import io.github.pangju666.framework.autoconfigure.web.log.revceiver.WebLogReceiver;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Objects;

public class WebLogKafkaListener {
	private final WebLogReceiver receiver;

	public WebLogKafkaListener(BeanFactory beanFactory) {
		this.receiver = beanFactory.getBean(WebLogReceiver.class);
	}

	@KafkaListener(topics = "${pangju.web.log.kafka.topic}")
	public void listenRequestLog(ConsumerRecord<String, WebLog> record, Acknowledgment ack) {
		WebLog webLog = record.value();
		if (Objects.nonNull(webLog)) {
			receiver.receive(webLog);
		}
		ack.acknowledge();
	}
}
