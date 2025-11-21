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

import io.github.pangju666.framework.boot.concurrent.OnceTaskExecutor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 去重任务执行器自动配置。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>注册 {@link OnceTaskExecutor} Bean。</li>
 *   <li>基于 {@link OnceTaskExecutorProperties} 提供的初始容量配置进行实例化。</li>
 * </ul>
 *
 * <p><b>配置项</b></p>
 * <ul>
 *   <li>前缀：{@code pangju.task.execution.once}。</li>
 *   <li>{@code sync-initial-capacity}、{@code async-initial-capacity}：同步/异步去重映射的初始容量。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(OnceTaskExecutorProperties.class)
public class OnceTaskExecutorAutoConfiguration {
    /**
     * 注册去重任务执行器 Bean。
     *
     * <p>行为：根据配置初始化同步与异步去重映射的初始容量。</p>
     * <p>启用条件：当容器中不存在其它 {@link OnceTaskExecutor} Bean 时生效。</p>
     *
     * @param properties 配置属性
     * @return 去重任务执行器实例
     * @since 1.0.0
     */
	@ConditionalOnMissingBean(OnceTaskExecutor.class)
    @Bean
    public OnceTaskExecutor onceTaskExecutor(OnceTaskExecutorProperties properties) {
        return new OnceTaskExecutor(properties.getSyncInitialCapacity(), properties.getAsyncInitialCapacity());
    }
}