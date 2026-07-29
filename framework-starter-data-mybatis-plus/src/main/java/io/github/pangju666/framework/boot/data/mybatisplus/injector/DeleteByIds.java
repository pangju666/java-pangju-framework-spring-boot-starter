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

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * 自定义根据ID批量删除方法实现类
 * <p>
 * 继承自MyBatis-Plus的{@link com.baomidou.mybatisplus.core.injector.methods.DeleteByIds}，
 * 重写了根据ID批量删除的SQL生成逻辑，支持在逻辑删除时自动填充自定义字段。
 * </p>
 * <p>
 * 当表配置了逻辑删除时，除了设置逻辑删除字段外，还会通过
 * {@link TableLogicFillUtils#sqlLogicFillSet(TableInfo)}方法
 * 自动填充标注了{@link io.github.pangju666.framework.boot.data.mybatisplus.annotation.TableLogicFill}注解的字段。
 * </p>
 * <p>
 * 该方法会根据表字段配置自动生成不同的SQL语句：
 * <ul>
 *     <li>如果字段配置了更新填充注解，会生成对应的SET语句（带条件判断）</li>
 *     <li>如果逻辑删除字段配置了更新填充注解，会生成条件判断的SET语句</li>
 *     <li>最后追加自定义逻辑删除填充字段的SET语句</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：在根据ID批量逻辑删除时需要同时更新其他字段（如删除时间、删除人等）
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
final class DeleteByIds extends com.baomidou.mybatisplus.core.injector.methods.DeleteByIds {
	public DeleteByIds() {
		super();
	}

	/**
	 * 重写逻辑删除脚本生成方法
	 * <p>
	 * 根据表字段配置生成逻辑删除的SQL脚本：
	 * <ul>
	 *     <li>筛选出配置了更新填充的字段（非逻辑删除字段）</li>
	 *     <li>检查逻辑删除字段是否配置了更新填充</li>
	 *     <li>生成包含所有填充字段的SET语句</li>
	 *     <li>追加自定义逻辑删除填充字段的SET语句</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 生成的SQL会使用MyBatis的动态SQL标签（如&lt;if&gt;、&lt;choose&gt;）来处理条件判断
	 * </p>
	 *
	 * @param tableInfo 表信息对象
	 * @param sqlMethod SQL方法枚举
	 * @return 完整的逻辑删除SQL脚本
	 * @since 1.0.0
	 */
	@Override
	public String logicDeleteScript(TableInfo tableInfo, SqlMethod sqlMethod) {
		List<TableFieldInfo> fieldInfos = tableInfo.getFieldList().stream()
			.filter(TableFieldInfo::isWithUpdateFill)
			.filter(f -> !f.isLogicDelete())
			.collect(toList());
		TableFieldInfo logicDeleteField = tableInfo.getLogicDeleteFieldInfo();
		boolean logicDeleteWithFill = logicDeleteField != null && logicDeleteField.isWithUpdateFill();
		String sqlSet = "SET ";
		if (CollectionUtils.isNotEmpty(fieldInfos)) {
			sqlSet += SqlScriptUtils.convertIf(fieldInfos.stream()
				.map(i -> i.getSqlSet(Constants.MP_FILL_ET + StringPool.DOT)).collect(joining(EMPTY)), String.format("%s != null", Constants.MP_FILL_ET), true);
		}
		if (logicDeleteWithFill) {
			String fillSql = logicDeleteField.getSqlSet(true, Constants.MP_FILL_ET + StringPool.DOT);
			fillSql = fillSql.substring(0, fillSql.length() - COMMA.length());
			String whenCondition = String.format("%s != null", Constants.MP_FILL_ET);
			sqlSet += SqlScriptUtils.convertChoose(whenCondition, fillSql, tableInfo.getLogicDeleteSql(false, false));
		} else {
			sqlSet += StringPool.EMPTY + tableInfo.getLogicDeleteSql(false, false);
		}

		// 拼接逻辑删除填充SQL
		sqlSet += "," + TableLogicFillUtils.sqlLogicFillSet(tableInfo);

		return sqlMethod.format(tableInfo.getTableName(), sqlSet, tableInfo.getKeyColumn(),
			getConvertForeachScript(tableInfo), tableInfo.getLogicDeleteSql(true, true));
	}
}
