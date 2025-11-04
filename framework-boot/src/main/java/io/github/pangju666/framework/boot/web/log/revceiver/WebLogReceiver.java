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

package io.github.pangju666.framework.boot.web.log.revceiver;

import io.github.pangju666.framework.boot.web.log.model.WebLog;

/**
 * Web 日志接收器接口
 * <p>
 * 该接口用于定义处理接收到的 {@link WebLog} 日志数据的标准方法。实现此接口的类可将日志数据
 * 持久化存储、提交分析平台或进行其他业务处理。
 * </p>
 *
 * <p>功能说明：</p>
 * <ul>
 *     <li>作为日志处理的抽象层，用于接收和处理采集的 {@link WebLog} 实例。</li>
 *     <li>支持多种接收器实现，例如存储到 MongoDB、存储到文件或将日志提交到分析服务。</li>
 *     <li>通过多种日志接收器的组合，适配多场景日志处理需求。</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>从 Kafka、Disruptor 或其他存储队列中消费日志后，调用该接口的实现进行处理。</li>
 *     <li>可拓展日志存储方式，将 Web 日志存储至 MongoDB 或其他外部系统。</li>
 * </ul>
 *
 * @author pangju666
 * @see WebLog
 * @since 1.0.0
 */
public interface WebLogReceiver {
	/**
	 * 处理接收到的 Web 日志
	 * <p>
	 * 用于处理日志事件，将接收到的 {@link WebLog} 数据执行持久化或其他业务处理。
	 * </p>
	 *
	 * @param webLog 接收到的日志实例 {@link WebLog}
	 * @since 1.0.0
	 */
	void receive(WebLog webLog);
}
