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
import io.github.pangju666.framework.boot.web.log.receiver.impl.slf4j.Slf4jWebLogReceiver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Web 日志 SLF4J 接收器自动配置。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>在满足配置条件时，自动注册基于 SLF4J 的 {@link Slf4jWebLogReceiver}。</li>
 *   <li>仅当容器中不存在其它 {@link WebLogReceiver} Bean 时生效，避免重复注册。</li>
 * </ul>
 *
 * <p><b>启用条件</b></p>
 * <ul>
 *   <li>属性 {@code pangju.web.log.receiver-type} 为 {@code SLF4J} 或未显式配置（默认允许）。</li>
 *   <li>存在属性 {@code pangju.web.log.slf4j.logger}（目标日志记录器名称）。</li>
 * </ul>
 *
 * <p><b>配置项</b></p>
 * <ul>
 *   <li>{@code pangju.web.log.slf4j.logger}：目标 Logger 名称，写入采集到的 {@link io.github.pangju666.framework.boot.web.log.model.WebLog}。</li>
 * </ul>
 *
 * <p><b>示例</b></p>
 * <pre>
 * pangju:
 *   web:
 *     log:
 *       receiver-type: SLF4J
 *       slf4j:
 *         logger: WebLogLogger
 * </pre>
 *
 * @author pangju666
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "pangju.web.log", name = "receiver-type", havingValue = "SLF4J", matchIfMissing = true)
@ConditionalOnProperty(prefix = "pangju.web.log.slf4j", name = "logger")
class Slf4jReceiverConfiguration {
	/**
     * 注册 SLF4J Web 日志接收器 Bean。
     *
     * <p>根据配置的 Logger 名称，将采集到的 {@link io.github.pangju666.framework.boot.web.log.model.WebLog}
     * 写入日志系统（SLF4J 兼容实现，如 Logback、Log4j2）。</p>
     *
     * @param properties Web 日志配置属性
     * @return {@link Slf4jWebLogReceiver} 实例
     * @since 1.0.0
     */
	@ConditionalOnMissingBean(WebLogReceiver.class)
	@Bean
	public Slf4jWebLogReceiver slf4jWebLogReceiver(WebLogProperties properties) {
		return new Slf4jWebLogReceiver(properties.getSlf4j().getLogger());
	}
}
