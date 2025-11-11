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

package io.github.pangju666.framework.boot.data.dynamic.mongo.annotation;

import java.lang.annotation.*;

/**
 * 动态 Mongo 数据源标识注解
 * <p>
 * 标注在 Spring Data 仓库接口上，指定该仓库使用的 MongoDB 数据源名称。
 * 名称将用于解析对应的 {@code MongoTemplate}，并由工厂类根据该数据源
 * 创建仓库实例；未标注时回退到默认主数据源。
 * </p>
 *
 * @author pangju666
 * @see io.github.pangju666.framework.boot.data.dynamic.mongo.DynamicMongoRepositoryFactoryBean
 * @since 1.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicMongo {
	/**
	 * 数据源名称标识
	 *
	 * @return 配置的 MongoDB 数据源名称
	 * @since 1.0.0
	 */
	String value();
}
