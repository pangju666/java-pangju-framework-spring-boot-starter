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

package io.github.pangju666.framework.autoconfigure.web.idempotent.config;

import io.github.pangju666.framework.autoconfigure.web.idempotent.validator.IdempotentValidator;
import io.github.pangju666.framework.autoconfigure.web.idempotent.validator.impl.ExpireMapIdempotentValidator;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 基于本地内存过期映射的幂等性校验配置类。
 * <p>
 * 当幂等性校验的类型为 {@code EXPIRE_MAP} 时（默认类型），将自动配置用于幂等性校验的
 * {@link ExpireMapIdempotentValidator} 实例，该实现基于本地内存提供幂等性支持。
 * </p>
 *
 * <p>配置特性：</p>
 * <ul>
 *     <li>仅当 {@code pangju.web.idempotent.type} 配置为 {@code EXPIRE_MAP} 或未设置时生效。</li>
 *     <li>避免重复注册：当上下文中不存在 {@link IdempotentValidator} Bean 时才会注册。</li>
 * </ul>
 *
 * <p>适用场景：</p>
 * <ul>
 *     <li>适用于单节点场景：本地内存校验方式更轻量，但不支持分布式环境。</li>
 *     <li>无需外部依赖的幂等性校验需求。</li>
 * </ul>
 *
 * <p>配置示例：</p>
 * <pre>
 * pangju.web.idempotent.type=EXPIRE_MAP
 * </pre>
 *
 * @author pangju666
 * @see ExpireMapIdempotentValidator
 * @see IdempotentValidator
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ExpiringMap.class)
@ConditionalOnProperty(prefix = "pangju.web.idempotent", value = "type", havingValue = "EXPIRE_MAP", matchIfMissing = true)
public class ExpireMapRequestRepeaterConfiguration {
	/**
	 * 创建基于本地内存的幂等性校验器。
	 * <p>
	 * 当上下文中没有已定义的 {@link IdempotentValidator} Bean 时，自动注册
	 * {@link ExpireMapIdempotentValidator} 实例。
	 * </p>
	 *
	 * @return {@link ExpireMapIdempotentValidator} 实例，用于幂等性校验。
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(IdempotentValidator.class)
	@Bean
	public ExpireMapIdempotentValidator expireMapIdempotentValidator() {
		return new ExpireMapIdempotentValidator();
	}
}
