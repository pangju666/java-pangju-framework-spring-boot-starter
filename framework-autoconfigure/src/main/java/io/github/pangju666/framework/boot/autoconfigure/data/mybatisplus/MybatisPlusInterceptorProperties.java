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

package io.github.pangju666.framework.boot.autoconfigure.data.mybatisplus;

import com.baomidou.mybatisplus.annotation.DbType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MyBatis-Plus 插件配置属性。
 *
 * <p><strong>前缀</strong>：{@code mybatis-plus.plugins}</p>
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>配置并启用以下内置插件：分页、乐观锁、防全表更新与删除。</li>
 *   <li>通过属性控制插件的开关与行为（数据库类型、Wrapper 模式等）。</li>
 * </ul>
 *
 * <p><strong>属性映射（含默认值）</strong></p>
 * <ul>
 *   <li>{@code mybatis-plus.plugins.pagination.enabled}：是否启用分页，默认 {@code true}。</li>
 *   <li>{@code mybatis-plus.plugins.pagination.db-type}：数据库类型，默认 {@code MYSQL}（{@link com.baomidou.mybatisplus.annotation.DbType}）。</li>
 *   <li>{@code mybatis-plus.plugins.optimistic-locker.enabled}：是否启用乐观锁，默认 {@code true}。</li>
 *   <li>{@code mybatis-plus.plugins.optimistic-locker.wrapper-mode}：是否启用 Wrapper 模式，默认 {@code true}。</li>
 *   <li>{@code mybatis-plus.plugins.block-attack.enabled}：是否启用防全表更新与删除，默认 {@code true}。</li>
 * </ul>
 *
 * <p><strong>配置示例</strong></p>
 * <pre>
 * mybatis-plus:
 *   plugins:
 *     pagination:
 *       enabled: true
 *       db-type: MYSQL
 *     optimistic-locker:
 *       enabled: true
 *       wrapper-mode: true
 *     block-attack:
 *       enabled: true
 * </pre>
 *
 * @author pangju666
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "mybatis-plus.plugins")
public class MybatisPlusInterceptorProperties {
	/**
	 * 分页插件配置
	 * <p>
	 * 用于配置MyBatis-Plus的分页插件，默认启用。
	 * 支持配置数据库类型和自定义方言实现。
	 * </p>
	 *
	 * @see com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor
	 * @since 1.0.0
	 */
	private Pagination pagination = new Pagination();
	/**
	 * 乐观锁插件配置
	 * <p>
	 * 用于配置MyBatis-Plus的乐观锁插件，默认开启。
	 * 通过版本号机制实现乐观锁，防止更新丢失问题。
	 * </p>
	 *
	 * @see com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor
	 * @since 1.0.0
	 */
	private OptimisticLocker optimisticLocker = new OptimisticLocker();
	/**
	 * 防全表更新与删除插件配置
	 * <p>
	 * 用于配置MyBatis-Plus的防全表更新与删除插件，默认开启。
	 * 防止无条件的全表更新或删除操作，提高数据安全性。
	 * </p>
	 *
	 * @see com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor
	 * @since 1.0.0
	 */
	private BlockAttack blockAttack = new BlockAttack();

	public Pagination getPagination() {
		return pagination;
	}

	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}

	public OptimisticLocker getOptimisticLocker() {
		return optimisticLocker;
	}

	public void setOptimisticLocker(OptimisticLocker optimisticLocker) {
		this.optimisticLocker = optimisticLocker;
	}

	public BlockAttack getBlockAttack() {
		return blockAttack;
	}

	public void setBlockAttack(BlockAttack blockAttack) {
		this.blockAttack = blockAttack;
	}

	/**
	 * 分页插件配置内部类
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class Pagination {
		/**
		 * 是否启用分页插件，默认为true
		 *
		 * @since 1.0.0
		 */
		private boolean enabled = true;
		/**
		 * 数据库类型，默认为MySQL
		 *
		 * @since 1.0.0
		 */
		private DbType dbType = DbType.MYSQL;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public DbType getDbType() {
			return dbType;
		}

		public void setDbType(DbType dbType) {
			this.dbType = dbType;
		}
	}

	/**
	 * 乐观锁插件配置内部类
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class OptimisticLocker {
		/**
		 * 是否启用乐观锁插件，默认为true
		 *
		 * @since 1.0.0
		 */
		private boolean enabled = true;
		/**
		 * 是否启用wrapper模式，默认为true
		 * <p>
		 * 启用wrapper模式后，在Wrapper方式更新时也可以使用乐观锁
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private boolean wrapperMode = true;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public boolean isWrapperMode() {
			return wrapperMode;
		}

		public void setWrapperMode(boolean wrapperMode) {
			this.wrapperMode = wrapperMode;
		}
	}

	/**
	 * 防全表更新与删除插件配置内部类
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class BlockAttack {
		/**
		 * 是否启用防全表更新与删除插件，默认为true
		 *
		 * @since 1.0.0
		 */
		private boolean enabled = true;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}
}
