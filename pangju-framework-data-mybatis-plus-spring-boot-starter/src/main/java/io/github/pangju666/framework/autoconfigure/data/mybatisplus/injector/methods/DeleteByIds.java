package io.github.pangju666.framework.autoconfigure.data.mybatisplus.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import io.github.pangju666.framework.autoconfigure.data.mybatisplus.utils.TableLogicFillUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

public class DeleteByIds extends AbstractMethod {
	public DeleteByIds() {
		this(SqlMethod.DELETE_BY_IDS.getMethod());
	}

	public DeleteByIds(String name) {
		super(name);
	}

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
