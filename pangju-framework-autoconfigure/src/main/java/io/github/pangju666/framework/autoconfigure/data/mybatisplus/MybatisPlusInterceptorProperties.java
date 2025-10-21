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

package io.github.pangju666.framework.autoconfigure.data.mybatisplus;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.IDialect;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MyBatis-Plus插件配置属性类
 * <p>
 * 用于配置MyBatis-Plus的各种插件，包括分页、多租户、动态表名、乐观锁、防全表更新与删除、数据权限等。
 * 通过在配置文件中设置相应的属性，可以启用或禁用这些插件，并配置它们的行为。
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * mybatis-plus:
 *   plugins:
 *     # 分页插件配置
 *     pagination:
 *       enabled: true
 *       db-type: MYSQL
 *       dialect: # 自定义方言类，不配置时使用默认方言
 *     # 多租户插件配置
 *     tenant-line:
 *       enabled: false
 *       handler: # 租户处理器实现类
 *     # 动态表名插件配置
 *     dynamic-table-name:
 *       enabled: false
 *       jsql-parser: true
 *       handler: # 表名处理器实现类
 *     # 乐观锁插件配置
 *     optimistic-locker:
 *       enabled: true
 *       wrapper-mode: true
 *     # 防全表更新与删除插件配置
 *     block-attack:
 *       enabled: true
 *     # 数据权限插件配置
 *     data-permission:
 *       enabled: false
 *       handler: # 数据权限处理器实现类
 * </pre>
 * </p>
 *
 * @author pangju666
 * @see com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor
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
	 * 多租户插件配置
	 * <p>
	 * 用于配置MyBatis-Plus的多租户插件，默认关闭。
	 * 通过指定租户处理器来实现多租户数据隔离。
	 * </p>
	 *
	 * @see com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor
	 * @see com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler
	 * @since 1.0.0
	 */
	private TenantLine tenantLine = new TenantLine();
	/**
	 * 动态表名插件配置
	 * <p>
	 * 用于配置MyBatis-Plus的动态表名插件，默认关闭。
	 * 支持在运行时动态修改表名，适用于分表场景。
	 * </p>
	 *
	 * @see com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor
	 * @see com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameJsqlParserInnerInterceptor
	 * @since 1.0.0
	 */
	private DynamicTableName dynamicTableName = new DynamicTableName();
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
	/**
	 * 数据权限插件配置
	 * <p>
	 * 用于配置MyBatis-Plus的数据权限插件，默认关闭。
	 * 通过指定数据权限处理器来实现细粒度的数据访问控制。
	 * </p>
	 *
	 * @see com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor
	 * @see com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler
	 * @since 1.0.0
	 */
	private DataPermission dataPermission = new DataPermission();

	public DataPermission getDataPermission() {
		return dataPermission;
	}

	public void setDataPermission(DataPermission dataPermission) {
		this.dataPermission = dataPermission;
	}

	public Pagination getPagination() {
		return pagination;
	}

	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}

	public TenantLine getTenantLine() {
		return tenantLine;
	}

	public void setTenantLine(TenantLine tenantLine) {
		this.tenantLine = tenantLine;
	}

	public DynamicTableName getDynamicTableName() {
		return dynamicTableName;
	}

	public void setDynamicTableName(DynamicTableName dynamicTableName) {
		this.dynamicTableName = dynamicTableName;
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
		/**
		 * 自定义分页方言实现类
		 *
		 * @since 1.0.0
		 */
		private Class<? extends IDialect> dialect;

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

		public Class<? extends IDialect> getDialect() {
			return dialect;
		}

		public void setDialect(Class<? extends IDialect> dialect) {
			this.dialect = dialect;
		}
	}

	/**
	 * 多租户插件配置内部类
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class TenantLine {
		/**
		 * 是否启用多租户插件，默认为false
		 *
		 * @since 1.0.0
		 */
		private boolean enabled = false;
		/**
		 * 租户处理器实现类
		 *
		 * @since 1.0.0
		 */
		private Class<? extends TenantLineHandler> handler;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public Class<? extends TenantLineHandler> getHandler() {
			return handler;
		}

		public void setHandler(Class<? extends TenantLineHandler> handler) {
			this.handler = handler;
		}
	}

	/**
	 * 动态表名插件配置内部类
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class DynamicTableName {
		/**
		 * 是否启用动态表名插件，默认为false
		 *
		 * @since 1.0.0
		 */
		private boolean enabled = false;
		/**
		 * 是否使用JSqlParser实现，默认为true
		 * <p>
		 * 启用JSqlParser实现可以处理复杂SQL中的表名替换，但性能略低
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private boolean jsqlParser = true;
		/**
		 * 表名处理器实现类
		 *
		 * @since 1.0.0
		 */
		private Class<? extends TableNameHandler> handler;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public Class<? extends TableNameHandler> getHandler() {
			return handler;
		}

		public void setHandler(Class<? extends TableNameHandler> handler) {
			this.handler = handler;
		}

		public boolean isJsqlParser() {
			return jsqlParser;
		}

		public void setJsqlParser(boolean jsqlParser) {
			this.jsqlParser = jsqlParser;
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
		 * 是否启用乐观锁插件，默认为false
		 *
		 * @since 1.0.0
		 */
		private boolean enabled = false;
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

	/**
	 * 数据权限插件配置内部类
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class DataPermission {
		/**
		 * 是否启用数据权限插件，默认为false
		 *
		 * @since 1.0.0
		 */
		private boolean enabled = false;
		/**
		 * 数据权限处理器实现类
		 *
		 * @since 1.0.0
		 */
		private Class<? extends DataPermissionHandler> handler;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public Class<? extends DataPermissionHandler> getHandler() {
			return handler;
		}

		public void setHandler(Class<? extends DataPermissionHandler> handler) {
			this.handler = handler;
		}
	}
}
