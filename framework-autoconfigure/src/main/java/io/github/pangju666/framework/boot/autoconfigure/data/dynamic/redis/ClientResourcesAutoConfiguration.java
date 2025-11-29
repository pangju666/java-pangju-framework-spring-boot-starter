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

import io.lettuce.core.RedisClient;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Lettuce 客户端资源自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>为 Lettuce 注册 {@link ClientResources} Bean，用于配置线程、度量与事件等客户端资源。</li>
 *   <li>支持通过 {@link org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer} 定制构建器。</li>
 * </ul>
 *
 * <p><strong>条件</strong></p>
 * <ul>
 *   <li>类路径存在 {@link io.lettuce.core.RedisClient}（{@link ConditionalOnClass}）。</li>
 *   <li>属性 {@code spring.data.redis.client-type=lettuce} 或未显式配置（{@link ConditionalOnProperty}）。</li>
 *   <li>容器中不存在 {@link ClientResources} Bean（{@link ConditionalOnMissingBean}）。</li>
 * </ul>
 *
 * <p><strong>来源</strong></p>
 * <p>参考 Spring Boot 的实现，并结合动态 Redis 的属性存在性进行装配。</p>
 *
 * @author pangju666
 * @see ClientResources
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnClass(RedisClient.class)
@ConditionalOnProperty(name = "spring.data.redis.client-type", havingValue = "lettuce", matchIfMissing = true)
public class ClientResourcesAutoConfiguration {
	/**
	 * 创建 Lettuce {@link ClientResources} Bean。
	 *
	 * <p><b>流程</b>：检查动态 Redis 配置是否有效（存在主库且定义在集合中）-> 无效则返回 {@code null}（交由 Spring 默认配置）->
	 * 构造 {@link DefaultClientResources.Builder} -> 应用所有 {@link ClientResourcesBuilderCustomizer} 定制 -> 构建并返回。</p>
	 * <p><b>约束</b>：受条件注解控制，仅在符合 Lettuce 客户端条件且容器中无现有 {@link ClientResources} Bean 时生效；
	 * 当未配置动态数据源或配置不完整时，返回 {@code null} 以保持与 Spring Boot 默认行为一致。</p>
	 *
	 * @param customizers 客户端资源构建器定制器集合
	 * @param properties 动态 Redis 配置属性
	 * @return 构建好的 {@link DefaultClientResources}；或在配置无效时返回 {@code null}
	 * @since 1.0.0
	 */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(ClientResources.class)
    public DefaultClientResources lettuceClientResources(ObjectProvider<ClientResourcesBuilderCustomizer> customizers,
                                                         DynamicRedisProperties properties) {
        // 如果不存在动态数据源配置则不注入，走 Spring 默认流程
        if (!StringUtils.hasText(properties.getPrimary()) || CollectionUtils.isEmpty(properties.getDatabases()) ||
            !properties.getDatabases().containsKey(properties.getPrimary())) {
            return null;
        }

        DefaultClientResources.Builder builder = DefaultClientResources.builder();
        customizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
        return builder.build();
    }
}
