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

package io.github.pangju666.framework.boot.autoconfigure.web.idempotent.config;

import io.github.pangju666.framework.boot.web.idempotent.validator.IdempotentValidator;
import io.github.pangju666.framework.boot.web.idempotent.validator.impl.ExpireMapIdempotentValidator;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 基于本地内存的幂等校验自动配置
 * <p>
 * 在选择 {@code EXPIRE_MAP} 实现时自动注册 {@link ExpireMapIdempotentValidator}，
 * 基于本地内存的 {@link ExpiringMap} 提供轻量幂等校验。
 * </p>
 * <p><b>生效条件</b></p>
 * <ul>
 *   <li>类路径存在 {@link ExpiringMap}（{@link ConditionalOnClass}）。</li>
 *   <li>属性 {@code pangju.web.idempotent.type} 为 {@code EXPIRE_MAP} 或未设置（{@link ConditionalOnProperty} 且 {@code matchIfMissing=true}）。</li>
 *   <li>上下文中不存在 {@link IdempotentValidator} Bean（{@link ConditionalOnMissingBean}）。</li>
 * </ul>
 * <p><b>行为说明</b></p>
 * <ul>
 *   <li>注册单个 {@link ExpireMapIdempotentValidator} Bean，供幂等验证使用。</li>
 *   <li>默认适用于单节点环境；分布式场景请改用 {@code REDIS} 实现。</li>
 * </ul>
 * <p><b>注意事项</b></p>
 * <ul>
 *   <li>并发语义详见验证器类 Javadoc；本地实现不保证跨进程强一致性。</li>
 *   <li>选择类型参考 {@link io.github.pangju666.framework.boot.autoconfigure.web.idempotent.IdempotentProperties.Type#EXPIRE_MAP}。</li>
 * </ul>
 * <p><b>配置示例（YAML）</b></p>
 * <pre>
 * {@code
 * pangju:
 *   web:
 *     idempotent:
 *       type: EXPIRE_MAP
 * }
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
	 * 注册基于本地内存的幂等验证器。
	 * <p>
	 * 仅在上下文缺少 {@link IdempotentValidator} Bean 时生效，避免重复注册。
	 * </p>
	 *
	 * @return {@link ExpireMapIdempotentValidator} 实例
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(IdempotentValidator.class)
	@Bean
	public ExpireMapIdempotentValidator expireMapIdempotentValidator() {
		return new ExpireMapIdempotentValidator();
	}
}
