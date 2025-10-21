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

package io.github.pangju666.framework.autoconfigure.web.log.sender;

import io.github.pangju666.framework.autoconfigure.web.log.model.WebLog;

/**
 * Web 日志发送器接口
 * <p>
 * 该接口定义了发送 Web 日志的标准方法。具体的日志发送实现（例如 Kafka、Disruptor 等）需要实现此接口，
 * 将日志数据 {@link io.github.pangju666.framework.autoconfigure.web.log.model.WebLog}
 * 发送到指定的存储介质或处理平台。
 * </p>
 *
 * <p>功能说明：</p>
 * <ul>
 *     <li>抽象日志发送逻辑，使日志收集与后续存储或处理解耦。</li>
 *     <li>支持多种实现方式，例如基于高性能队列（Disruptor）、分布式消息队列（Kafka）或自定义实现。</li>
 *     <li>通过统一接口，方便为不同的日志发送方式提供可插拔的实现。</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>日志采集完成后需要发送到指定目标（例如消息队列或数据库）。</li>
 *     <li>适用于支持高吞吐量、多消费者、异步日志处理的场景。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public interface WebLogSender {
	/**
	 * 发送 Web 日志
	 * <p>
	 * 定义日志发送的抽象方法。实现类需根据具体的发送方式处理
	 * {@link io.github.pangju666.framework.autoconfigure.web.log.model.WebLog}，
	 * 并将其传递到目标存储介质或服务。
	 * </p>
	 *
	 * @param webLog 当前采集的 Web 日志数据
	 * @since 1.0.0
	 */
	void send(WebLog webLog);
}
