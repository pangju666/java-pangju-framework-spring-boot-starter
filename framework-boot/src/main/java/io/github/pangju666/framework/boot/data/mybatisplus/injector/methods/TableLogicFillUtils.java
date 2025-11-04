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

package io.github.pangju666.framework.boot.data.mybatisplus.injector.methods;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import io.github.pangju666.framework.boot.data.mybatisplus.annotation.TableLogicFill;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Field;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * 逻辑删除字段填充工具类（内部使用）
 * <p>
 * 提供了一系列用于处理逻辑删除时字段自动填充的工具方法。
 * 主要用于生成包含自定义填充字段的SQL语句。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
final class TableLogicFillUtils {
	private TableLogicFillUtils() {
	}

	/**
	 * 判断字段是否标注了TableLogicFill注解
	 * <p>
	 * 通过反射检查字段是否包含TableLogicFill注解
	 * </p>
	 *
	 * @param field 需要检查的字段
	 * @return 如果字段包含TableLogicFill注解则返回true，否则返回false
	 */
	private static boolean isTableLogicFill(final Field field) {
		return field.getAnnotation(TableLogicFill.class) != null;
	}

	/**
	 * 获取字段的填充SQL片段
	 * <p>
	 * 根据字段的TableLogicFill注解生成对应的SQL赋值语句
	 * </p>
	 *
	 * @param info 表字段信息
	 * @return 生成的SQL赋值语句，格式为"column=value,"
	 */
	private static String getFillSql(final TableFieldInfo info) {
		TableLogicFill logicDelFill = info.getField().getAnnotation(TableLogicFill.class);
		return info.getColumn() + "=" + logicDelFill.value() + ",";
	}

	/**
	 * 获取需要填充的字段列表
	 * <p>
	 * 从表信息中筛选出所有标注了TableLogicFill注解的字段
	 * </p>
	 *
	 * @param tableInfo 表信息
	 * @return 需要填充的字段列表
	 */
	private static List<TableFieldInfo> getFillFieldInfoList(final TableInfo tableInfo) {
		return tableInfo.getFieldList()
			.stream()
			.filter(i -> TableLogicFillUtils.isTableLogicFill(i.getField()))
			.toList();
	}

	/**
	 * 生成逻辑删除的SET SQL语句
	 * <p>
	 * 根据表信息生成包含所有需要填充字段的SET语句，
	 * 并附加上MyBatis-Plus原生的逻辑删除SQL
	 * </p>
	 *
	 * @param tableInfo 表信息
	 * @return 完整的SET SQL语句
	 */
	public static String logicDeleteSetSql(final TableInfo tableInfo) {
		List<TableFieldInfo> list = getFillFieldInfoList(tableInfo);
		String sqlSet = "";
		if (!CollectionUtils.isEmpty(list)) {
			sqlSet = list.stream()
				.map(TableLogicFillUtils::getFillSql)
				.collect(joining(""));
		}
		sqlSet += tableInfo.getLogicDeleteSql(false, false);
		return "SET " + sqlSet;
	}
}
