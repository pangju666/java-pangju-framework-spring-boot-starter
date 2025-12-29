
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
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * MyBatis-Plus 内部拦截器自动配置类。
 *
 * <p>负责根据配置属性自动注册 MyBatis-Plus 的常用内部拦截器，包括：</p>
 * <ul>
 *   <li>{@link PaginationInnerInterceptor}：分页拦截器，支持多种数据库方言。</li>
 *   <li>{@link OptimisticLockerInnerInterceptor}：乐观锁拦截器，用于并发控制。</li>
 *   <li>{@link BlockAttackInnerInterceptor}：防全表更新与删除拦截器，保护数据安全。</li>
 * </ul>
 *
 * <p><b>生效条件</b></p>
 * <ul>
 *   <li>类路径中存在 {@link InnerInterceptor} 接口。</li>
 *   <li>容器中尚未配置 {@link MybatisPlusInterceptor} Bean（若用户自定义了拦截器链，则本配置不生效）。</li>
 * </ul>
 *
 * @author pangju666
 * @see MybatisPlusInterceptorProperties
 * @see PaginationInnerInterceptor
 * @see OptimisticLockerInnerInterceptor
 * @see BlockAttackInnerInterceptor
 * @see com.baomidou.mybatisplus.autoconfigure.MybatisPlusInnerInterceptorAutoConfiguration
 * @since 1.0.0
 */
@AutoConfiguration(before = com.baomidou.mybatisplus.autoconfigure.MybatisPlusInnerInterceptorAutoConfiguration.class)
@ConditionalOnClass(InnerInterceptor.class)
@EnableConfigurationProperties(MybatisPlusInterceptorProperties.class)
public class MybatisPlusInnerInterceptorAutoConfiguration {
	/**
	 * 注册分页拦截器。
	 *
	 * <p>用于支持 MyBatis-Plus 的物理分页功能。可以通过配置文件指定数据库方言。</p>
	 *
	 * <p><b>生效条件</b></p>
	 * <ul>
	 *   <li>类路径中存在 {@link PaginationInnerInterceptor} 类。</li>
	 *   <li>属性 {@code mybatis-plus.plugins.pagination.enabled} 为 {@code true}（默认）。</li>
	 *   <li>容器中尚未存在 {@link PaginationInnerInterceptor} Bean。</li>
	 * </ul>
	 *
	 * <p><b>配置项</b></p>
	 * <ul>
	 *   <li>{@code mybatis-plus.plugins.pagination.db-type}：数据库类型（默认为 {@link DbType#MYSQL}）。</li>
	 * </ul>
	 *
	 * @param properties 拦截器配置属性
	 * @return 分页内部拦截器实例
	 * @see PaginationInnerInterceptor
	 * @since 1.0.0
	 */
	@Order(Ordered.HIGHEST_PRECEDENCE + 11)
	@ConditionalOnClass(PaginationInnerInterceptor.class)
	@ConditionalOnBooleanProperty(prefix = "mybatis-plus.plugins.pagination", value = "enabled", matchIfMissing = true)
	@ConditionalOnMissingBean(PaginationInnerInterceptor.class)
	@Bean
	public PaginationInnerInterceptor paginationInnerInterceptor(MybatisPlusInterceptorProperties properties) {
		return new PaginationInnerInterceptor(properties.getPagination().getDbType());
	}

	/**
	 * 注册乐观锁拦截器。
	 *
	 * <p>用于实现数据库记录的乐观锁并发控制。需配合 {@code @Version} 注解使用。</p>
	 *
	 * <p><b>生效条件</b></p>
	 * <ul>
	 *   <li>类路径中存在 {@link OptimisticLockerInnerInterceptor} 类。</li>
	 *   <li>属性 {@code mybatis-plus.plugins.optimistic-locker.enabled} 为 {@code true}（默认）。</li>
	 *   <li>容器中尚未存在 {@link OptimisticLockerInnerInterceptor} Bean。</li>
	 * </ul>
	 *
	 * <p><b>配置项</b></p>
	 * <ul>
	 *   <li>{@code mybatis-plus.plugins.optimistic-locker.wrapper-mode}：是否开启 Wrapper 模式（默认为 {@code true}）。</li>
	 * </ul>
	 *
	 * @param properties 拦截器配置属性
	 * @return 乐观锁内部拦截器实例
	 * @see OptimisticLockerInnerInterceptor
	 * @since 1.0.0
	 */
	@Order(Ordered.HIGHEST_PRECEDENCE + 12)
	@ConditionalOnClass(OptimisticLockerInnerInterceptor.class)
	@ConditionalOnBooleanProperty(prefix = "mybatis-plus.plugins.optimistic-locker", value = "enabled", matchIfMissing = true)
	@ConditionalOnMissingBean(OptimisticLockerInnerInterceptor.class)
	@Bean
	public OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor(MybatisPlusInterceptorProperties properties) {
		return new OptimisticLockerInnerInterceptor(properties.getOptimisticLocker().isWrapperMode());
	}

	/**
	 * 注册防全表更新与删除拦截器。
	 *
	 * <p>用于防止误操作导致的全表更新或删除（即不带 WHERE 子句的 UPDATE/DELETE 语句）。</p>
	 *
	 * <p><b>生效条件</b></p>
	 * <ul>
	 *   <li>类路径中存在 {@link BlockAttackInnerInterceptor} 类。</li>
	 *   <li>属性 {@code mybatis-plus.plugins.block-attack.enabled} 为 {@code true}（默认）。</li>
	 *   <li>容器中尚未存在 {@link BlockAttackInnerInterceptor} Bean。</li>
	 * </ul>
	 *
	 * @return 防全表攻击内部拦截器实例
	 * @see BlockAttackInnerInterceptor
	 * @since 1.0.0
	 */
	@Order(Ordered.HIGHEST_PRECEDENCE + 22)
	@ConditionalOnClass(BlockAttackInnerInterceptor.class)
	@ConditionalOnBooleanProperty(prefix = "mybatis-plus.plugins.block-attack", value = "enabled", matchIfMissing = true)
	@ConditionalOnMissingBean(BlockAttackInnerInterceptor.class)
	@Bean
	public BlockAttackInnerInterceptor blockAttackInnerInterceptor() {
		return new BlockAttackInnerInterceptor();
	}
}
