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
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * 自定义根据ID删除方法实现类
 * <p>
 * 继承自MyBatis-Plus的{@link com.baomidou.mybatisplus.core.injector.methods.DeleteById}，
 * 重写了根据ID删除的SQL生成逻辑，支持在逻辑删除时自动填充自定义字段。
 * </p>
 * <p>
 * 当表配置了逻辑删除时，除了设置逻辑删除字段外，还会通过
 * {@link TableLogicFillUtils#sqlLogicFillSet(TableInfo)}方法
 * 自动填充标注了{@link io.github.pangju666.framework.boot.data.mybatisplus.annotation.TableLogicFill}注解的字段。
 * </p>
 * <p>
 * 该方法会根据表字段配置自动生成不同的SQL语句：
 * <ul>
 *     <li>如果字段配置了更新填充注解，会生成对应的SET语句</li>
 *     <li>如果逻辑删除字段配置了更新填充注解，会生成条件判断的SET语句</li>
 *     <li>最后追加自定义逻辑删除填充字段的SET语句</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：在根据ID逻辑删除时需要同时更新其他字段（如删除时间、删除人等）
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
final class DeleteById extends com.baomidou.mybatisplus.core.injector.methods.DeleteById {
	public DeleteById() {
		super();
	}

	/**
	 * 重写注入MappedStatement方法
	 * <p>
	 * 根据表是否配置了逻辑删除，生成不同的SQL语句：
	 * <ul>
	 *     <li>如果配置了逻辑删除：
	 *     <ul>
	 *         <li>检查是否有配置了更新填充的字段（非逻辑删除字段）</li>
	 *         <li>检查逻辑删除字段是否配置了更新填充</li>
	 *         <li>生成包含所有填充字段的UPDATE语句</li>
	 *         <li>追加自定义逻辑删除填充字段的SET语句</li>
	 *     </ul>
	 *     </li>
	 *     <li>如果没有配置逻辑删除：生成标准的DELETE语句</li>
	 * </ul>
	 * </p>
	 *
	 * @param mapperClass Mapper接口类
	 * @param modelClass  实体类
	 * @param tableInfo   表信息
	 * @return 生成的MappedStatement对象
	 * @since 1.0.0
	 */
	@Override
	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
		// 从父类 copy 的代码
		String sql;
		if (tableInfo.isWithLogicDelete()) {
			List<TableFieldInfo> fieldInfos = tableInfo.getFieldList().stream()
				.filter(TableFieldInfo::isWithUpdateFill)
				.filter(f -> !f.isLogicDelete())
				.collect(toList());
			TableFieldInfo logicDeleteField = tableInfo.getLogicDeleteFieldInfo();
			boolean logicDeleteWithFill = logicDeleteField != null && logicDeleteField.isWithUpdateFill();
			if (CollectionUtils.isNotEmpty(fieldInfos) || logicDeleteWithFill) {
				String entityCondition = "@org.apache.ibatis.reflection.SystemMetaObject@forObject(_parameter).findProperty('" + tableInfo.getKeyProperty() + "', false) != null";
				String fillSetSql = fieldInfos.stream().map(i -> i.getSqlSet(EMPTY)).collect(joining(EMPTY));
				String sqlSet;
				if (logicDeleteWithFill) {
					String fillSql = logicDeleteField.getSqlSet(true, EMPTY);
					fillSql = fillSql.substring(0, fillSql.length() - COMMA.length());
					String logicDeleteChoose = SqlScriptUtils.convertChoose(entityCondition, fillSql, tableInfo.getLogicDeleteSql(false, false));
					if (CollectionUtils.isNotEmpty(fieldInfos)) {
						sqlSet = "SET " + SqlScriptUtils.convertIf(fillSetSql, entityCondition, true) + logicDeleteChoose;
					} else {
						sqlSet = "SET " + logicDeleteChoose;
					}
				} else {
					sqlSet = "SET " + SqlScriptUtils.convertIf(fillSetSql, entityCondition, true)
						+ tableInfo.getLogicDeleteSql(false, false);
				}

				// 拼接逻辑删除填充SQL
				sqlSet += "," + TableLogicFillUtils.sqlLogicFillSet(tableInfo);

				sql = SqlMethod.LOGIC_DELETE_BY_ID.format(tableInfo.getTableName(), sqlSet, tableInfo.getKeyColumn(),
					tableInfo.getKeyProperty(), tableInfo.getLogicDeleteSql(true, true));
			} else {
				sql = SqlMethod.LOGIC_DELETE_BY_ID.format(tableInfo.getTableName(), sqlLogicSet(tableInfo),
					tableInfo.getKeyColumn(), tableInfo.getKeyProperty(),
					tableInfo.getLogicDeleteSql(true, true));
			}
			SqlSource sqlSource = super.createSqlSource(configuration, sql, Object.class);
			return addUpdateMappedStatement(mapperClass, modelClass, methodName, sqlSource);
		} else {
			return super.injectMappedStatement(mapperClass, modelClass, tableInfo);
		}
	}
}
