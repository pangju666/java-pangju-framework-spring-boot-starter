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

package io.github.pangju666.framework.autoconfigure.web.log.sender.impl.kafka;

import io.github.pangju666.framework.autoconfigure.web.log.model.WebLog;
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
