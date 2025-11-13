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

package io.github.pangju666.framework.boot.autoconfigure.web.log;

import io.github.pangju666.framework.boot.web.log.receiver.WebLogReceiver;
import io.github.pangju666.framework.boot.web.log.receiver.impl.disk.DiskWebLogReceiver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Web 日志磁盘接收器自动配置。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>在满足配置条件时，自动注册基于磁盘的 {@link DiskWebLogReceiver}。</li>
 *   <li>仅当容器中不存在其它 {@link WebLogReceiver} Bean 时生效，避免重复注册。</li>
 * </ul>
 *
 * <p><b>启用条件</b></p>
 * <ul>
 *   <li>存在属性 {@code pangju.web.log.disk.directory}（写入目录）。</li>
 *   <li>属性 {@code pangju.web.log.receiver-type} 为 {@code DISK} 或未显式配置（默认允许）。</li>
 * </ul>
 *
 * <p><b>配置项</b></p>
 * <ul>
 *   <li>{@code pangju.web.log.disk.directory}：日志目录，自动创建。</li>
 *   <li>{@code pangju.web.log.disk.base-filename}：基础文件名后缀，可选。</li>
 *   <li>{@code pangju.web.log.disk.writer-buffer-size}：写缓冲大小（字节）。</li>
 *   <li>{@code pangju.web.log.disk.queue-size}：背压队列容量（条）。</li>
 *   <li>{@code pangju.web.log.disk.write-thread-destroy-wait-mills}：关闭等待写线程结束的毫秒数。</li>
 * </ul>
 *
 * <p><b>示例</b></p>
 * <pre>
 * pangju:
 *   web:
 *     log:
 *       receiver-type: DISK
 *       disk:
 *         directory: logs/web
 *         base-filename: access
 *         writer-buffer-size: 8192
 *         queue-size: 10000
 *         write-thread-destroy-wait-mills: 5000
 * </pre>
 *
 * @author pangju666
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "pangju.web.log", name = "disk.directory")
@ConditionalOnProperty(prefix = "pangju.web.log", name = "receiver-type", havingValue = "DISK", matchIfMissing = true)
class DiskReceiverConfiguration {
	/**
     * 注册基于磁盘的 Web 日志接收器 Bean。
     *
     * <p>
     * Bean 的销毁方法设为 {@code shutdown}，在容器关闭时会尝试处理完队列中的剩余消息并释放文件资源。
     * </p>
     *
     * @param properties Web 日志配置属性
     * @return {@link DiskWebLogReceiver} 实例
     * @throws IOException 当日志目录创建失败或底层文件 IO 初始化失败时抛出
	 * @since 1.0.0
     */
	@ConditionalOnMissingBean(WebLogReceiver.class)
	@Bean(destroyMethod = "shutdown")
	public DiskWebLogReceiver diskWebLogReceiver(WebLogProperties properties) throws IOException {
		return new DiskWebLogReceiver(properties.getDisk().getDirectory(), properties.getDisk().getBaseFilename(),
			properties.getDisk().getWriterBufferSize(), properties.getDisk().getQueueSize(),
			properties.getDisk().getWriteThreadDestroyWaitMills());
	}
}
