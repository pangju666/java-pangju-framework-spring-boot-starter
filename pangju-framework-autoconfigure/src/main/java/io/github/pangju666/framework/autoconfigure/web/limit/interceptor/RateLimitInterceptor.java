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

package io.github.pangju666.framework.autoconfigure.web.limit.interceptor;

import io.github.pangju666.framework.autoconfigure.spring.StaticSpringContext;
import io.github.pangju666.framework.autoconfigure.web.limit.annotation.RateLimit;
import io.github.pangju666.framework.autoconfigure.web.limit.enums.RateLimitScope;
import io.github.pangju666.framework.autoconfigure.web.limit.exception.RateLimitException;
import io.github.pangju666.framework.autoconfigure.web.limit.limiter.RateLimiter;
import io.github.pangju666.framework.autoconfigure.web.limit.source.RateLimitSourceExtractor;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.interceptor.BaseHttpHandlerInterceptor;
import io.github.pangju666.framework.web.utils.ServletResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitInterceptor extends BaseHttpHandlerInterceptor {
	private final RateLimiter rateLimiter;
	private final SpelExpressionParser parser;
	private final Map<Class<? extends RateLimitSourceExtractor>, RateLimitSourceExtractor> sourceExtractorMap = new ConcurrentHashMap(10);

	public RateLimitInterceptor(RateLimiter requestLimiter) {
		super(Collections.singleton("/**"), Collections.emptySet());
		this.rateLimiter = requestLimiter;
		this.parser = new SpelExpressionParser();
	}

	@Override
	public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
		if (handler instanceof HandlerMethod handlerMethod) {
			RateLimit annotation = handlerMethod.getMethodAnnotation(RateLimit.class);
			if (Objects.isNull(annotation)) {
				Class<?> targetClass = handlerMethod.getBeanType();
				annotation = targetClass.getAnnotation(RateLimit.class);
			}
			if (Objects.isNull(annotation)) {
				return true;
			}

			try {
				String key = generateKey(annotation, request);
				if (!rateLimiter.tryAcquire(key, annotation, request)) {
					ServletResponseUtils.writeHttpExceptionToResponse(new RateLimitException(annotation), response);
					return false;
				}
			} catch (Exception e) {
				ServletResponseUtils.writeHttpExceptionToResponse(new ServerException(e), response);
				return false;
			}
		}
		return true;
	}

	private String generateKey(RateLimit annotation, HttpServletRequest request) throws NoSuchMethodException,
		InvocationTargetException, InstantiationException, IllegalAccessException {
		StringBuilder keyBuilder = new StringBuilder(annotation.prefix().trim());
		if (StringUtils.isNotBlank(annotation.key())) {
			EvaluationContext context = new StandardEvaluationContext();
			context.setVariable("request", request);
			Expression expression = parser.parseExpression(annotation.key());
			keyBuilder.append(expression.getValue(context, String.class));
		}
		if (keyBuilder.isEmpty()) {
			keyBuilder
				.append(request.getRequestURI())
				.append("_")
				.append(request.getMethod());
		}

		if (annotation.scope() == RateLimitScope.SOURCE) {
			RateLimitSourceExtractor sourceExtractor;
			try {
				sourceExtractor = sourceExtractorMap.putIfAbsent(annotation.source(),
					StaticSpringContext.getBeanFactory().getBean(annotation.source()));
				if (Objects.isNull(sourceExtractor)) {
					sourceExtractor = sourceExtractorMap.get(annotation.source());
				}
			} catch (NoSuchBeanDefinitionException e) {
				sourceExtractor = sourceExtractorMap.putIfAbsent(annotation.source(),
					annotation.source().getDeclaredConstructor().newInstance());
				if (Objects.isNull(sourceExtractor)) {
					sourceExtractor = sourceExtractorMap.get(annotation.source());
				}
			}

			keyBuilder
				.append("_")
				.append(sourceExtractor.getSource(request));
		}

		return keyBuilder.toString();
	}
}
