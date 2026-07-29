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
import io.github.pangju666.framework.boot.data.mybatisplus.annotation.TableLogicFill;

import java.util.Objects;

import static java.util.stream.Collectors.joining;

/**
 * 逻辑删除字段填充工具类（内部使用）
 * <p>
 * 提供用于处理逻辑删除时字段自动填充的工具方法。
 * 该工具类主要用于生成包含自定义填充字段的SQL语句，
 * 这些字段通过{@link io.github.pangju666.framework.boot.data.mybatisplus.annotation.TableLogicFill}注解标注。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>扫描实体类字段，查找标注了TableLogicFill注解的字段</li>
 *     <li>根据注解配置生成对应的SQL SET语句</li>
 *     <li>将生成的SET语句拼接成完整的SQL片段</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：在逻辑删除时需要自动填充额外的字段值（如删除时间、删除人ID等）
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
final class TableLogicFillUtils {
	private TableLogicFillUtils() {
	}

	/**
	 * 生成逻辑删除的SET SQL语句
	 * <p>
	 * 该方法会扫描表的所有字段，筛选出标注了
	 * {@link io.github.pangju666.framework.boot.data.mybatisplus.annotation.TableLogicFill}注解的字段，
	 * 并根据注解配置生成对应的SET语句。
	 * </p>
	 * <p>
	 * 生成的SQL格式为：column1=value1,column2=value2,...
	 * 其中value值来自注解的value属性，可以是SQL表达式或常量值。
	 * </p>
	 * <p>
	 * 示例：
	 * <pre>
	 * // 如果字段标注了：@TableLogicFill("NOW()")
	 * // 生成的SQL：deleted_time=NOW()
	 * </pre>
	 * </p>
	 *
	 * @param tableInfo 表信息对象，包含字段列表等元数据
	 * @return 包含所有自定义填充字段的SET语句片段，多个字段用逗号分隔
	 */
	static String sqlLogicFillSet(final TableInfo tableInfo) {
		return tableInfo.getFieldList()
			.stream()
			.filter(info -> Objects.nonNull(info.getField().getAnnotation(TableLogicFill.class)))
			.map(info -> info.getColumn() + "=" + info.getField().getAnnotation(TableLogicFill.class).value())
			.collect(joining(","));
	}
}
