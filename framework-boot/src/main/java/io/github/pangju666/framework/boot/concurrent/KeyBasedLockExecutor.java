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
 * 基于键的锁执行器接口。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>通过相同的键标识同一临界区，避免并发冲突。</li>
 *   <li>不同实现可提供进程内锁或分布式锁能力。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public interface KeyBasedLockExecutor {
	/**
	 * 使用指定键获取锁并执行任务，执行结束后释放锁。
	 *
	 * <p>参数校验规则：如果 {@code key} 为空或任务为 {@code null}，具体实现可抛出异常或忽略执行。</p>
	 *
	 * @param key  锁的键标识
	 * @param task 需要在锁保护下执行的任务
	 * @param <T>  任务返回类型
	 * @return 任务返回结果
	 * @since 1.0.0
	 */
	<T> T executeWithLock(String key, KeyBasedLockTask<T> task);
}
