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

import io.github.pangju666.framework.boot.web.idempotent.aspect.IdempotentAspect;
import io.github.pangju666.framework.boot.web.idempotent.validator.IdempotentValidator;
import io.github.pangju666.framework.spring.utils.SpELUtils;
import io.github.pangju666.framework.web.exception.base.BaseHttpException;
import io.github.pangju666.framework.web.model.Result;
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
 * 幂等性自动配置。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>根据配置自动装配幂等性校验组件（本地内存或 Redis）。</li>
 *   <li>注册切面 {@link IdempotentAspect} 以拦截业务方法。</li>
 * </ul>
 *
 * <p><b>激活条件</b></p>
 * <ul>
 *   <li>启用 Spring AOP（配置项 {@code spring.aop.auto}，默认开启）。</li>
 *   <li>类路径存在 {@link Advice}。</li>
 *   <li>应用上下文可创建或已存在 {@link IdempotentValidator} 实现。</li>
 * </ul>
 *
 * <p><b>行为</b></p>
 * <ul>
 *   <li>读取 {@link IdempotentProperties} 并按 {@code pangju.web.idempotent.type} 选择实现（{@code EXPIRE_MAP} 或 {@code REDIS}）。</li>
 *   <li>导入 {@link ExpireMapRequestRepeaterConfiguration} 与 {@link RedisRequestRepeaterConfiguration}，由各自条件决定是否生效。</li>
 *   <li>存在 {@link IdempotentValidator} 时注册 {@link IdempotentAspect}，拦截带有 {@code @Idempotent} 的方法并执行校验。</li>
 * </ul>
 *
 * <p><b>注意事项</b></p>
 * <ul>
 *   <li>未配置 {@code pangju.web.idempotent.type} 时，默认使用本地内存（{@code EXPIRE_MAP}）。</li>
 *   <li>禁用 AOP（{@code spring.aop.auto=false}）将使切面不生效，但属性与配置类仍可加载。</li>
 * </ul>
 *
 * <p><b>示例（YAML）</b></p>
 * <pre>
 * pangju:
 *   web:
 *     idempotent:
 *       type: REDIS
 * spring:
 *   aop:
 *     auto: true
 * </pre>
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
@ConditionalOnClass({Advice.class, Result.class, BaseHttpException.class})
@Import({ExpireMapRequestRepeaterConfiguration.class, RedisRequestRepeaterConfiguration.class})
@EnableConfigurationProperties(IdempotentProperties.class)
public class IdempotentAutoConfiguration {
	/**
	 * 注册幂等性校验切面。
	 *
	 * <p><b>激活</b></p>
	 * <ul>
	 *   <li>当上下文存在 {@link IdempotentValidator} Bean 时注册。</li>
	 * </ul>
	 *
	 * <p><b>行为</b></p>
	 * <ul>
	 *   <li>拦截标注 {@link io.github.pangju666.framework.boot.web.idempotent.annotation.Idempotent} 的方法，委托校验器执行幂等校验。</li>
	 * </ul>
	 *
	 * @param idempotentValidator 幂等校验器实现（如内存/Redis）。
	 * @return {@link IdempotentAspect}，用于 AOP 拦截并执行业务幂等校验。
	 * @see IdempotentAspect
	 * @see IdempotentValidator
	 * @since 1.0.0
	 */
	@ConditionalOnClass(SpELUtils.class)
	@ConditionalOnBean(IdempotentValidator.class)
	@Bean
	public IdempotentAspect idempotentAspect(IdempotentValidator idempotentValidator) {
		return new IdempotentAspect(idempotentValidator);
	}
}
