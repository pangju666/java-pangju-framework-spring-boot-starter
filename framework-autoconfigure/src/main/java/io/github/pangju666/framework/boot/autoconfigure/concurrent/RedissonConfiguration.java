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

import io.github.pangju666.framework.boot.concurrent.KeyBasedLockExecutor;
import io.github.pangju666.framework.boot.concurrent.impl.RedissonKeyBasedLockExecutor;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 分布式键锁执行器自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>仅在类路径存在 {@link RedissonClient} 时生效。</li>
 *   <li>当容器中不存在自定义 {@link KeyBasedLockExecutor} Bean 时，提供基于 Redisson 的实现。</li>
 *   <li>从 {@link KeyBasedLockExecutorProperties.Redisson} 读取租约时间、单位与键前缀。</li>
 *   <li>当配置属性 {@code pangju.concurrent.executor.key-based-lock=REDISSON} 时启用；否则不装配。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({RedissonClient.class})
@ConditionalOnProperty(prefix = "pangju.concurrent.executor.key-based-lock", name = "type", havingValue = "REDISSON")
class RedissonConfiguration {
	/**
	 * 创建基于 Redisson 的 {@link KeyBasedLockExecutor} 实例。
	 *
	 * <p>参数校验规则：</p>
	 * <p>如果 {@code redissonClient} 为空，则不创建；如果 {@code properties.redisson.unit} 为空，可能导致加锁时抛出异常；如果 {@code properties.redisson.prefix} 为空，则不设置前缀。</p>
	 *
	 * @param redissonClient Redisson 客户端
	 * @param properties     锁执行器属性
	 * @return Redisson 键锁执行器
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(KeyBasedLockExecutor.class)
	@Bean
	public RedissonKeyBasedLockExecutor redissonKeyBasedLockExecutor(RedissonClient redissonClient,
									   KeyBasedLockExecutorProperties properties) {
		return new RedissonKeyBasedLockExecutor(redissonClient, properties.getRedisson().getPrefix(),
			properties.getRedisson().getLeaseTime(), properties.getRedisson().getUnit());
	}
}
