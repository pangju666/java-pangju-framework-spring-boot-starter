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

package io.github.pangju666.framework.boot.autoconfigure.data.dynamic.mongo;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 动态 MongoDB 配置属性类。
 * <p>
 * 用于配置多个 MongoDB 数据源，配置前缀为 {@code spring.data.mongodb.dynamic}。
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * spring:
 *   data:
 *     mongodb:
 *       dynamic:
 *         primary: mongo1
 *         databases:
 *           mongo1:
 *             host: 127.0.0.1
 *             port: 27017
 *             database: xxx
 *           mongo2:
 *             host: 127.0.0.1
 *             port: 27017
 *             database: xxx
 * </pre>
 * </p>
 *
 * @author pangju666
 * @see org.springframework.boot.autoconfigure.mongo.MongoProperties
 * @see DynamicMongoAutoConfiguration
 * @see DynamicMongoRegistrar
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = DynamicMongoProperties.PREFIX)
public class DynamicMongoProperties {
	/**
	 * 配置属性前缀
	 *
	 * @since 1.0.0
	 */
	public static final String PREFIX = "spring.data.mongodb.dynamic";

	/**
	 * 主 MongoDB 数据源名称
	 * <p>
	 * 必须对应 {@link #databases} 中的一个键；主数据源的 Bean
	 * 将被标记为主 Bean，在自动注入时默认使用。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private String primary;
	/**
	 * MongoDB 数据源配置集合
	 * <p>
	 * key 为数据源名称（标识），value 为该数据源的 {@link MongoProperties} 配置。
	 * 集合不可为空，至少需要配置一个数据源；每个数据源配置继承 Spring Boot 标准属性。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private Map<String, MongoProperties> databases;

	public Map<String, MongoProperties> getDatabases() {
		return databases;
	}

	public void setDatabases(Map<String, MongoProperties> databases) {
		this.databases = databases;
	}

	public String getPrimary() {
		return primary;
	}

	public void setPrimary(String primary) {
		this.primary = primary;
	}
}
