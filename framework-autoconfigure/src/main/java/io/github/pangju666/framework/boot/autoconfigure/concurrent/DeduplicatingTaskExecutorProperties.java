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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 去重任务执行器配置属性。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>属性前缀：{@code pangju.task.execution.deduplicating}。</li>
 *   <li>用于配置同步/异步任务去重映射的初始容量，以降低高并发场景下的扩容开销。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "pangju.task.execution.deduplicating")
public class DeduplicatingTaskExecutorProperties {
    /**
     * 同步任务映射初始容量。
     *
     * <p>默认值：16。用于初始化内部 {@code ConcurrentHashMap} 的容量。</p>
     *
     * @since 1.0.0
     */
    private int syncInitialCapacity = 16;
    /**
     * 异步任务映射初始容量。
     *
     * <p>默认值：16。用于初始化内部 {@code ConcurrentHashMap} 的容量。</p>
     *
     * @since 1.0.0
     */
    private int asyncInitialCapacity = 16;

    /**
     * 获取同步任务映射初始容量。
     *
     * @return 初始容量
     * @since 1.0.0
     */
    public int getSyncInitialCapacity() {
        return syncInitialCapacity;
    }

    /**
     * 设置同步任务映射初始容量。
     *
     * <p>参数校验规则：如果 {@code syncInitialCapacity} ≤ 0，则不设置或保留默认值。</p>
     *
     * @param syncInitialCapacity 初始容量
     * @since 1.0.0
     */
    public void setSyncInitialCapacity(int syncInitialCapacity) {
        this.syncInitialCapacity = syncInitialCapacity;
    }

    /**
     * 获取异步任务映射初始容量。
     *
     * @return 初始容量
     * @since 1.0.0
     */
    public int getAsyncInitialCapacity() {
        return asyncInitialCapacity;
    }

    /**
     * 设置异步任务映射初始容量。
     *
     * <p>参数校验规则：如果 {@code asyncInitialCapacity} ≤ 0，则不设置或保留默认值。</p>
     *
     * @param asyncInitialCapacity 初始容量
     * @since 1.0.0
     */
    public void setAsyncInitialCapacity(int asyncInitialCapacity) {
        this.asyncInitialCapacity = asyncInitialCapacity;
    }
}
