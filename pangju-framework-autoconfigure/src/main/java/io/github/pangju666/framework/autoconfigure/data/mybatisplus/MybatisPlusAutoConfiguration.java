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

import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.IDialect;
import io.github.pangju666.framework.data.mybatisplus.injector.TableLogicFillSqlInjector;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * MyBatis-Plus自动配置类
 * <p>
 * 该类用于自动配置MyBatis-Plus相关的拦截器和SQL注入器。
 * 配置优先级在{@code com.baomidou.mybatisplus.autoconfigure.MybatisPlusInnerInterceptorAutoConfiguration}之后执行。
 * </p>
 * <p>
 * 支持的功能包括：
 * <ul>
 *     <li>数据权限插件</li>
 *     <li>多租户插件</li>
 *     <li>动态表名插件</li>
 *     <li>分页插件</li>
 *     <li>乐观锁插件</li>
 *     <li>防全表更新与删除插件</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
@AutoConfiguration(after = com.baomidou.mybatisplus.autoconfigure.MybatisPlusInnerInterceptorAutoConfiguration.class)
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties(MybatisPlusInterceptorProperties.class)
public class MybatisPlusAutoConfiguration implements BeanFactoryAware {
	private final Logger LOGGER = LoggerFactory.getLogger(MybatisPlusAutoConfiguration.class);

	private BeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * 创建MyBatis-Plus拦截器
	 * <p>
	 * 根据配置属性动态加载以下拦截器：
	 * <ul>
	 *     <li>数据权限拦截器 - 用于实现数据权限控制</li>
	 *     <li>多租户拦截器 - 用于实现多租户数据隔离</li>
	 *     <li>动态表名拦截器 - 用于动态修改表名</li>
	 *     <li>分页拦截器 - 用于实现分页查询</li>
	 *     <li>乐观锁拦截器 - 用于实现乐观锁机制</li>
	 *     <li>防全表更新与删除拦截器 - 用于防止误操作全表</li>
	 * </ul>
	 * </p>
	 *
	 * @param properties MyBatis-Plus拦截器配置属性
	 * @return 配置好的MyBatis-Plus拦截器实例
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean
	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor(MybatisPlusInterceptorProperties properties) {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

		// 启用数据权限插件插件 https://baomidou.com/plugins/data-permission/
		if (properties.getDataPermission().isEnabled()) {
			Class<? extends DataPermissionHandler> handlerClass = properties.getDataPermission().getHandler();
			try {
				DataPermissionHandler handler = getInstanceOrBean(handlerClass, DataPermissionHandler.class);
				interceptor.addInnerInterceptor(new DataPermissionInterceptor(handler));
			} catch (BeansException e) {
				LOGGER.error("数据权限插件加载失败，未找到相关处理器", e);
			}
		}

		// 启用多租户插件 https://baomidou.com/plugins/tenant/
		if (properties.getTenantLine().isEnabled()) {
			Class<? extends TenantLineHandler> handlerClass = properties.getTenantLine().getHandler();
			try {
				TenantLineHandler handler = getInstanceOrBean(handlerClass, TenantLineHandler.class);
				interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(handler));
			} catch (BeansException e) {
				LOGGER.error("多租户插件加载失败，未找到相关处理器", e);
			}
		}

		// 启用动态表名插件 https://baomidou.com/plugins/dynamic-table-name/
		if (properties.getDynamicTableName().isEnabled()) {
			Class<? extends TableNameHandler> handlerClass = properties.getDynamicTableName().getHandler();
			try {
				TableNameHandler handler = getInstanceOrBean(handlerClass, TableNameHandler.class);
				if (properties.getDynamicTableName().isJsqlParser()) {
					interceptor.addInnerInterceptor(new DynamicTableNameJsqlParserInnerInterceptor(handler));
				} else {
					interceptor.addInnerInterceptor(new DynamicTableNameInnerInterceptor(handler));
				}
			} catch (BeansException e) {
				LOGGER.error("动态表名插件加载失败，未找到相关处理器", e);
			}
		}

		// 启用分页插件 https://baomidou.com/plugins/pagination/
		if (properties.getPagination().isEnabled()) {
			Class<? extends IDialect> dialectClass = properties.getPagination().getDialect();
			if (Objects.nonNull(dialectClass)) {
				try {
					IDialect dialect = getInstanceOrBean(dialectClass, IDialect.class);
					interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dialect));
				} catch (BeansException e) {
					LOGGER.error("分页插件加载失败，未找到方言实现类", e);
				}
			} else {
				interceptor.addInnerInterceptor(new PaginationInnerInterceptor(properties.getPagination().getDbType()));
			}
		}

		// 启用乐观锁插件 https://baomidou.com/plugins/optimistic-locker/
		if (properties.getOptimisticLocker().isEnabled()) {
			interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor(properties.getOptimisticLocker().isWrapperMode()));
		}

		// 启用防全表更新与删除插件 https://baomidou.com/plugins/block-attack/
		if (properties.getBlockAttack().isEnabled()) {
			interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
		}

		return interceptor;
	}

	private <T> T getInstanceOrBean(Class<? extends T> clazz, Class<T> superClass) throws BeansException {
		if (Objects.nonNull(clazz)) {
			try {
				return clazz.getDeclaredConstructor().newInstance();
			} catch (InvocationTargetException | InstantiationException | IllegalAccessException |
					 NoSuchMethodException ignored) {
				return beanFactory.getBean(superClass);
			}
		} else {
			return beanFactory.getBean(superClass);
		}
	}

	/**
	 * 表逻辑填充SQL注入器配置类
	 * <p>
	 * 该内部配置类用于注册{@link TableLogicFillSqlInjector}，
	 * 提供表逻辑删除字段的自动填充功能。
	 * </p>
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(TableLogicFillSqlInjector.class)
	static class TableLogicFillSqlInjectorConfiguration {
		/**
		 * 创建表逻辑填充SQL注入器
		 * <p>
		 * 该Bean具有最高优先级+1的顺序，确保在其他SQL注入器之前加载。
		 * </p>
		 *
		 * @return 表逻辑填充SQL注入器实例
		 * @since 1.0.0
		 */
		@Order(Ordered.HIGHEST_PRECEDENCE + 1)
		@ConditionalOnMissingBean
		@Bean
		public ISqlInjector tableLogicFillSqlInjector() {
			return new TableLogicFillSqlInjector();
		}
	}
}
