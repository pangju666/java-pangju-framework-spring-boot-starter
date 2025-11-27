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

package io.github.pangju666.framework.boot.autoconfigure.task;

import io.github.pangju666.framework.boot.task.impl.FutureOnceTaskExecutor;
import io.github.pangju666.framework.boot.task.OnceTaskExecutor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 一次性任务执行器自动配置。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>注册 {@link OnceTaskExecutor} Bean（默认实现为 {@link FutureOnceTaskExecutor}）。</li>
 *   <li>基于 {@link OnceTaskExecutorProperties} 提供的初始容量配置进行实例化。</li>
 * </ul>
 *
 * <p><b>配置项</b></p>
 * <ul>
 *   <li>前缀：{@code pangju.task.execution.once}。</li>
 *   <li>{@code sync-initial-capacity}、{@code async-initial-capacity}：同步/异步任务映射的初始容量。</li>
 * </ul>
 *
 * <p><b>启用条件</b></p>
 * <ul>
 *   <li>启用 {@link org.springframework.boot.context.properties.EnableConfigurationProperties}，加载 {@link OnceTaskExecutorProperties}。</li>
 *   <li>容器中不存在其它 {@link OnceTaskExecutor} Bean（受 {@link org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean} 约束）。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 * @see OnceTaskExecutor
 * @see FutureOnceTaskExecutor
 * @see OnceTaskExecutorProperties
 */
@AutoConfiguration
@EnableConfigurationProperties(OnceTaskExecutorProperties.class)
public class OnceTaskExecutorAutoConfiguration {
    /**
     * 注册一次性任务执行器 Bean。
     *
     * <p>行为：根据配置初始化同步与异步任务映射的初始容量。</p>
     * <p>启用条件：当容器中不存在其它 {@link OnceTaskExecutor} Bean 时生效（{@link ConditionalOnMissingBean}）。</p>
     *
     * @param properties 配置属性（前缀 {@code pangju.task.execution.once}）
     * @return 单次任务执行器实例
     * @since 1.0.0
     */
	@ConditionalOnMissingBean(OnceTaskExecutor.class)
    @Bean
    public FutureOnceTaskExecutor futureOnceTaskExecutor(OnceTaskExecutorProperties properties) {
        return new FutureOnceTaskExecutor(properties.getSyncInitialCapacity(), properties.getAsyncInitialCapacity());
    }
}