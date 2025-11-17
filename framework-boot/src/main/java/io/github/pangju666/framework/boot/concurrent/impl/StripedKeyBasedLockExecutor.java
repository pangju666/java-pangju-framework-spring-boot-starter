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

package io.github.pangju666.framework.boot.concurrent.impl;

import com.google.common.util.concurrent.Striped;
import io.github.pangju666.framework.boot.concurrent.KeyBasedLockExecutor;
import io.github.pangju666.framework.boot.concurrent.KeyBasedLockTask;
import org.springframework.util.Assert;

import java.util.concurrent.locks.Lock;

/**
 * 基于 Guava {@link Striped} 的进程内键锁实现。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>通过条带化锁降低锁对象数量，按键映射到固定数量的锁。</li>
 *   <li>适用于单进程内的并发控制，不提供分布式锁能力。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class StripedKeyBasedLockExecutor implements KeyBasedLockExecutor {
	/**
	 * 条带化锁集合。
	 *
	 * @since 1.0.0
	 */
	private final Striped<Lock> striped;

	/**
	 * 创建条带化锁执行器。
	 *
	 * <p>参数校验规则：如果 {@code stripes} ≤ 0，将导致锁集合不可用；调用方需提供正数。</p>
	 *
	 * @param stripes 锁条带数量
	 * @since 1.0.0
	 */
	public StripedKeyBasedLockExecutor(int stripes) {
		this.striped = Striped.lazyWeakLock(stripes);
	}

	/**
	 * 使用键映射的进程内锁保护并执行任务。
	 *
	 * <p>释放策略：始终在 {@code finally} 中释放锁。</p>
	 *
	 * @param key  锁的键标识
	 * @param task 需要在锁保护下执行的任务
	 * @param <T>  返回类型
	 * @return 任务返回结果
	 * @throws Exception 任务执行过程中抛出的异常
	 * @since 1.0.0
	 */
	@Override
	public <T> T executeWithLock(String key, KeyBasedLockTask<T> task) throws Exception {
		Assert.hasText(key, "key 不可为空");
		Assert.notNull(task, "task 不可为 null");

		Lock lock = striped.get(key);
		lock.lock();

		try {
			return task.execute(key);
		} finally {
			lock.unlock();
		}
	}
}
