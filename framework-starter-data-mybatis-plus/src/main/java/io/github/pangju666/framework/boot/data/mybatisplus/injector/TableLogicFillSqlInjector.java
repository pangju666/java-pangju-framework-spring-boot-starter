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

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 逻辑删除字段填充SQL注入器
 * <p>
 * 继承自{@link DefaultSqlInjector}，重写了{@link DefaultSqlInjector#getMethodList getMethodList}方法，
 * 用于在逻辑删除时支持自定义字段填充功能。
 * 通过替换默认的{@link com.baomidou.mybatisplus.core.injector.methods.Delete Delete}、
 * {@link com.baomidou.mybatisplus.core.injector.methods.DeleteById DeleteById}和
 * {@link com.baomidou.mybatisplus.core.injector.methods.DeleteByIds DeleteByIds}方法实现。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class TableLogicFillSqlInjector extends DefaultSqlInjector {
	/**
	 * 获取SQL方法列表
	 * <p>
	 * 重写父类方法，添加自定义的Delete、DeleteById和DeleteByIds方法，
	 * 以支持逻辑删除时的字段自动填充功能。
	 * 注意：自定义的方法需要先于父类的方法注入，否则会导致注入失败。
	 * </p>
	 *
	 * @param configuration MyBatis配置对象
	 * @param mapperClass   Mapper接口类
	 * @param tableInfo     表信息对象
	 * @return 包含所有SQL方法的列表
	 * @since 1.0.0
	 */
	@Override
	public List<AbstractMethod> getMethodList(Configuration configuration, Class<?> mapperClass, TableInfo tableInfo) {
		List<AbstractMethod> oldMethods = super.getMethodList(configuration, mapperClass, tableInfo);
		boolean hasPk = tableInfo.havePK();
		// 重写的注入要先于父类的注入，否则会导致注入失败
		List<AbstractMethod> newMethods = new ArrayList<>(oldMethods.size() + (hasPk ? 3 : 1));
		newMethods.add(new Delete());
		if (hasPk) {
			newMethods.add(new DeleteByIds());
			newMethods.add(new DeleteById());
		}
		newMethods.addAll(oldMethods);
		return newMethods;
	}
}
