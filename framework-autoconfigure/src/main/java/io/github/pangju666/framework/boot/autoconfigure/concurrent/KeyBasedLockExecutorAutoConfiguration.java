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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 基于键的锁执行器自动配置入口。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>启用 {@link KeyBasedLockExecutorProperties} 属性绑定。</li>
 *   <li>按条件导入进程内实现（Guava Striped）与分布式实现（Redisson）。</li>
 *   <li>仅在未存在自定义 {@code KeyBasedLockExecutor} Bean 时进行配置。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(KeyBasedLockExecutorProperties.class)
@Import({GuavaConfiguration.class, RedissonConfiguration.class})
public class KeyBasedLockExecutorAutoConfiguration {
}
