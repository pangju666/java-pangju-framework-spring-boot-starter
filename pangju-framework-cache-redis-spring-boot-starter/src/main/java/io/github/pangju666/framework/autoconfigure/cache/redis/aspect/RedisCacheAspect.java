package io.github.pangju666.framework.autoconfigure.cache.redis.aspect;

import io.github.pangju666.commons.lang.utils.ReflectionUtils;
import io.github.pangju666.framework.autoconfigure.cache.redis.RedisCacheManager;
import io.github.pangju666.framework.autoconfigure.cache.redis.annoation.RedisCacheEvict;
import io.github.pangju666.framework.autoconfigure.cache.redis.annoation.RedisCachePut;
import io.github.pangju666.framework.autoconfigure.cache.redis.annoation.RedisCacheable;
import io.github.pangju666.framework.autoconfigure.cache.redis.annoation.RedisCaching;
import io.github.pangju666.framework.core.utils.SpELUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Aspect
public class RedisCacheAspect {
    private final SpelExpressionParser parser;
    private final ParameterNameDiscoverer discoverer;
    private final RedisCacheManager cacheManager;

    public RedisCacheAspect(RedisCacheManager cacheManager) {
        this.parser = new SpelExpressionParser();
        this.discoverer = new DefaultParameterNameDiscoverer();
        this.cacheManager = cacheManager;
    }

    @Around("@annotation(io.github.pangju666.framework.autoconfigure.cache.redis.annoation.RedisCacheable)")
    public Object handleRedisCacheable(ProceedingJoinPoint point) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();
        EvaluationContext context = SpELUtils.initEvaluationContext(method, point.getArgs(), discoverer);

        RedisCacheable annotation = method.getAnnotation(RedisCacheable.class);

        Boolean condition = true;
        if (StringUtils.isNotBlank(annotation.condition())) {
            Expression conditionExpression = parser.parseExpression(annotation.condition());
            condition = conditionExpression.getValue(context, Boolean.class);
        }
        if (Boolean.TRUE.equals(condition)) {
            String cacheName = annotation.cache();
            if (StringUtils.isBlank(cacheName)) {
                return point.proceed();
            }

            if (annotation.allEntries()) {
                if (!cacheManager.existCache(cacheName)) {
                    Object returnValue = point.proceed();
                    putResultToCache(returnValue, context, cacheName, annotation);
                    return returnValue;
                }

                List<Object> result = cacheManager.getAll(cacheName)
                        .stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(result)) {
                    result.sort(getResultComparator(context, annotation.keyField(), annotation.sortField(), annotation.reverseOrder()));
                }
                return result;
            }

            Expression keyExpression = parser.parseExpression(annotation.key());
            Object key = keyExpression.getValue(context, Object.class);
            if (ObjectUtils.isEmpty(key)) {
                return point.proceed();
            }
            if (key instanceof Collection<?> collection) {
                if (CollectionUtils.isEmpty(collection)) {
                    return Collections.emptyList();
                }
                if (!cacheManager.existCache(cacheName)) {
                    Object returnValue = point.proceed();
                    putResultToCache(returnValue, context, cacheName, annotation);
                    return returnValue;
                }

                Set<String> hashKeys;
                Class<?> keyClass = collection.iterator()
                        .next()
                        .getClass();
                if (ClassUtils.isPrimitiveOrWrapper(keyClass)) {
                    hashKeys = collection
                            .stream()
                            .map(Object::toString)
                            .collect(Collectors.toSet());
                } else {
                    hashKeys = collection
                            .stream()
                            .map(object -> getFieldValue(object, annotation.keyField()))
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .collect(Collectors.toSet());
                }

                List<Object> result = ListUtils.emptyIfNull(cacheManager.multiGet(cacheName, hashKeys))
                        .stream()
                        .filter(Objects::nonNull)
                        .sorted(getResultComparator(context, annotation.keyField(), annotation.sortField(), annotation.reverseOrder()))
                        .toList();
                if (result.size() != hashKeys.size()) {
                    Object returnValue = point.proceed();
                    putResultToCache(returnValue, context, cacheName, annotation);
                    return returnValue;
                }
                return result;
            } else {
                String hashKey = key.toString();
                if (StringUtils.isBlank(hashKey)) {
                    return point.proceed();
                }
                if (!ClassUtils.isPrimitiveOrWrapper(key.getClass())) {
                    hashKey = getFieldValue(key, annotation.keyField()).toString();
                }
                if (cacheManager.exist(cacheName, hashKey)) {
                    return cacheManager.get(cacheName, hashKey);
                }

                Object returnValue = point.proceed();
                context.setVariable("result", returnValue);
                Boolean unless = false;
                if (StringUtils.isNotBlank(annotation.unless())) {
                    Expression unlessExpression = parser.parseExpression(annotation.unless());
                    unless = unlessExpression.getValue(context, Boolean.class);
                }
                if (!Boolean.TRUE.equals(unless)) {
                    cacheManager.put(cacheName, hashKey, returnValue);
                }
                return returnValue;
            }
        }
        return point.proceed();
    }

    @AfterReturning(pointcut = "@annotation(io.github.pangju666.framework.autoconfigure.cache.redis.annoation.RedisCachePut)", returning = "returnValue")
    public void handleRedisCachePut(JoinPoint point, Object returnValue) {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();

        EvaluationContext context = SpELUtils.initEvaluationContext(method, point.getArgs(), discoverer);
        context.setVariable("result", returnValue);

        RedisCachePut annotation = method.getAnnotation(RedisCachePut.class);
        put(context, annotation);
    }

    @AfterReturning(pointcut = "@annotation(io.github.pangju666.framework.autoconfigure.cache.redis.annoation.RedisCacheEvict)", returning = "returnValue")
    public void handleRedisCacheEvict(JoinPoint point, Object returnValue) {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();

        EvaluationContext context = SpELUtils.initEvaluationContext(method, point.getArgs(), discoverer);
        context.setVariable("result", returnValue);

        RedisCacheEvict annotation = method.getAnnotation(RedisCacheEvict.class);
        evict(context, annotation);
    }

    @AfterReturning(pointcut = "@annotation(io.github.pangju666.framework.autoconfigure.cache.redis.annoation.RedisCaching)", returning = "returnValue")
    public void handleRedisCaching(JoinPoint point, Object returnValue) {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();

        EvaluationContext context = SpELUtils.initEvaluationContext(method, point.getArgs(), discoverer);
        context.setVariable("result", returnValue);

        RedisCaching annotation = method.getAnnotation(RedisCaching.class);
        for (RedisCacheEvict evictAnnotation : annotation.evicts()) {
            evict(context, evictAnnotation);
        }
        for (RedisCachePut putAnnotation : annotation.puts()) {
            put(context, putAnnotation);
        }
    }

    private Comparator<? super Object> getResultComparator(EvaluationContext context, String hashKeyField,
                                                           String sortField, String reverseOrder) {
        Boolean isReverseOrder = false;
        if (StringUtils.isNotBlank(reverseOrder)) {
            Expression reverseOrderExpression = parser.parseExpression(reverseOrder);
            isReverseOrder = reverseOrderExpression.getValue(context, Boolean.class);
        }
        Comparator<? super Object> comparator = (a, b) -> {
            if (StringUtils.isBlank(hashKeyField)) {
                Object left = a;
                Object right = b;

                if (StringUtils.isNotBlank(sortField)) {
                    left = ReflectionUtils.getFieldValue(a, sortField);
                    right = ReflectionUtils.getFieldValue(b, sortField);
                }

                if (left instanceof Long longA && right instanceof Long longB) {
                    return longA.compareTo(longB);
                }
                if (left instanceof Integer integerA && right instanceof Integer integerB) {
                    return integerA.compareTo(integerB);
                }
                if (left instanceof Short shortA && right instanceof Short shortB) {
                    return shortA.compareTo(shortB);
                }
                if (left instanceof Double doubleA && right instanceof Double doubleB) {
                    return doubleA.compareTo(doubleB);
                }
                if (left instanceof Float floatA && right instanceof Float floatB) {
                    return floatA.compareTo(floatB);
                }
                if (left instanceof Date dateA && right instanceof Date dateB) {
                    return dateA.compareTo(dateB);
                }
                if (left instanceof LocalDate localDateA && right instanceof LocalDate localDateB) {
                    return localDateA.compareTo(localDateB);
                }
                if (left instanceof LocalDateTime localDateTimeA && right instanceof LocalDateTime localDateTimeB) {
                    return localDateTimeA.compareTo(localDateTimeB);
                }
                if (left instanceof BigInteger bigIntegerA && right instanceof BigInteger bigIntegerB) {
                    return bigIntegerA.compareTo(bigIntegerB);
                }
                if (left instanceof BigDecimal bigDecimalA && right instanceof BigDecimal bigDecimalB) {
                    return bigDecimalA.compareTo(bigDecimalB);
                }
                if (left instanceof String stringA && right instanceof String stringB) {
                    return stringA.compareTo(stringB);
                }
                return Integer.compare(left.hashCode(), right.hashCode());
            }
            if (StringUtils.isNotBlank(sortField)) {
                return Comparator.naturalOrder().compare(
                        ReflectionUtils.getFieldValue(a, sortField),
                        ReflectionUtils.getFieldValue(b, sortField)
                );
            } else {
                return Comparator.naturalOrder().compare(
                        ReflectionUtils.getFieldValue(a, hashKeyField),
                        ReflectionUtils.getFieldValue(b, hashKeyField)
                );
            }
        };
        return Boolean.TRUE.equals(isReverseOrder) ? comparator.reversed() : comparator;
    }

    private void putResultToCache(Object result, EvaluationContext context, String cacheName, RedisCacheable annotation) {
        if (result instanceof Collection<?> resultCollection) {
            context.setVariable("result", resultCollection);

            Boolean unless = false;
            if (StringUtils.isNotBlank(annotation.unless())) {
                Expression unlessExpression = parser.parseExpression(annotation.unless());
                unless = unlessExpression.getValue(context, Boolean.class);
            }
            if (!Boolean.TRUE.equals(unless)) {
                cacheManager.putAll(cacheName, StringUtils.defaultIfBlank(annotation.keyField(), null), resultCollection);
            }
        }
    }

    private Object getFieldValue(Object object, String field) {
        return StringUtils.isNotBlank(field) ? ReflectionUtils.getFieldValue(object, field) : object;
    }

    private void put(EvaluationContext context, RedisCachePut annotation) {
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
                    cacheManager.putAll(cacheName, StringUtils.defaultIfBlank(annotation.key(), null), CollectionUtils.emptyIfNull(collection));
                } else {
                    Expression keyExpression = parser.parseExpression(annotation.key());
                    Object key = keyExpression.getValue(context, Object.class);
                    if (Objects.nonNull(key)) {
                        String keyStr = StringUtils.defaultIfBlank(key.toString(), value.toString());
                        cacheManager.put(cacheName, keyStr, value);
                    }
                }
            }
        }
    }

    private void evict(EvaluationContext context, RedisCacheEvict annotation) {
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
                cacheManager.clearAll(cacheNames);
            } else {
                Expression keyExpression = parser.parseExpression(annotation.key());
                Object key = keyExpression.getValue(context, Object.class);
                if (Objects.nonNull(key)) {
                    if (key instanceof Collection<?> collection) {
                        for (String cacheName : cacheNames) {
                            cacheManager.evictAll(cacheName, StringUtils.defaultIfBlank(annotation.keyField(), null), collection);
                        }
                    } else {
                        String keyStr = key.toString();
                        if (StringUtils.isNotBlank(keyStr)) {
                            for (String cacheName : cacheNames) {
                                cacheManager.evict(cacheName, keyStr);
                            }
                        }
                    }
                }
            }
        }
    }
}