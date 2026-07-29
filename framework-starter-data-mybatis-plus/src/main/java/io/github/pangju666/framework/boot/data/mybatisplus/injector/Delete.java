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

package io.github.pangju666.framework.boot.data.mybatisplus.injector;

import com.baomidou.mybatisplus.core.metadata.TableInfo;

/**
 * 自定义删除方法实现类
 * <p>
 * 继承自MyBatis-Plus的{@link com.baomidou.mybatisplus.core.injector.methods.Delete}，
 * 重写了逻辑删除的SQL生成逻辑，支持在逻辑删除时自动填充自定义字段。
 * </p>
 * <p>
 * 当表配置了逻辑删除时，除了设置逻辑删除字段外，还会通过
 * {@link TableLogicFillUtils#sqlLogicFillSet(TableInfo)}方法
 * 自动填充标注了{@link io.github.pangju666.framework.boot.data.mybatisplus.annotation.TableLogicFill}注解的字段。
 * </p>
 * <p>
 * 使用场景：在逻辑删除时需要同时更新其他字段（如删除时间、删除人等）
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
final class Delete extends com.baomidou.mybatisplus.core.injector.methods.Delete {
	public Delete() {
		super();
	}

	/**
	 * 重写逻辑删除的SET语句生成方法
	 * <p>
	 * 在MyBatis-Plus原生的逻辑删除SET语句基础上，
	 * 追加自定义逻辑删除填充字段的SET语句。
	 * </p>
	 *
	 * @param table 表信息对象
	 * @return 包含逻辑删除字段和自定义填充字段的SET语句
	 */
	@Override
	protected String sqlLogicSet(TableInfo table) {
		return "SET " + table.getLogicDeleteSql(false, false) + "," +
			TableLogicFillUtils.sqlLogicFillSet(table);
	}
}
