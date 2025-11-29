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

import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import io.github.pangju666.framework.boot.data.mybatisplus.injector.TableLogicFillSqlInjector;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;

/**
 * MyBatis-Plus 自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>注册 {@link MybatisPlusInterceptor} 并按属性启用内置拦截器（分页、乐观锁、防全表更新与删除）。</li>
 *   <li>注册自定义 SQL 注入器 {@link TableLogicFillSqlInjector}。</li>
 * </ul>
 *
 * <p><strong>条件</strong></p>
 * <ul>
 *   <li>类路径存在 {@link SqlSessionFactory} 与 {@link SqlSessionFactoryBean}。</li>
 *   <li>存在单个 {@link javax.sql.DataSource} 候选。</li>
 *   <li>启用属性绑定：{@link EnableConfigurationProperties}({@link MybatisPlusInterceptorProperties})。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties(MybatisPlusInterceptorProperties.class)
public class MybatisPlusAutoConfiguration {
    /**
     * 创建并配置 {@link MybatisPlusInterceptor}。
     *
     * <p><b>流程</b>：初始化拦截器 -> 按属性启用分页 -> 按属性启用乐观锁（支持 Wrapper 模式） -> 按属性启用防全表更新与删除 -> 返回。</p>
     * <p><b>约束</b>：仅启用当前实现内置的三类拦截器（分页/乐观锁/防全表），不包含多租户、动态表名、数据权限等。</p>
     * <p><b>顺序说明</b>：当前实现的注册顺序为：分页 -> 乐观锁 -> 防全表更新与删除，与方法体一致。</p>
     *
     * @param properties 插件配置属性
     * @return 配置好的拦截器实例
     * @since 1.0.0
     */
	@Order
	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor(MybatisPlusInterceptorProperties properties) {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

		// 启用分页插件 https://baomidou.com/plugins/pagination/
		if (properties.getPagination().isEnabled()) {
			interceptor.addInnerInterceptor(new PaginationInnerInterceptor(properties.getPagination().getDbType()));
		}

		// 启用乐观锁插件 https://baomidou.com/plugins/optimistic-locker/
		if (properties.getOptimisticLocker().isEnabled()) {
			boolean wrapperMode = properties.getOptimisticLocker().isWrapperMode();
			interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor(wrapperMode));
		}

		// 启用防全表更新与删除插件 https://baomidou.com/plugins/block-attack/
		if (properties.getBlockAttack().isEnabled()) {
			interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
		}

		return interceptor;
	}

	/**
	 * 注册自定义 SQL 注入器。
	 *
	 * <p><b>流程</b>：缺少 {@link ISqlInjector} Bean -> 创建 {@link TableLogicFillSqlInjector} -> 返回。</p>
	 * <p><b>约束</b>：受 {@link org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean} 控制，仅在未定义其它注入器时注册。</p>
	 * <p><b>作用</b>：提供逻辑字段填充等 SQL 注入扩展能力，配合项目内约定的实体与拦截器使用。</p>
	 *
	 * @return 注入器实例
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(ISqlInjector.class)
	@Bean
	public TableLogicFillSqlInjector tableLogicFillSqlInjector() {
		return new TableLogicFillSqlInjector();
	}
}
