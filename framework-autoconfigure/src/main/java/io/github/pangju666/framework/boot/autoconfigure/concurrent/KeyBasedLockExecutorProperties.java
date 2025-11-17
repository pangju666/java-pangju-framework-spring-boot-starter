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

import java.util.concurrent.TimeUnit;

/**
 * 基于键的锁执行器配置属性。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>属性前缀：{@code pangju.concurrent.executor.key-based-lock}。</li>
 *   <li>包含 Guava（进程内锁）与 Redisson（分布式锁）两种实现的配置。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "pangju.concurrent.executor.key-based-lock")
public class KeyBasedLockExecutorProperties {
	/**
	 * Redisson 分布式锁配置。
	 *
	 * @since 1.0.0
	 */
	private Redisson redisson = new Redisson();
	/**
	 * Guava Striped 进程内锁配置。
	 *
	 * @since 1.0.0
	 */
	private Guava guava = new Guava();
	/**
	 * 锁实现类型选择。
	 *
	 * <p>默认值：{@link LockType#GUAVA}。</p>
	 *
	 * @since 1.0.0
	 */
	private LockType type = LockType.GUAVA;

	/**
	 * 获取锁实现类型。
	 *
	 * @return 锁类型，可能为 {@code null}
	 * @since 1.0.0
	 */
	public LockType getType() {
		return type;
	}

	/**
	 * 设置锁实现类型。
	 *
	 * <p>参数校验规则：如果 {@code type} 为空，则不设置。</p>
	 *
	 * @param type 锁类型
	 * @since 1.0.0
	 */
	public void setType(LockType type) {
		this.type = type;
	}

	/**
	 * 获取 Redisson 分布式锁配置。
	 *
	 * @return Redisson 配置对象
	 * @since 1.0.0
	 */
	public Redisson getRedisson() {
		return redisson;
	}

	/**
	 * 设置 Redisson 分布式锁配置。
	 *
	 * <p>参数校验规则：如果 {@code redisson} 为空，则不设置。</p>
	 *
	 * @param redisson Redisson 配置对象
	 * @since 1.0.0
	 */
	public void setRedisson(Redisson redisson) {
		this.redisson = redisson;
	}

	/**
	 * 获取 Guava Striped 进程内锁配置。
	 *
	 * @return Guava 配置对象
	 * @since 1.0.0
	 */
	public Guava getGuava() {
		return guava;
	}

	/**
	 * 设置 Guava Striped 进程内锁配置。
	 *
	 * <p>参数校验规则：如果 {@code guava} 为空，则不设置。</p>
	 *
	 * @param guava Guava 配置对象
	 * @since 1.0.0
	 */
	public void setGuava(Guava guava) {
		this.guava = guava;
	}

	/**
	 * 锁实现类型枚举。
	 *
	 * @since 1.0.0
	 */
	public enum LockType {
		/**
		 * 使用 Redisson 分布式锁实现。
		 *
		 * @since 1.0.0
		 */
		REDISSON,
		/**
		 * 使用 Guava Striped 进程内锁实现。
		 *
		 * @since 1.0.0
		 */
		GUAVA
	}

	public static class Guava {
		/**
		 * 锁条带数量（建议为正数）。
		 *
		 * @since 1.0.0
		 */
		private int stripes = 64;

		/**
		 * 获取锁条带数量。
		 *
		 * @return 条带数量
		 * @since 1.0.0
		 */
		public int getStripes() {
			return stripes;
		}

		/**
		 * 设置锁条带数量。
		 *
		 * <p>参数校验规则：如果 {@code stripes} ≤ 0，则不设置或保留默认值。</p>
		 *
		 * @param stripes 条带数量
		 * @since 1.0.0
		 */
		public void setStripes(int stripes) {
			this.stripes = stripes;
		}
	}

	public static class Redisson {
		/**
		 * 锁的租约时间（建议为正数）。
		 *
		 * @since 1.0.0
		 */
		private long leaseTime = 10;
		/**
		 * 租约时间单位。
		 *
		 * @since 1.0.0
		 */
		private TimeUnit unit = TimeUnit.MINUTES;
		/**
		 * 键前缀（可为空）。
		 *
		 * @since 1.0.0
		 */
		private String prefix;

		/**
		 * 获取锁的租约时间。
		 *
		 * @return 租约时间
		 * @since 1.0.0
		 */
		public long getLeaseTime() {
			return leaseTime;
		}

		/**
		 * 设置锁的租约时间。
		 *
		 * <p>参数校验规则：如果 {@code leaseTime} ≤ 0，则不设置或保留默认值。</p>
		 *
		 * @param leaseTime 租约时间
		 * @since 1.0.0
		 */
		public void setLeaseTime(long leaseTime) {
			this.leaseTime = leaseTime;
		}

		/**
		 * 获取租约时间单位。
		 *
		 * @return 时间单位
		 * @since 1.0.0
		 */
		public TimeUnit getUnit() {
			return unit;
		}

		/**
		 * 设置租约时间单位。
		 *
		 * <p>参数校验规则：如果 {@code unit} 为空，则不设置或保留默认值。</p>
		 *
		 * @param unit 时间单位
		 * @since 1.0.0
		 */
		public void setUnit(TimeUnit unit) {
			this.unit = unit;
		}

		/**
		 * 获取键前缀。
		 *
		 * @return 键前缀
		 * @since 1.0.0
		 */
		public String getPrefix() {
			return prefix;
		}

		/**
		 * 设置键前缀。
		 *
		 * <p>参数校验规则：如果 {@code prefix} 为空，则不设置。</p>
		 *
		 * @param prefix 键前缀
		 * @since 1.0.0
		 */
		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}
	}
}
