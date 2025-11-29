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

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 动态 Mongo 数据源条件判断。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>用于在自动配置中判断是否存在有效的动态 Mongo 数据源配置。</li>
 *   <li>当满足条件时返回匹配结果，从而启用后续自动配置或注册逻辑。</li>
 * </ul>
 *
 * <p><strong>判定规则</strong></p>
 * <ul>
 *   <li>已成功绑定前缀为 {@link DynamicMongoProperties#PREFIX} 的属性；</li>
 *   <li>配置的主数据源 {@code primary} 非空；</li>
 *   <li>数据源集合 {@code databases} 非空且包含 {@code primary} 键。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class OnDynamicMongoCondition extends SpringBootCondition {
    /**
     * 计算条件匹配结果。
     *
     * <p><b>流程</b>：获取 {@link Binder} -> 绑定 {@link DynamicMongoProperties} -> 已绑定且校验通过则返回
     * {@link ConditionOutcome#match()} -> 否则返回 {@link ConditionOutcome#noMatch(String)}。</p>
     * <p><b>约束</b>：判定依赖于配置是否完整（主库与数据源集合一致）；当未绑定或配置不完整时视为不匹配。</p>
     *
     * @param context 条件上下文
     * @param metadata 注解元数据
     * @return 条件匹配结果
     */
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Binder binder = Binder.get(context.getEnvironment());
        BindResult<DynamicMongoProperties> bindResult = binder.bind(DynamicMongoProperties.PREFIX,
            DynamicMongoProperties.class);

		if (bindResult.isBound()) {
			DynamicMongoProperties properties = bindResult.get();
			if (StringUtils.hasText(properties.getPrimary()) &&
				!CollectionUtils.isEmpty(properties.getDatabases()) &&
				properties.getDatabases().containsKey(properties.getPrimary())) {
				return ConditionOutcome.match();
			}
        }
        return ConditionOutcome.noMatch("");
    }
}
