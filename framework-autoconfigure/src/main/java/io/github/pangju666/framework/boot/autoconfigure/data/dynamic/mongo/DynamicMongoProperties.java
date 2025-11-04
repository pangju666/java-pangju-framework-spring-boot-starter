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

import io.github.pangju666.framework.boot.data.dynamic.mongo.utils.DynamicMongoUtils;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 动态MongoDB配置属性类
 * <p>
 * 用于配置多个MongoDB数据源。配置前缀为{@code spring.data.mongodb.dynamic}。
 * 通过该配置类可以在应用启动时指定多个MongoDB数据库连接，并选择主数据源。
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * spring:
 *   data:
 *     mongodb:
 *       dynamic:
 *         primary: mongodb-primary
 *         databases:
 *           mongodb-primary:
 *             uri: mongodb://user:password@localhost:27017/primary_db
 *             auto-index-creation: true
 *           mongodb-secondary:
 *             uri: mongodb://user:password@192.168.1.100:27017/secondary_db
 *             auto-index-creation: true
 *           mongodb-tertiary:
 *             host: 192.168.1.101
 *             port: 27017
 *             database: tertiary_db
 *             username: user
 *             password: password
 *             auto-index-creation: false
 * </pre>
 * </p>
 * <p>
 * 继承关系：
 * <p>
 * 该类使用的数据源配置{@link MongoProperties}是Spring Boot的标准MongoDB配置属性类，
 * 支持所有Spring Boot MongoDB的配置选项。
 * </p>
 * </p>
 * <p>
 * 生成的Bean：
 * <ul>
 *     <li>{name}MongoConnectionDetails - 每个数据源的连接详情</li>
 *     <li>{name}MongoClientSettings - 每个数据源的客户端设置</li>
 *     <li>{name}MongoMappingContext - 每个数据源的映射上下文</li>
 *     <li>{name}MongoCustomConversions - 每个数据源的自定义类型转换</li>
 *     <li>{name}MongoClient - 每个数据源的客户端实例</li>
 *     <li>{name}MongoDatabaseFactory - 每个数据源的数据库工厂</li>
 *     <li>{name}MongoConverter - 每个数据源的数据转换器</li>
 *     <li>{name}MongoTemplate - 每个数据源的操作模板</li>
 *     <li>{name}GridFsTemplate - 每个数据源的GridFS操作模板</li>
 *     <li>mongoTemplate - 主数据源的操作模板（标记为primary）</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 使用主数据源（自动注入）
 * &#64;Autowired
 * private MongoTemplate mongoTemplate;
 *
 * // 使用指定数据源
 * &#64;Autowired
 * &#64;Qualifier("db2MongoTemplate")
 * private MongoTemplate mongoTemplate2;
 * </pre>
 * </p>
 *
 * @author pangju666
 * @see org.springframework.boot.autoconfigure.mongo.MongoProperties
 * @see DynamicMongoAutoConfiguration
 * @see DynamicMongoRegistrar
 * @see DynamicMongoUtils
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = DynamicMongoProperties.PREFIX)
public class DynamicMongoProperties {
	/**
	 * 配置属性前缀
	 * <p>
	 * 对应Spring Boot配置文件中的前缀路径
	 * </p>
	 *
	 * @since 1.0.0
	 */
	public static final String PREFIX = "spring.data.mongodb.dynamic";

	/**
	 * 主MongoDB数据源名称
	 * <p>
	 * 该值必须对应{@link #databases}中的一个键。
	 * 主数据源的Bean将被标记为主Bean，在自动注入时默认使用。
	 * 如果应用中只使用一个MongoDB数据源，建议配置为该数据源的名称。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private String primary;
	/**
	 * MongoDB数据源配置集合
	 * <p>
	 * key为数据源名称（标识），value为该数据源的{@link MongoProperties}配置。
	 * 集合不可为空，至少需要配置一个数据源。
	 * 每个数据源的配置均继承自Spring Boot的MongoDB配置属性。
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
