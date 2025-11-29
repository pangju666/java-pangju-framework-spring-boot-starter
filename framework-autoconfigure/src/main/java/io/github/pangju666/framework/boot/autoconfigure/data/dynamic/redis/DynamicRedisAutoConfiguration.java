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

import io.github.pangju666.framework.boot.data.dynamic.redis.DynamicRedisUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisOperations;

/**
 * 动态 Redis 自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>启用 {@link DynamicRedisProperties} 属性绑定。</li>
 *   <li>导入 {@link DynamicRedisRegistrar}，在启动阶段动态注册多数据源相关 Bean。</li>
 *   <li>支持按数据源名称生成统一命名的 Bean，并为主数据源设置 {@code primary} 标志。</li>
 * </ul>
 *
 * <p><strong>条件</strong></p>
 * <ul>
 *   <li>类路径存在 {@link org.springframework.data.redis.core.RedisOperations}（{@link ConditionalOnClass}）。</li>
 *   <li>在 {@link RedisAutoConfiguration} 之前、{@link ClientResourcesAutoConfiguration} 之后执行（{@link AutoConfiguration}）。</li>
 * </ul>
 *
 * <p><strong>流程</strong>：启用属性绑定 -> 导入注册器 -> 解析配置并注册 Bean -> 标记主数据源 -> 完成装配。</p>
 *
 * <p><strong>生成的 Bean</strong>（每个数据源各一份）</p>
 * <ul>
 *   <li>{name}RedisConnectionDetails：连接详情。</li>
 *   <li>{name}RedisConnectionFactory：连接工厂。</li>
 *   <li>{name}RedisTemplate：对象键值模板。</li>
 *   <li>{name}StringRedisTemplate：字符串键值模板。</li>
 *   <li>{name}ScanRedisTemplate：支持游标扫描的模板。</li>
 *   <li>{name}StringScanRedisTemplate：支持游标扫描的字符串模板。</li>
 * </ul>
 *
 * <p><strong>使用示例</strong></p>
 * <pre>{@code
 * // 使用主数据源（自动注入）
 * @Autowired
 * private RedisTemplate<Object, Object> redisTemplate;
 *
 * // 指定数据源名称
 * @Autowired
 * @Qualifier("db2RedisTemplate")
 * private RedisTemplate<Object, Object> redisTemplate2;
 *
 * @Autowired
 * @Qualifier("db2StringRedisTemplate")
 * private StringRedisTemplate stringRedisTemplate2;
 * }
 * </pre>
 *
 * @author pangju666
 * @see DynamicRedisProperties
 * @see DynamicRedisRegistrar
 * @see DynamicRedisUtils
 * @since 1.0.0
 */
@AutoConfiguration(before = RedisAutoConfiguration.class, after = ClientResourcesAutoConfiguration.class)
@ConditionalOnClass(RedisOperations.class)
@EnableConfigurationProperties(DynamicRedisProperties.class)
@Import(DynamicRedisRegistrar.class)
public class DynamicRedisAutoConfiguration {
}
