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

package io.github.pangju666.framework.boot.web.aspect;

import io.github.pangju666.framework.boot.web.annotation.Idempotent;
import io.github.pangju666.framework.boot.web.exception.IdempotentException;
import io.github.pangju666.framework.boot.web.idempotent.IdempotentValidator;
import io.github.pangju666.framework.spring.utils.SpELUtils;
import io.github.pangju666.framework.web.exception.base.ServerException;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 幂等性校验的切面类。
 * <p>
 * 该类基于 Spring AOP 和 {@link Idempotent} 注解，实现了请求的幂等性校验逻辑。
 * 在方法执行前检查是否为重复请求，如果是，则会阻止方法的执行并抛出异常。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *     <li>拦截带有 {@link Idempotent} 注解的方法。</li>
 *     <li>从注解配置中动态解析 SpEL 表达式生成幂等性校验的唯一键。</li>
 *     <li>通过 {@link IdempotentValidator} 验证请求是否为重复提交。</li>
 *     <li>在校验失败或发生异常时移除幂等记录并抛出异常。</li>
 * </ul>
 *
 * <p>适用场景：</p>
 * <ul>
 *     <li>需要避免重复提交的接口，如订单提交、支付操作等。</li>
 *     <li>需要在分布式系统中保证接口的幂等性。</li>
 * </ul>
 *
 * @author pangju666
 * @see Idempotent
 * @see IdempotentValidator
 * @see IdempotentException
 * @see Aspect
 * @since 1.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Aspect
public class IdempotentAspect {
	/**
	 * 用于解析 SpEL 表达式的解析器。
	 * <p>
	 * 该字段用于动态解析 {@link Idempotent} 注解中的 SpEL 表达式，
	 * 生成幂等性校验所需的唯一键。
	 * </p>
	 *
	 * @see SpelExpressionParser
	 * @since 1.0.0
	 */
	private final SpelExpressionParser parser;
	/**
	 * 方法参数的名称解析器。
	 * <p>
	 * 该字段用于在解析 SpEL 表达式时识别方法的参数名称，为表达式解析提供上下文支持。
	 * </p>
	 *
	 * @see ParameterNameDiscoverer
	 * @see DefaultParameterNameDiscoverer
	 * @since 1.0.0
	 */
	private final ParameterNameDiscoverer discoverer;
	/**
	 * 幂等性校验器，用于验证请求是否为重复提交。
	 * <p>
	 * 负责具体的幂等性验证逻辑，包括检查指定键是否已存在，以及移除相关记录。
	 * 支持不同的实现方式（如 Redis 或本地缓存）。
	 * </p>
	 *
	 * @see IdempotentValidator
	 * @since 1.0.0
	 */
	private final IdempotentValidator idempotentValidator;

	/**
	 * 构造方法，初始化幂等性切面的核心组件。
	 *
	 * @param idempotentValidator 幂等性校验器，用于实际校验请求的唯一性。
	 * @since 1.0.0
	 */
	public IdempotentAspect(IdempotentValidator idempotentValidator) {
		this.parser = new SpelExpressionParser();
		this.discoverer = new DefaultParameterNameDiscoverer();
		this.idempotentValidator = idempotentValidator;
	}

	/**
	 * 方法执行前的幂等性校验逻辑。
	 * <p>
	 * 拦截带有 {@link Idempotent} 注解的控制器方法，通过解析注解中的 SpEL 表达式生成唯一键，
	 * 并调用 {@link IdempotentValidator} 进行校验。如果校验失败，则直接抛出 {@link IdempotentException}；
	 * 如果校验过程中发生异常，则会移除已记录的幂等性键并抛出 {@link ServerException}。
	 * </p>
	 *
	 * <p>
	 * 校验步骤：
	 * <ol>
	 *     <li>从方法签名和参数中动态解析 SpEL 表达式生成唯一键。</li>
	 *     <li>检查键是否已存在于幂等性存储中，判断是否为重复请求。</li>
	 *     <li>校验通过时，记录请求键并允许方法继续执行。</li>
	 *     <li>校验失败时，抛出异常，阻止方法执行。</li>
	 * </ol>
	 * </p>
	 *
	 * @param point AOP 切点，包含拦截方法的上下文信息（如方法签名和参数）。
	 * @throws IdempotentException 当请求为重复时抛出此异常。
	 * @throws ServerException     当校验过程中发生异常时抛出此异常。
	 * @since 1.0.0
	 */
	@Before("(@within(org.springframework.web.bind.annotation.RestController) || " +
		"@within(org.springframework.stereotype.Controller)) && " +
		"@annotation(io.github.pangju666.framework.boot.web.annotation.Idempotent)")
	public void doBefore(JoinPoint point) {
		MethodSignature methodSignature = (MethodSignature) point.getSignature();
		Method method = methodSignature.getMethod();
		Idempotent annotation = method.getAnnotation(Idempotent.class);

		String key = null;
		if (StringUtils.isNotBlank(annotation.key())) {
			EvaluationContext context = SpELUtils.initEvaluationContext(method, point.getArgs(), discoverer);
			Expression expression = parser.parseExpression(annotation.key());
			key = expression.getValue(context, String.class);
			if (StringUtils.isNotBlank(key)) {
				key = annotation.prefix() + "_" + key;
			}
		}

		boolean result = true;
		if (Objects.nonNull(key)) {
			try {
				result = idempotentValidator.validate(key, annotation);
			} catch (Exception e) {
				idempotentValidator.remove(annotation.key(), annotation);
				throw new ServerException(e);
			}
		}
		if (!result) {
			throw new IdempotentException(annotation);
		}
	}
}
