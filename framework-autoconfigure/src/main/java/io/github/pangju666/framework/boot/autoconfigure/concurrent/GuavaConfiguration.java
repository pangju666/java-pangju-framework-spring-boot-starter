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

package io.github.pangju666.framework.boot.autoconfigure.concurrent;

import com.google.common.util.concurrent.Striped;
import io.github.pangju666.framework.boot.concurrent.KeyBasedLockTaskExecutor;
import io.github.pangju666.framework.boot.concurrent.impl.StripedKeyBasedLockTaskExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Guava Striped 进程内键锁执行器自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>仅在类路径存在 {@link Striped} 时生效。</li>
 *   <li>当容器中不存在自定义 {@link KeyBasedLockTaskExecutor} Bean 时，提供基于 Guava 的实现。</li>
 *   <li>从 {@link KeyBasedLockTaskExecutorProperties.Guava} 读取条带数量。</li>
 *   <li>当配置属性 {@code pangju.concurrent.executor.key-based-lock=GUAVA} 或未配置该属性（默认）时启用。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Striped.class})
@ConditionalOnProperty(prefix = "pangju.concurrent.executor.key-based-lock", name = "type", havingValue = "GUAVA", matchIfMissing = true)
class GuavaConfiguration {
	/**
	 * 创建基于 Guava Striped 的 {@link KeyBasedLockTaskExecutor} 实例。
	 *
	 * <p>参数校验规则：</p>
	 * <p>如果 {@code properties.guava.stripes} ≤ 0，则不设置或保留默认值。</p>
	 *
	 * @param properties 锁执行器属性
	 * @return Striped 键锁执行器
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(KeyBasedLockTaskExecutor.class)
	@Bean
	public StripedKeyBasedLockTaskExecutor stripedKeyBasedLockExecutor(KeyBasedLockTaskExecutorProperties properties) {
		return new StripedKeyBasedLockTaskExecutor(properties.getGuava().getStripes());
	}
}
