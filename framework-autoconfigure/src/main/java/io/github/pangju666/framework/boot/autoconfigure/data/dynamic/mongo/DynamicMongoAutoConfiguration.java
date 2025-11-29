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
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

/**
 * 动态 Mongo 自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>启用 {@link DynamicMongoProperties} 属性绑定。</li>
 *   <li>导入 {@link DynamicMongoRegistrar}，在启动阶段按数据源动态注册连接相关 Bean。</li>
 *   <li>支持多个 MongoDB 数据源并为主数据源标记 {@code primary}。</li>
 * </ul>
 *
 * <p><strong>条件</strong></p>
 * <ul>
 *   <li>类路径存在 {@link MongoClient}、{@link MongoTemplate}、{@link GridFsTemplate}（{@link ConditionalOnClass}）。</li>
 *   <li>在 {@link MongoDataAutoConfiguration} 之前执行（{@link AutoConfiguration}）。</li>
 * </ul>
 *
 * <p><strong>流程</strong>：启用属性绑定 -> 导入注册器 -> 解析配置并注册 Bean -> 标记主数据源 -> 完成装配。</p>
 *
 * <p><strong>生成的 Bean</strong>（按数据源）</p>
 * <ul>
 *   <li>{name}MongoConnectionDetails：连接详情。</li>
 *   <li>{name}MongoClientSettings：客户端设置。</li>
 *   <li>{name}MongoMappingContext：映射上下文。</li>
 *   <li>{name}MongoClient：客户端实例。</li>
 *   <li>{name}MongoDatabaseFactory：数据库工厂。</li>
 *   <li>{name}MongoConverter：数据转换器。</li>
 *   <li>{name}MongoTemplate：操作模板。</li>
 *   <li>{name}GridFsTemplate：GridFS 操作模板。</li>
 *   <li>mongoTemplate：主数据源操作模板（标记为 {@code primary}）。</li>
 * </ul>
 *
 * <p><strong>使用示例</strong></p>
 * <pre>{@code
 * // 使用主数据源（自动注入）
 * @Autowired
 * private MongoTemplate mongoTemplate;
 *
 * // 使用指定数据源
 * @Autowired
 * @Qualifier("db2MongoTemplate")
 * private MongoTemplate mongoTemplate2;
 * }
 * </pre>
 *
 * @author pangju666
 * @see DynamicMongoProperties
 * @see DynamicMongoRegistrar
 * @since 1.0.0
 */
@AutoConfiguration(before = MongoAutoConfiguration.class)
@ConditionalOnClass({MongoClient.class, MongoTemplate.class, GridFsTemplate.class})
@EnableConfigurationProperties(DynamicMongoProperties.class)
@Import(DynamicMongoRegistrar.class)
public class DynamicMongoAutoConfiguration {
}
