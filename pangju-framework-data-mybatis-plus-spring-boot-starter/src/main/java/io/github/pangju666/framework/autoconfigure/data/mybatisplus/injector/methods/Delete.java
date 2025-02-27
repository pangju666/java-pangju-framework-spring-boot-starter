package io.github.pangju666.framework.autoconfigure.data.mybatisplus.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import io.github.pangju666.framework.autoconfigure.data.mybatisplus.utils.TableLogicFillUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

public class Delete extends AbstractMethod {
	public Delete() {
		this(SqlMethod.DELETE.getMethod());
	}

	public Delete(String name) {
		super(name);
	}

	@Override
	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
		String sql;
		SqlMethod sqlMethod = SqlMethod.LOGIC_DELETE;
		if (tableInfo.isWithLogicDelete()) {
			String sqlSet = TableLogicFillUtils.logicDeleteSetSql(tableInfo);
			sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), sqlSet,
				sqlWhereEntityWrapper(true, tableInfo), sqlComment());
			SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
			return this.addUpdateMappedStatement(mapperClass, modelClass, methodName, sqlSource);
		} else {
			sqlMethod = SqlMethod.DELETE;
			sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(),
				sqlWhereEntityWrapper(true, tableInfo),
				sqlComment());
			SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
			return this.addDeleteMappedStatement(mapperClass, methodName, sqlSource);
		}
	}
}
