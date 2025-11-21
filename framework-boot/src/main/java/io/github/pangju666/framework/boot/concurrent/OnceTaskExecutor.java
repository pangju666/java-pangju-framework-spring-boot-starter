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

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * 去重任务执行器（按键去重）。
 *
 * <p>概述：基于键进行并发去重，确保同一键的任务在同一时间仅执行一次；后续并发请求复用首个任务的结果。</p>
 * <p>实现：同步场景使用 {@link FutureTask} 与 {@link ConcurrentMap} 去重；异步场景使用 {@link CompletableFuture}
 * 与 {@link ConcurrentMap} 去重，并通过 {@link AsyncTaskExecutor} 提交任务。</p>
 * <p>并发与清理：按键注册进行去重；任务结束（成功或异常）后移除对应键，避免内存泄漏与错误复用。</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class OnceTaskExecutor {
    /**
     * 正在执行中的同步任务映射（用于并发去重）。
     *
     * @since 1.0.0
     */
    private final ConcurrentMap<String, Future<Object>> syncRunningTasks;
    /**
     * 正在执行中的异步任务映射（用于并发去重）。
     *
     * @since 1.0.0
     */
    private final ConcurrentMap<String, CompletableFuture<Object>> asyncRunningTasks;

    /**
     * 创建去重任务执行器（指定初始容量）。
     *
     * <p>行为：初始化同步与异步运行任务映射，合理的初始容量可降低并发场景下的扩容开销。</p>
     *
     * <p>异常说明：当 {@code syncInitialCapacity} ≤ 0 或 {@code asyncInitialCapacity} ≤ 0 时断言失败，将抛出 {@link IllegalArgumentException}。</p>
     *
     * @param syncInitialCapacity  同步任务映射初始容量
     * @param asyncInitialCapacity 异步任务映射初始容量
     * @throws IllegalArgumentException 当任一初始容量 ≤ 0 时抛出
     * @since 1.0.0
     */
    public OnceTaskExecutor(int syncInitialCapacity, int asyncInitialCapacity) {
        Assert.isTrue(syncInitialCapacity > 0, "syncInitialCapacity 必须大于 0");
        Assert.isTrue(asyncInitialCapacity > 0, "asyncInitialCapacity 必须大于 0");

        this.syncRunningTasks = new ConcurrentHashMap<>(syncInitialCapacity);
        this.asyncRunningTasks = new ConcurrentHashMap<>(asyncInitialCapacity);
    }

    /**
     * 同步执行一次任务（按键去重）。
     *
     * <p>行为：
     * 首次调用会创建并执行任务；并发的后续调用复用同一个 {@link Future} 并返回相同结果。
     * 任务完成后自动从去重映射中移除键。</p>
     *
     * <p>异常说明：当 {@code key} 为空白或 {@code task} 为 {@code null} 时断言失败，将抛出 {@link IllegalArgumentException}；
     * 如任务抛出异常，则将其原始异常向上抛出。</p>
     *
     * @param key  去重键标识
     * @param task 需要执行的任务
     * @param <T>  返回类型
     * @return 任务返回结果
     * @throws Throwable 任务执行过程中抛出的原始异常
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    public <T> T executeOnce(String key, Callable<T> task) throws Throwable {
        Assert.hasText(key, "key 不可为空");
        Assert.notNull(task, "task 不可为 null");

        Future<Object> futureTask = syncRunningTasks.computeIfAbsent(key, k -> {
            FutureTask<Object> newFutureTask = new FutureTask<>(() -> {
                try {
                    return task.call();
                } finally {
					syncRunningTasks.remove(k);
                }
            });
            newFutureTask.run();
            return newFutureTask;
        });

        try {
            return (T) futureTask.get();
        } catch (ExecutionException e) {
            throw Objects.requireNonNullElse(e.getCause(), e);
        }
    }

    /**
     * 异步提交一次任务（按键去重）。
     *
     * <p>行为：
     * 若同键任务正在执行，则直接返回已注册的 {@link CompletableFuture}；否则注册并提交新任务。
     * 任务完成（成功或异常）后，从去重映射中移除键。</p>
     *
     * <p>异常说明：当 {@code key} 为空白、{@code executor} 或 {@code task} 为 {@code null} 时断言失败，将抛出 {@link IllegalArgumentException}。
     * 返回的 {@link CompletableFuture} 可能以异常结束，异常为任务执行抛出的原始异常。</p>
     *
     * @param executor 异步任务执行器
     * @param key      去重键标识
     * @param task     需要执行的任务
     * @param <T>      返回类型
     * @return 去重后的 {@link CompletableFuture}
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<T> submitOnce(AsyncTaskExecutor executor, String key, Callable<T> task) {
        Assert.hasText(key, "key 不可为空");
        Assert.notNull(executor, "executor 不可为 null");
        Assert.notNull(task, "task 不可为 null");

        CompletableFuture<Object> existingFuture = asyncRunningTasks.get(key);
        if (Objects.nonNull(existingFuture)) {
            return (CompletableFuture<T>) existingFuture;
        }

        CompletableFuture<Object> newFuture = new CompletableFuture<>();

        CompletableFuture<Object> registeredFuture = asyncRunningTasks.putIfAbsent(key, newFuture);
        if (Objects.nonNull(registeredFuture)) {
            return (CompletableFuture<T>) registeredFuture;
        }

        executor.submit(() -> {
            try {
                newFuture.complete(task.call());
            } catch (Throwable t) {
                newFuture.completeExceptionally(t);
            } finally {
                asyncRunningTasks.remove(key);
            }
        });

        return (CompletableFuture<T>) newFuture;
    }
}
