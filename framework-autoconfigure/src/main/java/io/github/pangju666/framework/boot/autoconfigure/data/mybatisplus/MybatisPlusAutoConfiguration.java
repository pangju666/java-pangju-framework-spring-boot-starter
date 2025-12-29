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
import io.github.pangju666.framework.boot.data.mybatisplus.injector.TableLogicFillSqlInjector;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * MyBatis-Plus 自动配置扩展类。
 *
 * <p>用于增强 MyBatis-Plus 的默认配置，提供额外的功能支持。</p>
 *
 * <p><b>主要功能</b></p>
 * <ul>
 *   <li>注册 {@link TableLogicFillSqlInjector}：支持逻辑删除时的自定义字段填充（如删除时间、操作人等）。</li>
 * </ul>
 *
 * <p><b>生效顺序</b></p>
 * <ul>
 *   <li>在 {@link com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration} 之前执行，
 *   以便提供的 Bean（如 {@link ISqlInjector}）能被官方自动配置类引用。</li>
 * </ul>
 *
 * @author pangju666
 * @see TableLogicFillSqlInjector
 * @since 1.0.0
 */
@AutoConfiguration(before = com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class)
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@ConditionalOnSingleCandidate(DataSource.class)
public class MybatisPlusAutoConfiguration {
	/**
	 * 注册逻辑删除字段填充 SQL 注入器。
	 *
	 * <p>当容器中不存在 {@link ISqlInjector} 类型的 Bean 时，注册该注入器。</p>
	 *
	 * <p><b>功能说明</b></p>
	 * <ul>
	 *   <li>替换默认的 SQL 注入器，增强删除相关方法的逻辑。</li>
	 *   <li>支持识别 {@link io.github.pangju666.framework.boot.data.mybatisplus.annotation.TableLogicFill} 注解，
	 *   在执行逻辑删除时自动填充指定字段的值。</li>
	 * </ul>
	 *
	 * @return 逻辑删除字段填充 SQL 注入器实例
	 * @see TableLogicFillSqlInjector
	 */
	@ConditionalOnMissingBean(ISqlInjector.class)
	@Bean
	public TableLogicFillSqlInjector tableLogicFillSqlInjector() {
		return new TableLogicFillSqlInjector();
	}
}
