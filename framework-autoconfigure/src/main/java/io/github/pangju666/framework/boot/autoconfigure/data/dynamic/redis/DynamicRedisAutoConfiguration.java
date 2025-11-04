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

package io.github.pangju666.framework.boot.autoconfigure.data.dynamic.redis;

import io.github.pangju666.framework.boot.data.dynamic.redis.utils.DynamicRedisUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisOperations;

/**
 * 动态Redis自动配置类
 * <p>
 * 该类用于在Spring Boot应用启动时自动配置多数据源Redis功能。
 * 通过导入{@link DynamicRedisRegistrar}，实现动态注册多个Redis连接相关的Bean。
 * </p>
 * <p>
 * 配置优先级：
 * <ul>
 *     <li>在{@code org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration}之前加载</li>
 *     <li>在{@code ClientResourcesAutoConfiguration}之后加载</li>
 * </ul>
 * </p>
 * <p>
 * 配置条件：
 * <ul>
 *     <li>Classpath中必须存在{@link RedisOperations}类（即Spring Data Redis库）</li>
 * </ul>
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>启用{@link DynamicRedisProperties}配置属性的支持</li>
 *     <li>导入{@link DynamicRedisRegistrar}以实现动态Bean注册</li>
 *     <li>支持多个Redis数据源的配置和管理</li>
 *     <li>自动选择主数据源</li>
 * </ul>
 * </p>
 * <p>
 * <p>
 * 生成的Bean：
 * <ul>
 *     <li>{name}RedisConnectionDetails - 每个数据源的连接详情</li>
 *     <li>{name}RedisConnectionFactory - 每个数据源的连接工厂</li>
 *     <li>{name}RedisTemplate - 每个数据源的模板</li>
 *     <li>redisTemplate - 主数据源的模板（标记为primary）</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 使用主数据源（自动注入）
 * &#64;Autowired
 * private RedisTemplate&lt;Object, Object&gt; redisTemplate;
 *
 * &#64;Autowired
 * &#64;Qualifier("db2RedisTemplate")
 * private RedisTemplate&lt;Object, Object&gt; redisTemplate2;
 * </pre>
 * </p>
 *
 * @author pangju666
 * @see DynamicRedisProperties
 * @see DynamicRedisRegistrar
 * @see DynamicRedisUtils
 * @see ClientResourcesAutoConfiguration
 * @see org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
 * @since 1.0.0
 */
@AutoConfiguration(before = RedisAutoConfiguration.class, after = ClientResourcesAutoConfiguration.class)
@ConditionalOnClass(RedisOperations.class)
@EnableConfigurationProperties(DynamicRedisProperties.class)
@Import(DynamicRedisRegistrar.class)
public class DynamicRedisAutoConfiguration {
}
