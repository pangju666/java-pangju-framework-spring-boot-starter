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
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * 自定义根据ID批量删除方法实现类
 * <p>
 * 重写了MyBatis-Plus的根据ID批量删除方法，支持在逻辑删除时进行字段自动填充。
 * 当表配置了逻辑删除时，会使用{@link TableLogicFillUtils#logicDeleteSetSql(TableInfo)}
 * 生成包含自定义填充字段的SQL语句。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
final class DeleteByIds extends AbstractMethod {
	public DeleteByIds() {
		this(SqlMethod.DELETE_BY_IDS.getMethod());
	}

	public DeleteByIds(String name) {
		super(name);
	}

	/**
	 * 注入{@link MappedStatement}
	 * <p>
	 * 根据表是否配置了逻辑删除，生成不同的SQL语句：
	 * <ul>
	 *     <li>如果配置了逻辑删除，则生成带有自定义字段填充的UPDATE语句</li>
	 *     <li>如果没有配置逻辑删除，则生成标准的DELETE语句</li>
	 * </ul>
	 * 使用SqlScriptUtils.convertForeach生成批量处理的SQL片段
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
		String sql;
		SqlMethod sqlMethod = SqlMethod.LOGIC_DELETE_BY_IDS;
		if (tableInfo.isWithLogicDelete()) {
			String sqlSet = TableLogicFillUtils.logicDeleteSetSql(tableInfo);
			sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), sqlSet, tableInfo.getKeyColumn(),
				SqlScriptUtils.convertForeach("#{item}", COLL, null, "item", COMMA),
				tableInfo.getLogicDeleteSql(true, true));
			SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, Object.class);
			return addUpdateMappedStatement(mapperClass, modelClass, methodName, sqlSource);
		} else {
			sqlMethod = SqlMethod.DELETE_BY_IDS;
			sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), tableInfo.getKeyColumn(),
				SqlScriptUtils.convertForeach("#{item}", COLL, null, "item", COMMA));
			SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, Object.class);
			return this.addDeleteMappedStatement(mapperClass, methodName, sqlSource);
		}
	}
}
