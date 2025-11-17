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

import io.github.pangju666.framework.boot.concurrent.KeyBasedLockExecutor;
import io.github.pangju666.framework.boot.concurrent.KeyBasedLockTask;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redisson 的分布式键锁实现。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>通过 Redis 分布式锁保障同一键的任务在分布式环境下互斥执行。</li>
 *   <li>支持可选键前缀，便于在多业务场景下进行命名空间隔离。</li>
 *   <li>使用租约时间（leaseTime）避免死锁，超时后由 Redisson 自动释放。</li>
 * </ul>
 *
 * <p><strong>释放策略</strong>：始终在 {@code finally} 中解锁，并在解锁前判断是否由当前线程持有；当设置了租约时间时，Redisson 也会在到期时自动释放。</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class RedissonKeyBasedLockExecutor implements KeyBasedLockExecutor {
    /**
     * Redis 键路径分隔符。
     *
     * @since 1.0.0
     */
    protected static final String REDIS_PATH_DELIMITER = ":";

    /**
     * Redisson 客户端实例，用于创建与操作分布式锁。
     *
     * @since 1.0.0
     */
    private final RedissonClient redissonClient;
    /**
     * 锁的租约时间。超过该时间后，锁将自动释放。
     *
     * @since 1.0.0
     */
    private final long leaseTime;
    /**
     * 租约时间单位。
     *
     * @since 1.0.0
     */
    private final TimeUnit unit;
    /**
     * 键前缀。为空时不添加前缀。
     *
     * @since 1.0.0
     */
    private final String keyPrefix;

    /**
     * 创建基于 Redisson 的键锁执行器。
     *
     * <p>参数校验规则：</p>
     * <p>如果 {@code keyPrefix} 为空，则不设置前缀；如果 {@code unit} 为空，将可能导致加锁抛出异常，调用方需保证非空；{@code leaseTime} 建议为正数以启用自动释放。</p>
     *
     * @param redissonClient Redisson 客户端
     * @param keyPrefix      键前缀，可为空
     * @param leaseTime      租约时间
     * @param unit           时间单位
     * @since 1.0.0
     */
    public RedissonKeyBasedLockExecutor(RedissonClient redissonClient, String keyPrefix, long leaseTime, TimeUnit unit) {
        this.redissonClient = redissonClient;
        this.leaseTime = leaseTime;
        this.unit = unit;
        this.keyPrefix = StringUtils.isBlank(keyPrefix) ? StringUtils.EMPTY : keyPrefix + REDIS_PATH_DELIMITER;
    }

	/**
	 * 使用指定键获取分布式锁并执行任务，执行结束后释放锁。
	 *
	 * <p>释放策略：始终在 {@code finally} 中解锁，并在解锁前判断是否由当前线程持有。</p>
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

        RLock lock = redissonClient.getLock(keyPrefix + key);
        lock.lock(leaseTime, unit);

		try {
			return task.execute(key);
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}
}
