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

package io.github.pangju666.framework.autoconfigure.web.repeater.aspect;

import io.github.pangju666.framework.autoconfigure.web.repeater.annotation.Repeat;
import io.github.pangju666.framework.autoconfigure.web.repeater.exception.RequestRepeatException;
import io.github.pangju666.framework.autoconfigure.web.repeater.handler.RequestRepeater;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Aspect
public class RequestRepeatAspect {
	private final SpelExpressionParser parser;
	private final ParameterNameDiscoverer discoverer;
	private final RequestRepeater requestRepeater;

	public RequestRepeatAspect(RequestRepeater requestRepeater) {
		this.parser = new SpelExpressionParser();
		this.discoverer = new DefaultParameterNameDiscoverer();
		this.requestRepeater = requestRepeater;
	}

	@Before("(@within(org.springframework.web.bind.annotation.RestController) || " +
		"@within(org.springframework.stereotype.Controller)) && " +
		"@annotation(io.github.pangju666.framework.autoconfigure.web.repeater.annotation.Repeat)")
	public void doBefore(JoinPoint point) {
		MethodSignature methodSignature = (MethodSignature) point.getSignature();
		Method method = methodSignature.getMethod();
		Repeat annotation = method.getAnnotation(Repeat.class);

		boolean result;
		try {
			EvaluationContext context = SpELUtils.initEvaluationContext(method, point.getArgs(), discoverer);
			Expression expression = parser.parseExpression(annotation.key());
			String key = expression.getValue(context, String.class);
			result = requestRepeater.tryAcquire(key, annotation, ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest());
		} catch (Exception e) {
			throw new ServerException(e);
		}
		if (!result) {
			throw new RequestRepeatException(annotation);
		}
	}
}
