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

package io.github.pangju666.framework.boot.autoconfigure.data.mongo;

import com.mongodb.client.MongoClient;
import io.github.pangju666.framework.data.mongodb.repository.SimpleBaseMongoRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.ConditionalOnRepositoryType;
import org.springframework.boot.autoconfigure.data.RepositoryType;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.MongoRepositoryConfigurationExtension;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;

/**
 * Mongo 仓库自动配置
 * <p>
 * 在满足依赖与仓库类型条件时，优先于 Spring Boot 默认的
 * {@link org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration}
 * 启用 Spring Data Mongo 仓库支持，并通过 {@link MongoRepositoriesRegistrar}
 * 注册仓库扫描与配置扩展。
 * </p>
 * <p>
 * 生效条件：
 * <ul>
 *     <li>类路径存在 {@link com.mongodb.client.MongoClient} 与 {@link org.springframework.data.mongodb.repository.MongoRepository}</li>
 *     <li>上下文中不存在 {@link MongoRepositoryFactoryBean} 或
 *     {@link org.springframework.data.mongodb.repository.config.MongoRepositoryConfigurationExtension}</li>
 *     <li>仓库类型为 {@code mongodb} 且为命令式（{@link org.springframework.boot.autoconfigure.data.RepositoryType#IMPERATIVE}）</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * @Repository
 * public interface UserRepository extends BaseMongoRepository<UserDocument, String> {
 * }
 * }
 * </pre>
 * </p>
 *
 * @author pangju666
 * @see MongoRepositoriesRegistrar
 * @see org.springframework.data.mongodb.repository.MongoRepository
 * @see org.springframework.data.mongodb.repository.config.MongoRepositoryConfigurationExtension
 * @see MongoRepositoryFactoryBean
 * @since 1.0.0
 */
@AutoConfiguration(before = org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration.class)
@ConditionalOnClass({ MongoClient.class, MongoRepository.class, SimpleBaseMongoRepository.class })
@ConditionalOnMissingBean({ MongoRepositoryFactoryBean.class, MongoRepositoryConfigurationExtension.class })
@ConditionalOnRepositoryType(store = "mongodb", type = RepositoryType.IMPERATIVE)
@Import(MongoRepositoriesRegistrar.class)
public class MongoRepositoriesAutoConfiguration {
}
