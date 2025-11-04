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

import io.github.pangju666.framework.boot.web.log.model.WebLog;

/**
 * Web 日志事件类
 * <p>
 * 该类被设计用于封装一次 Web 日志事件的数据，适配高性能异步处理框架（如 Disruptor）。
 * 每个实例表示一个可被异步处理的 {@link WebLog} 日志记录。
 * 它通常作为事件模型，在日志的生产者（如请求处理完成时）与消费者（如日志持久化处理）之间传递。
 * </p>
 *
 * <p>功能说明：</p>
 * <ul>
 *     <li>作为 Disruptor 或其他事件处理框架的事件承载对象。</li>
 *     <li>封装 {@link WebLog} 日志数据，使其可以在不同线程中异步处理。</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>高吞吐量场景下使用 Disruptor 进行 Web 日志异步处理。</li>
 *     <li>日志数据在采集后，将其封装成事件对象，传递给事件处理器进行存储或进一步处理。</li>
 * </ul>
 *
 * @author pangju666
 * @see WebLog
 * @see DisruptorWebLogSender
 * @see DisruptorWebLogEventHandler
 * @since 1.0.0
 */
public class WebLogEvent {
	/**
	 * Web 日志数据
	 * <p>
	 * 封装具体的 Web 请求与响应日志数据，供事件消费者使用。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private WebLog webLog;

	public WebLog getWebLog() {
		return webLog;
	}

	public void setWebLog(WebLog webLog) {
		this.webLog = webLog;
	}
}
