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

package io.github.pangju666.framework.boot.task;

import org.springframework.core.task.AsyncTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 一次性任务执行器。
 * <p>
 * 提供按 {@code key} 维度的去重执行能力：在并发或重复请求场景下，确保同一 {@code key} 的任务仅执行一次，
 * 调用方均获得该次执行的结果。支持同步执行与异步提交，并可配置超时控制。
 * </p>
 *
 * <p>线程安全与去重策略由具体实现保证。</p>
 *
 * @author pangju666
 * @see AsyncTaskExecutor
 * @see CompletableFuture
 * @since 1.0.0
 */
public interface OnceTaskExecutor {
    /**
     * 同步执行一次性任务（按 {@code key} 去重）。
     * <p>
     * 当已存在相同 {@code key} 的任务正在或已执行完成时，复用其执行结果；否则触发新的执行。
     * </p>
     *
     * @param key  任务唯一标识
     * @param task 待执行的任务
     * @return 任务执行结果
     * @throws Exception 任务执行失败或被中断时抛出；具体异常由实现决定
     * @since 1.0.0
     */
    Object executeOnce(String key, Callable<Object> task) throws Exception;

    /**
     * 同步执行一次性任务并设置超时（按 {@code key} 去重）。
     * <p>
     * 在超时未完成时抛出异常（例如 {@link java.util.concurrent.TimeoutException}），具体异常类型与处理由实现决定；
     * 已存在相同 {@code key} 的任务时复用其结果。
     * </p>
     *
     * @param key     任务唯一标识
     * @param task    待执行的任务
     * @param timeout 超时时长
     * @param unit    超时单位
     * @return 任务执行结果
     * @throws Exception 执行失败或超时抛出；具体异常由实现决定
     * @since 1.0.0
     */
    Object executeOnce(String key, Callable<Object> task, long timeout, TimeUnit unit) throws Exception;

    /**
     * 异步提交一次性任务（按 {@code key} 去重）。
     * <p>
     * 使用提供的 {@link AsyncTaskExecutor} 调度任务；若相同 {@code key} 的任务已在执行或已完成，
     * 返回复用的 {@link CompletableFuture}，其完成状态与结果与该次执行保持一致。
     * </p>
     *
     * @param executor 异步任务执行器
     * @param key      任务唯一标识
     * @param task     待提交的任务
     * @return 可观察任务结果的 {@link CompletableFuture}
     * @since 1.0.0
     */
    CompletableFuture<Object> submitOnce(AsyncTaskExecutor executor, String key, Callable<Object> task);
}
