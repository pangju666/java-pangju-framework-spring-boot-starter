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

package io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.annotation;

import io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.processor.DynamicMongoBeanPostProcessor;
import io.github.pangju666.framework.data.mongodb.repository.BaseRepository;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.lang.annotation.*;

/**
 * 动态MongoDB数据源注解
 * <p>
 * 用于标注{@link BaseRepository MongoDB Repository}类，指定该Repository使用的MongoDB数据源。
 * 当应用配置了多个MongoDB数据源时，可以通过该注解为不同的Repository指定不同的数据源。
 * </p>
 * <p>
 * 注解作用：
 * <ul>
 *     <li>标注在Repository类上</li>
 *     <li>指定该Repository使用的MongoDB数据源名称</li>
 *     <li>由{@link DynamicMongoBeanPostProcessor}处理</li>
 *     <li>自动为Repository注入对应的{@link MongoTemplate}</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <p>
 * 当应用中存在多个MongoDB数据源（如主从部署、数据分片等），
 * 不同的业务模块需要访问不同的数据库时，可以使用该注解来区分。
 * </p>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 主数据源Repository（不需要标注，使用默认MongoTemplate）
 * &#64;Repository
 * public class UserRepository extends BaseRepository&lt;User, String&gt; {
 *     // 使用默认的MongoTemplate
 * }
 *
 * // 从数据源Repository（指定secondary数据源）
 * &#64;Repository
 * &#64;DynamicMongo("secondary")
 * public class UserSecondaryRepository extends BaseRepository&lt;User, String&gt; {
 *     // 使用secondary数据源的MongoTemplate
 * }
 *
 * // 另一个从数据源Repository（指定tertiary数据源）
 * &#64;Repository
 * &#64;DynamicMongo("tertiary")
 * public class LogTertiaryRepository extends BaseRepository&lt;Log, String&gt; {
 *     // 使用tertiary数据源的MongoTemplate
 * }
 * </pre>
 * </p>
 * <p>
 * 配置对应关系：
 * <p>
 * 注解中指定的数据源名称必须与{@code spring.data.mongodb.dynamic.databases}配置中的键名对应。
 * 框架会根据该名称查找对应的{@link MongoTemplate} Bean。
 * </p>
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *     <li>注解中指定的数据源名称必须在配置中存在，否则会抛出异常</li>
 *     <li>注解只能标注在类上，不能标注在方法或字段上</li>
 *     <li>只有{@link BaseRepository}的实现类才能被处理</li>
 *     <li>如果不标注该注解，Repository将使用主数据源的MongoTemplate</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see DynamicMongoBeanPostProcessor
 * @since 1.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicMongo {
	String value();
}
