package io.github.pangju666.framework.autoconfigure.web.log;

import io.github.pangju666.framework.autoconfigure.web.log.revceiver.WebLogReceiver;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Objects;

public class WebLogKafkaListener {
	private final WebLogReceiver receiver;

	public WebLogKafkaListener(WebLogReceiver webLogReceiver) {
		this.receiver = webLogReceiver;
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
