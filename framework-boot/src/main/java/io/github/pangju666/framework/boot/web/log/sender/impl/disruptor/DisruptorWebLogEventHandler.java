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

import com.lmax.disruptor.EventHandler;
import io.github.pangju666.framework.boot.web.log.model.WebLog;
import io.github.pangju666.framework.boot.web.log.receiver.WebLogReceiver;

import java.util.Objects;

/**
 * Disruptor Web 日志事件处理器
 * <p>
 * 该类是 Disruptor 框架中的事件处理器，实现了 {@link EventHandler} 接口。
 * 它用于消费由 {@link WebLogEvent} 封装的 Web 日志数据，并通过 {@link WebLogReceiver}
 * 将日志传递到下一处理环节（如持久化存储或分析处理）。
 * </p>
 *
 * <p>功能说明：</p>
 * <ul>
 *     <li>作为 Disruptor 的事件处理器，处理生产者投递到 RingBuffer 的 Web 日志事件。</li>
 *     <li>将事件中的 Web 日志数据提取并传递给 {@link WebLogReceiver} 进行业务处理。</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>适用于基于 Disruptor 的高性能日志异步处理架构。</li>
 *     <li>消费生产者（如 {@link DisruptorWebLogSender}）
 *         发布的 Web 日志事件。</li>
 * </ul>
 *
 * <p>实现逻辑：</p>
 * <ul>
 *     <li>从 {@link WebLogEvent} 提取事件中封装的 Web 日志 {@link WebLog}。</li>
 *     <li>若日志数据不为空，则调用 {@link WebLogReceiver#receive(WebLog)} 进行日志处理。</li>
 * </ul>
 *
 * @author pangju666
 * @see com.lmax.disruptor.EventHandler
 * @see WebLogEvent
 * @see WebLogReceiver
 * @see DisruptorWebLogSender
 * @since 1.0.0
 */
public class DisruptorWebLogEventHandler implements EventHandler<WebLogEvent> {
	/**
	 * Web 日志接收器
	 * <p>
	 * 定义用于接收和处理 Web 日志的接口，实现类可以将日志数据存储至 MongoDB、
	 * ElasticSearch 等持久化存储或进行其他处理。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final WebLogReceiver receiver;

	/**
	 * 构造方法
	 * <p>
	 * 通过构造函数注入 {@link WebLogReceiver}，实现具体的日志接收逻辑。
	 * </p>
	 *
	 * @param webLogReceiver {@link WebLogReceiver} 实现，用于处理接收的 Web 日志
	 * @since 1.0.0
	 */
	public DisruptorWebLogEventHandler(WebLogReceiver webLogReceiver) {
		this.receiver = webLogReceiver;
	}

	/**
	 * 事件处理方法
	 * <p>
	 * 当事件被发布到 Disruptor RingBuffer 时调用该方法进行消费。
	 * 从事件中提取日志数据 {@link WebLog} 并交由接收器 {@link WebLogReceiver} 进行处理。
	 * </p>
	 *
	 * @param event      当前被消费的日志事件 {@link WebLogEvent}
	 * @param sequence   当前事件在 RingBuffer 中的序号
	 * @param endOfBatch 是否为当前批次中的最后一个事件
	 */
	@Override
	public void onEvent(WebLogEvent event, long sequence, boolean endOfBatch) {
		WebLog webLog = event.getWebLog();
		if (Objects.nonNull(webLog)) {
			receiver.receive(webLog);
		}
	}
}
