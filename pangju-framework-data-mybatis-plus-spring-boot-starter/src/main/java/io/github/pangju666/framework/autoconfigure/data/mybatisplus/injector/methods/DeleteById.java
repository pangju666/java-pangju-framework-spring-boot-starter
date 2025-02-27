package io.github.pangju666.framework.autoconfigure.data.mybatisplus.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import io.github.pangju666.framework.autoconfigure.data.mybatisplus.utils.TableLogicFillUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

public class DeleteById extends AbstractMethod {
	public DeleteById() {
		this(SqlMethod.DELETE_BY_ID.getMethod());
	}

	public DeleteById(String name) {
		super(name);
	}

	@Override
	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
		String sql;
		SqlMethod sqlMethod = SqlMethod.LOGIC_DELETE_BY_ID;
		if (tableInfo.isWithLogicDelete()) {
			String sqlSet = TableLogicFillUtils.logicDeleteSetSql(tableInfo);
			sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), sqlSet,
				tableInfo.getKeyColumn(), tableInfo.getKeyProperty(),
				tableInfo.getLogicDeleteSql(true, true));
			SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
			return addUpdateMappedStatement(mapperClass, modelClass, methodName, sqlSource);
		} else {
			sqlMethod = SqlMethod.DELETE_BY_ID;
			sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), tableInfo.getKeyColumn(),
				tableInfo.getKeyProperty());
			SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
			return addDeleteMappedStatement(mapperClass, methodName, sqlSource);
		}
	}
}
