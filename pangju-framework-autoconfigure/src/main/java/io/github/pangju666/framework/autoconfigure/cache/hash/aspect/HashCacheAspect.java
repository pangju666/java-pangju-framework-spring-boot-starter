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

package io.github.pangju666.framework.autoconfigure.cache.hash.aspect;

import io.github.pangju666.framework.autoconfigure.cache.hash.HashCacheManager;
import io.github.pangju666.framework.autoconfigure.cache.hash.annoation.HashCacheEvict;
import io.github.pangju666.framework.autoconfigure.cache.hash.annoation.HashCachePut;
import io.github.pangju666.framework.autoconfigure.cache.hash.annoation.HashCacheable;
import io.github.pangju666.framework.autoconfigure.cache.hash.annoation.HashCaching;
import io.github.pangju666.framework.spring.utils.ReflectionUtils;
import io.github.pangju666.framework.spring.utils.SpELUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Aspect
public class HashCacheAspect {
	private final SpelExpressionParser parser;
	private final ParameterNameDiscoverer discoverer;
	private final HashCacheManager hashCacheManager;

	public HashCacheAspect(HashCacheManager hashCacheManager) {
		this.parser = new SpelExpressionParser();
		this.discoverer = new DefaultParameterNameDiscoverer();
		this.hashCacheManager = hashCacheManager;
	}

	@Around("@annotation(io.github.pangju666.framework.autoconfigure.cache.hash.annoation.HashCacheable)")
	public Object handleHashCacheable(ProceedingJoinPoint point) throws Throwable {
		Signature signature = point.getSignature();
		if (!(signature instanceof MethodSignature)) {
			return point.proceed();
		}

		MethodSignature methodSignature = (MethodSignature) point.getSignature();
		Method method = methodSignature.getMethod();
		Object[] args = point.getArgs();
		Object target = point.getTarget();
		HashCacheExpressionRootObject rootObject = new HashCacheExpressionRootObject(method, args, target, target.getClass());

		EvaluationContext context = SpELUtils.initEvaluationContext(method, args, discoverer);
		context.setVariable("target", target);
		context.setVariable("root", rootObject);

		HashCacheable annotation = method.getAnnotation(HashCacheable.class);
		Object cacheKey;

		// 如果缓存名称为空，则直接返回方法值
		if (StringUtils.isBlank(annotation.cache())) {
			return point.proceed();
		}
		// 判断缓存结果是否存在，存在则返回缓存结果
		if (hashCacheManager.existCache(annotation.cache())) {
			// 判断是否返回所有缓存条目
			if (annotation.allEntries()) {
				return hashCacheManager.getAll(annotation.cache());
			} else {
				// 如果key表达式为空，则直接返回方法值
				if (StringUtils.isBlank(annotation.key())) {
					return point.proceed();
				}

				Expression expression = parser.parseExpression(annotation.key());
				cacheKey = expression.getValue(context, rootObject);
				// 如果key为null，则直接返回方法值
				if (Objects.isNull(cacheKey)) {
					return point.proceed();
				} else if (cacheKey instanceof Collection<?> collection) {    // 根据hashkey集合获取缓存结果
					List<?> cacheResult = getEntities(annotation.cache(), collection, annotation.keyField());
					// 如果缓存条目结果不为空则返回
					if (!cacheResult.isEmpty()) {
						return cacheResult;
					}
					// 如果存在缓存条目则返回
				} else if (hashCacheManager.exist(annotation.cache(), Objects.toString(cacheKey))) {
					return hashCacheManager.get(annotation.cache(), Objects.toString(cacheKey));
				}
			}
		}

		Boolean condition = true;
		// 如果缓存写入条件表达式不为空，则计算缓存写入条件
		if (StringUtils.isNotBlank(annotation.condition())) {
			Expression conditionExpression = parser.parseExpression(annotation.condition());
			condition = conditionExpression.getValue(context, rootObject, Boolean.class);
		}
		// 如果缓存写入条件不为true，则直接返回方法值
		if (!Boolean.TRUE.equals(condition)) {
			return point.proceed();
		}

		Object returnValue = point.proceed();
		context.setVariable("result", returnValue);

		Boolean unless = false;
		// 如果缓存否决条件表达式不为空，则计算缓存否决条件
		if (StringUtils.isNotBlank(annotation.unless())) {
			Expression unlessExpression = parser.parseExpression(annotation.unless());
			unless = unlessExpression.getValue(context, rootObject, Boolean.class);
		}
		// 如果缓存否决条件不为true，则将返回值写入缓存
		if (!Boolean.TRUE.equals(unless)) {
			if (returnValue instanceof Collection<?> collection) {
				hashCacheManager.putAll(annotation.cache(), StringUtils.defaultIfBlank(
					annotation.keyField(), null), collection);
			} else {
				hashCacheManager.put(annotation.cache(), StringUtils.defaultIfBlank(annotation.keyField(),
					null), returnValue);
			}
		}

		return returnValue;
	}

	@AfterReturning(pointcut = "@annotation(io.github.pangju666.framework.autoconfigure.cache.hash.annoation.HashCachePut)", returning = "returnValue")
	public void handleHashCachePut(JoinPoint point, Object returnValue) {
		MethodSignature methodSignature = (MethodSignature) point.getSignature();
		Method method = methodSignature.getMethod();

		EvaluationContext context = SpELUtils.initEvaluationContext(method, point.getArgs(), discoverer);
		context.setVariable("result", returnValue);
		context.setVariable("target", point.getTarget());

		HashCachePut annotation = method.getAnnotation(HashCachePut.class);
		put(context, annotation);
	}

	@AfterReturning(pointcut = "@annotation(io.github.pangju666.framework.autoconfigure.cache.hash.annoation.HashCacheEvict)", returning = "returnValue")
	public void handleHashCacheEvict(JoinPoint point, Object returnValue) {
		MethodSignature methodSignature = (MethodSignature) point.getSignature();
		Method method = methodSignature.getMethod();

		EvaluationContext context = SpELUtils.initEvaluationContext(method, point.getArgs(), discoverer);
		context.setVariable("result", returnValue);
		context.setVariable("target", point.getTarget());

		HashCacheEvict annotation = method.getAnnotation(HashCacheEvict.class);
		evict(context, annotation);
	}

	@AfterReturning(pointcut = "@annotation(io.github.pangju666.framework.autoconfigure.cache.hash.annoation.HashCaching)", returning = "returnValue")
	public void handleHashCaching(JoinPoint point, Object returnValue) {
		MethodSignature methodSignature = (MethodSignature) point.getSignature();
		Method method = methodSignature.getMethod();

		EvaluationContext context = SpELUtils.initEvaluationContext(method, point.getArgs(), discoverer);
		context.setVariable("result", returnValue);
		context.setVariable("target", point.getTarget());

		HashCaching annotation = method.getAnnotation(HashCaching.class);
		for (HashCacheEvict evictAnnotation : annotation.evicts()) {
			evict(context, evictAnnotation);
		}
		for (HashCachePut putAnnotation : annotation.puts()) {
			put(context, putAnnotation);
		}
	}

	private void put(EvaluationContext context, HashCachePut annotation) {
		Boolean condition = true;
		if (StringUtils.isNotBlank(annotation.condition())) {
			Expression conditionExpression = parser.parseExpression(annotation.condition());
			condition = conditionExpression.getValue(context, Boolean.class);
		}

		if (Boolean.TRUE.equals(condition)) {
			String cacheName = annotation.cache();
			if (StringUtils.isBlank(cacheName)) {
				return;
			}

			Expression valueExpression = parser.parseExpression(annotation.value());
			Object value = valueExpression.getValue(context, Object.class);
			if (Objects.nonNull(value)) {
				if (value instanceof Collection<?> collection) {
					hashCacheManager.putAll(cacheName, StringUtils.defaultIfBlank(annotation.key(), null), CollectionUtils.emptyIfNull(collection));
				} else {
					Expression keyExpression = parser.parseExpression(annotation.key());
					Object key = keyExpression.getValue(context, Object.class);
					if (Objects.nonNull(key)) {
						String keyStr = StringUtils.defaultIfBlank(key.toString(), value.toString());
						hashCacheManager.put(cacheName, keyStr, value);
					}
				}
			}
		}
	}

	private void evict(EvaluationContext context, HashCacheEvict annotation) {
		Boolean condition = true;
		if (StringUtils.isNotBlank(annotation.condition())) {
			Expression conditionExpression = parser.parseExpression(annotation.condition());
			condition = conditionExpression.getValue(context, Boolean.class);
		}

		if (Boolean.TRUE.equals(condition)) {
			String[] cacheNames = annotation.caches();
			if (cacheNames.length == 0) {
				return;
			}

			if (annotation.allEntries()) {
				hashCacheManager.clearAll(cacheNames);
			} else {
				Expression keyExpression = parser.parseExpression(annotation.key());
				Object key = keyExpression.getValue(context, Object.class);
				if (Objects.nonNull(key)) {
					if (key instanceof Collection<?> collection) {
						for (String cacheName : cacheNames) {
							hashCacheManager.evictAll(cacheName, StringUtils.defaultIfBlank(annotation.keyField(), null), collection);
						}
					} else {
						String keyStr = key.toString();
						if (StringUtils.isNotBlank(keyStr)) {
							for (String cacheName : cacheNames) {
								hashCacheManager.evict(cacheName, keyStr);
							}
						}
					}
				}
			}
		}
	}

	private List<?> getEntities(String cacheName, Collection<?> cacheKeys, String cacheKeyField) {
		Set<String> hashKeys;
		Class<?> keyClass = ReflectionUtils.getClassGenericType(cacheKeys.getClass());
		if (ClassUtils.isPrimitiveOrWrapper(keyClass) || keyClass.isAssignableFrom(String.class)) {
			hashKeys = cacheKeys.stream()
				.filter(Objects::nonNull)
				.map(Objects::toString)
				.collect(Collectors.toSet());
		} else {
			hashKeys = cacheKeys.stream()
				.map(object -> {
					if (StringUtils.isBlank(cacheKeyField)) {
						return Objects.toString(object, null);
					} else {
						return Objects.toString(ReflectionUtils.getFieldValue(object, cacheKeyField), null);
					}
				})
				.filter(Objects::nonNull)
				.map(Objects::toString)
				.collect(Collectors.toSet());
		}
		List<?> cacheResult = ListUtils.emptyIfNull(hashCacheManager.multiGet(cacheName, hashKeys));
		if (cacheResult.size() == cacheKeys.size()) {
			return cacheResult;
		}
		return Collections.emptyList();
	}
}