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

package io.github.pangju666.framework.autoconfigure.web.log.sender.impl.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.github.pangju666.framework.autoconfigure.web.log.WebLogProperties;
import io.github.pangju666.framework.autoconfigure.web.log.model.WebLog;
import io.github.pangju666.framework.autoconfigure.web.log.sender.WebLogSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

public class DisruptorWebLogSender implements WebLogSender {
	private static final Logger log = LoggerFactory.getLogger(DisruptorWebLogSender.class);

	private final Disruptor<WebLogEvent> disruptor;

	public DisruptorWebLogSender(WebLogProperties properties, DisruptorWebLogEventHandler eventHandler) {
		this.disruptor = new Disruptor<>(
			WebLogEvent::new,
			properties.getDisruptor().getBufferSize(),
			Executors.defaultThreadFactory(),
			ProducerType.SINGLE,
			new YieldingWaitStrategy()
		);
		disruptor.handleEventsWith(eventHandler);
		this.disruptor.start();
	}

	@Override
	public void send(WebLog webLog) {
		try {
			RingBuffer<WebLogEvent> ringBuffer = disruptor.getRingBuffer();
			long sequence = ringBuffer.next();
			WebLogEvent event = ringBuffer.get(sequence);
			event.setWebLog(webLog);
			ringBuffer.publish(sequence);
		} catch (RuntimeException e) {
			log.error("接口请求信息发送至Disruptor队列失败", e);
		}
	}
}
