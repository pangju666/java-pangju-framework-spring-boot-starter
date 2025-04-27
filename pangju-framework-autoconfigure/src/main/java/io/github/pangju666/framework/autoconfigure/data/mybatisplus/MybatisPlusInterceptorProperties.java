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

@ConfigurationProperties(prefix = "pangju.mybatis-plus.plugins")
public class MybatisPlusInterceptorProperties {
	/**
	 * 分页插件，默认开启
	 */
	private Pagination pagination = new Pagination();
	/**
	 * 多租户插件，默认关闭
	 */
	private TenantLine tenantLine = new TenantLine();
	/**
	 * 动态表名插件，默认关闭
	 */
	private DynamicTableName dynamicTableName = new DynamicTableName();
	/**
	 * 乐观锁插件，默认开启
	 */
	private OptimisticLocker optimisticLocker = new OptimisticLocker();
	/**
	 * 防止全表更新与删除插件，默认开启
	 */
	private BlockAttack blockAttack = new BlockAttack();
	/**
	 * 数据权限插件插件，默认关闭
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

	public static class Pagination {
		private boolean enabled = true;
		private DbType dbType = DbType.MYSQL;
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

	public static class TenantLine {
		private boolean enabled = false;
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

	public static class DynamicTableName {
		private boolean enabled = false;
		private boolean jsqlParser = true;
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

	public static class OptimisticLocker {
		private boolean enabled = true;
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

	public static class BlockAttack {
		private boolean enabled = true;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}

	public static class DataPermission {
		private boolean enabled = false;
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
