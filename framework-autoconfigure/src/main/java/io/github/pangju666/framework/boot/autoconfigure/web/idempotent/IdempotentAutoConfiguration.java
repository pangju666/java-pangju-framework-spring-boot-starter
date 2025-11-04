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

package io.github.pangju666.framework.boot.autoconfigure.web.idempotent;

import io.github.pangju666.framework.boot.autoconfigure.web.idempotent.config.ExpireMapRequestRepeaterConfiguration;
import io.github.pangju666.framework.boot.autoconfigure.web.idempotent.config.RedisRequestRepeaterConfiguration;
import io.github.pangju666.framework.boot.web.idempotent.aspect.IdempotentAspect;
import io.github.pangju666.framework.boot.web.idempotent.validator.IdempotentValidator;
import org.aspectj.weaver.Advice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * 幂等性功能的自动配置类。
 * <p>
 * 用于自动化配置幂等性校验相关的组件，适配不同的幂等性校验实现（如基于本地内存或 Redis），
 * 并注入相应的幂等校验切面 {@link IdempotentAspect}。
 * 配置生效需要满足以下条件：
 * <ul>
 *     <li>启用了 Spring AOP（默认开启）。</li>
 *     <li>类路径中存在 {@link Advice}。</li>
 *     <li>配置了必要的 {@link IdempotentValidator} 实现。</li>
 * </ul>
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *     <li>基于 {@code pangju.web.idempotent.type} 配置选择幂等校验实现（支持 {@code EXPIRE_MAP} 和 {@code REDIS}）。</li>
 *     <li>将幂等校验切面 {@link IdempotentAspect} 注册为 Spring Bean，用于拦截目标方法并实现幂等性校验逻辑。</li>
 *     <li>加载幂等性相关的配置属性 {@link IdempotentProperties}。</li>
 * </ul>
 *
 * <p>配置示例：</p>
 * <pre>
 * pangju.web.idempotent.type=REDIS
 * spring.aop.auto=true
 * </pre>
 *
 * <p>默认行为：</p>
 * <ul>
 *     <li>如果未配置 {@code pangju.web.idempotent.type}，默认为 {@code EXPIRE_MAP}（本地内存校验）。</li>
 *     <li>如果未禁用 Spring AOP（即 {@code spring.aop.auto=true} 或未配置），切面自动生效。</li>
 * </ul>
 *
 * @author pangju666
 * @see IdempotentAspect
 * @see IdempotentProperties
 * @see ExpireMapRequestRepeaterConfiguration
 * @see RedisRequestRepeaterConfiguration
 * @since 1.0.0
 */
@AutoConfiguration(after = AopAutoConfiguration.class)
@ConditionalOnBooleanProperty(name = "spring.aop.auto", matchIfMissing = true)
@ConditionalOnClass(Advice.class)
@Import({ExpireMapRequestRepeaterConfiguration.class, RedisRequestRepeaterConfiguration.class})
@EnableConfigurationProperties(IdempotentProperties.class)
public class IdempotentAutoConfiguration {
	/**
	 * 注册幂等性校验切面。
	 * <p>
	 * 当上下文中存在 {@link IdempotentValidator} 实例时，将自动注册 {@link IdempotentAspect}。
	 * </p>
	 *
	 * <p>校验器的主要功能：</p>
	 * <ul>
	 *     <li>动态校验请求是否为重复提交。</li>
	 *     <li>基于注解 {@link io.github.pangju666.framework.autoconfigure.web.idempotent.annotation.Idempotent} 的逻辑配置。</li>
	 * </ul>
	 *
	 * @param idempotentValidator 幂等性校验器接口的实现（如基于 Redis 或内存的实现）。
	 * @return {@link IdempotentAspect} 实例，用于 AOP 拦截目标方法并执行幂等性逻辑。
	 * @see IdempotentAspect
	 * @see IdempotentValidator
	 * @since 1.0.0
	 */
	@ConditionalOnBean(IdempotentValidator.class)
	@Bean
	public IdempotentAspect idempotentAspect(IdempotentValidator idempotentValidator) {
		return new IdempotentAspect(idempotentValidator);
	}
}
