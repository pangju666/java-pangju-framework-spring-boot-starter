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

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 动态 Redis Lettuce 客户端条件判断类。
 *
 * <p>用于判断是否需要启用 Lettuce 相关的自动配置。</p>
 *
 * <p><b>匹配逻辑</b></p>
 * <ul>
 *   <li>检查是否配置了动态 Redis 数据源。</li>
 *   <li>遍历所有已配置的数据源，只要有一个数据源使用 Lettuce 客户端（{@code client-type} 为 {@link RedisProperties.ClientType#LETTUCE} 或 null），即视为匹配。</li>
 * </ul>
 *
 * @author pangju666
 * @see DynamicRedisAutoConfiguration
 * @since 1.0.0
 */
public class OnDynamicRedisLettuceCondition extends SpringBootCondition {
    /**
     * 判断条件是否匹配。
     *
     * @param context  条件上下文
     * @param metadata 注解元数据
     * @return 匹配结果
     */
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Binder binder = Binder.get(context.getEnvironment());
        BindResult<DynamicRedisProperties> bindResult = binder.bind(DynamicRedisProperties.PREFIX,
            DynamicRedisProperties.class);

		if (bindResult.isBound()) {
			DynamicRedisProperties properties = bindResult.get();
			if (StringUtils.hasText(properties.getPrimary()) &&
				!CollectionUtils.isEmpty(properties.getDatabases()) &&
				properties.getDatabases().containsKey(properties.getPrimary())) {
				for (DynamicRedisProperties.RedisProperties value : properties.getDatabases().values()) {
					if (Objects.isNull(value.getClientType()) || value.getClientType() == RedisProperties.ClientType.LETTUCE) {
						return ConditionOutcome.match();
					}
				}
			}
        }
        return ConditionOutcome.noMatch("");
    }
}
