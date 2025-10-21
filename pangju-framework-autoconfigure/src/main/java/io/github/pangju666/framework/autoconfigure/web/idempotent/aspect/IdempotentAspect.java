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

package io.github.pangju666.framework.autoconfigure.web.idempotent.aspect;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.github.pangju666.framework.autoconfigure.web.idempotent.annotation.Idempotent;
import io.github.pangju666.framework.autoconfigure.web.idempotent.exception.IdempotentException;
import io.github.pangju666.framework.autoconfigure.web.idempotent.validator.IdempotentValidator;
import io.github.pangju666.framework.spring.utils.SpELUtils;
import io.github.pangju666.framework.web.exception.base.ServerException;
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

@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Aspect
public class IdempotentAspect {
	private final SpelExpressionParser parser;
	private final ParameterNameDiscoverer discoverer;
	private final IdempotentValidator idempotentValidator;

	public IdempotentAspect(IdempotentValidator idempotentValidator) {
		this.parser = new SpelExpressionParser();
		this.discoverer = new DefaultParameterNameDiscoverer();
		this.idempotentValidator = idempotentValidator;
	}

	@Before("(@within(org.springframework.web.bind.annotation.RestController) || " +
		"@within(org.springframework.stereotype.Controller)) && " +
		"@annotation(io.github.pangju666.framework.autoconfigure.web.idempotent.annotation.Idempotent)")
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
