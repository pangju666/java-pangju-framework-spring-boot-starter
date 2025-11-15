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

package io.github.pangju666.framework.boot.concurrent;

/**
 * 基于键的锁任务接口。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>表示在键锁保护下执行的任务单元，返回结果类型由泛型 {@code T} 指定。</li>
 *   <li>通常与 {@link io.github.pangju666.framework.boot.concurrent.KeyBasedLockExecutor} 配合使用，由执行器负责获取与释放锁。</li>
 * </ul>
 *
 * @param <T> 任务返回结果类型
 * @author pangju666
 * @since 1.0.0
 */
@FunctionalInterface
public interface KeyBasedLockTask<T> {
    /**
     * 执行在指定键上下文中的任务。
     *
     * <p>参数校验规则：</p>
     * <p>如果 {@code key} 为空，则不执行或抛出异常；具体由调用方或实现决定。</p>
     *
     * @param key 锁的键标识
     * @return 任务返回结果
     * @throws Exception 任务执行过程中抛出的异常
     * @since 1.0.0
     */
    T execute(String key) throws Exception;
}
