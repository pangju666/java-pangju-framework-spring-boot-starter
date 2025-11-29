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
import io.github.pangju666.framework.data.mongodb.repository.SimpleBaseMongoRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.ConditionalOnRepositoryType;
import org.springframework.boot.autoconfigure.data.RepositoryType;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.MongoRepositoryConfigurationExtension;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;

/**
 * 动态 Mongo 仓库自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>在启用动态 Mongo 条件下，注册基于 Imperative 风格的 {@link MongoRepository} 仓库支持。</li>
 *   <li>导入 {@link DynamicMongoRepositoriesRegistrar}，按数据源生成仓库相关 Bean 定义。</li>
 * </ul>
 *
 * <p><strong>条件</strong></p>
 * <ul>
 *   <li>类路径存在 {@link MongoClient}、{@link MongoRepository} 与 {@link io.github.pangju666.framework.data.mongodb.repository.SimpleBaseMongoRepository}。</li>
 *   <li>容器中不存在 {@link MongoRepositoryFactoryBean}、{@link MongoRepositoryConfigurationExtension}。</li>
 *   <li>仓库类型为 {@code mongodb} 且风格为 {@link org.springframework.boot.autoconfigure.data.RepositoryType#IMPERATIVE}。</li>
 *   <li>满足 {@link OnDynamicMongoCondition} 条件（动态数据源配置有效）。</li>
 * </ul>
 *
 * <p><strong>顺序</strong></p>
 * <ul>
 *   <li>在 {@link MongoRepositoriesAutoConfiguration} 之前、{@link DynamicMongoAutoConfiguration} 之后执行。</li>
 * </ul>
 *
 * <p><strong>使用示例</strong></p>
 * <pre>{@code
 * @DynamicMongo("db1") // 指定数据源名称
 * @Repository
 * public interface UserRepository extends BaseMongoRepository<UserDocument, String> {
 * }
 * }
 * </pre>
 * <p>其中 {@code db1} 为数据源名称，需要与 {@code spring.data.mongodb.dynamic.databases} 中的键一致。</p>
 *
 * @author pangju666
 * @see DynamicMongoRepositoriesRegistrar
 * @see OnDynamicMongoCondition
 * @see MongoRepositoriesAutoConfiguration
 * @since 1.0.0
 */
@AutoConfiguration(before = MongoRepositoriesAutoConfiguration.class, after = DynamicMongoAutoConfiguration.class)
@ConditionalOnClass({ MongoClient.class, MongoRepository.class, SimpleBaseMongoRepository.class })
@ConditionalOnMissingBean({ MongoRepositoryFactoryBean.class, MongoRepositoryConfigurationExtension.class })
@ConditionalOnRepositoryType(store = "mongodb", type = RepositoryType.IMPERATIVE)
@Conditional(OnDynamicMongoCondition.class)
@Import(DynamicMongoRepositoriesRegistrar.class)
public class DynamicMongoRepositoriesAutoConfiguration {
}
