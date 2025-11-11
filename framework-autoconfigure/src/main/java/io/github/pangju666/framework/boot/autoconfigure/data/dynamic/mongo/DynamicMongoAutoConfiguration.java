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

import com.mongodb.client.MongoClient;
import io.github.pangju666.framework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

/**
 * 动态MongoDB自动配置类
 * <p>
 * 在应用启动时自动配置多数据源的 MongoDB 能力；通过导入
 * {@link DynamicMongoRegistrar} 与 {@link DynamicMongoRepositoriesRegistrar}
 * 注册连接相关 Bean 与仓库支持。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>启用 {@link DynamicMongoProperties} 配置属性</li>
 *     <li>导入 {@link DynamicMongoRegistrar} 实现动态 Bean 注册</li>
 *     <li>导入 {@link DynamicMongoRepositoriesRegistrar} 启用仓库支持</li>
 *     <li>支持多个 MongoDB 数据源并自动选择主数据源</li>
 * </ul>
 * </p>
 * <p>
 * 生成的Bean（按数据源）：
 * <ul>
 *     <li>{name}MongoConnectionDetails - 连接详情</li>
 *     <li>{name}MongoClientSettings - 客户端设置</li>
 *     <li>{name}MongoMappingContext - 映射上下文</li>
 *     <li>{name}MongoClient - 客户端实例</li>
 *     <li>{name}MongoDatabaseFactory - 数据库工厂</li>
 *     <li>{name}MongoConverter - 数据转换器</li>
 *     <li>{name}MongoTemplate - 操作模板</li>
 *     <li>{name}GridFsTemplate - GridFS 操作模板</li>
 *     <li>mongoTemplate - 主数据源操作模板（标记为 primary）</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 使用主数据源（自动注入）
 * @Autowired
 * private MongoTemplate mongoTemplate;
 *
 * // 使用指定数据源
 * @Autowired
 * @Qualifier("db2MongoTemplate")
 * private MongoTemplate mongoTemplate2;
 * }</pre>
 * </p>
 *
 * @author pangju666
 * @see DynamicMongoProperties
 * @see DynamicMongoRegistrar
 * @see DynamicMongoRepositoriesRegistrar
 * @since 1.0.0
 */
@AutoConfiguration(before = {MongoDataAutoConfiguration.class, MongoRepositoriesAutoConfiguration.class})
@ConditionalOnClass({MongoClient.class, MongoTemplate.class, GridFsTemplate.class})
@EnableConfigurationProperties(DynamicMongoProperties.class)
@Import({DynamicMongoRegistrar.class, DynamicMongoRepositoriesRegistrar.class})
public class DynamicMongoAutoConfiguration {
}
