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

package io.github.pangju666.framework.boot.web.log.sender.impl.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.github.pangju666.framework.boot.web.log.model.WebLog;
import io.github.pangju666.framework.boot.web.log.sender.WebLogSender;

import java.util.concurrent.Executors;

/**
 * 基于 Disruptor 的 Web 日志发送器
 * <p>
 * 该类是 {@link WebLogSender} 的实现，基于 Disruptor 高性能异步框架，用于将 Web 日志
 * 异步地发送到 RingBuffer 队列中，从而实现高效的日志数据处理和传递。
 * </p>
 *
 * <p>功能说明：</p>
 * <ul>
 *     <li>初始化 Disruptor 框架，配置 RingBuffer、事件处理器等。</li>
 *     <li>将日志数据封装为事件 {@link WebLogEvent} 并发布到 RingBuffer 中，供事件处理器消费。</li>
 *     <li>通过 {@link DisruptorWebLogEventHandler} 进行消费并调用日志接收器处理日志信息。</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>需要高吞吐量和低延迟的日志处理场景。</li>
 *     <li>支持异步日志传递的分布式架构。</li>
 * </ul>
 *
 * <p>实现逻辑：</p>
 * <ul>
 *     <li>初始化 Disruptor，通过 RingBuffer 持有 Web 日志事件。</li>
 *     <li>生产者使用 {@link #send(WebLog)} 方法将日志推送到 RingBuffer。</li>
 *     <li>消费者（如 {@link DisruptorWebLogEventHandler}）从 RingBuffer 中提取事件并进行日志处理。</li>
 * </ul>
 *
 * @author pangju666
 * @see WebLogSender
 * @see WebLogEvent
 * @see DisruptorWebLogEventHandler
 * @since 1.0.0
 */
public class DisruptorWebLogSender implements WebLogSender {
	/**
	 * Disruptor 实例
	 * <p>
	 * 持有 RingBuffer 的核心组件，管理事件的生产和消费。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final Disruptor<WebLogEvent> disruptor;

	/**
	 * 构造方法
	 * <p>
	 * 初始化 Disruptor 对象，配置事件工厂、环形缓冲区大小、事件处理器等。
	 * </p>
	 *
	 * @param bufferSize   环形缓冲区大小
	 * @param eventHandler 事件处理器，用于消费 RingBuffer 中的 {@link WebLogEvent}
	 * @since 1.0.0
	 */
	public DisruptorWebLogSender(int bufferSize, DisruptorWebLogEventHandler eventHandler) {
		this.disruptor = new Disruptor<>(
			WebLogEvent::new,
			bufferSize,
			Executors.defaultThreadFactory(),
			ProducerType.SINGLE,
			new YieldingWaitStrategy()
		);
		disruptor.handleEventsWith(eventHandler);
		this.disruptor.start();
	}

	/**
	 * 发送 Web 日志
	 * <p>
	 * 将日志数据推送到 Disruptor 的 RingBuffer 中，封装为 {@link WebLogEvent}。
	 * 如果失败，则记录错误日志。
	 * </p>
	 *
	 * @param webLog 当前采集的 Web 日志
	 */
	@Override
	public void send(WebLog webLog) {
		RingBuffer<WebLogEvent> ringBuffer = disruptor.getRingBuffer();
		long sequence = ringBuffer.next();
		WebLogEvent event = ringBuffer.get(sequence);
		event.setWebLog(webLog);
		ringBuffer.publish(sequence);
	}
}
