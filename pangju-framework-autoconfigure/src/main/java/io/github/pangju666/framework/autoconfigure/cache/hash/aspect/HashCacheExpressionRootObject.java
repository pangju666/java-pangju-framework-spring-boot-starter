package io.github.pangju666.framework.autoconfigure.cache.hash.aspect;


import java.lang.reflect.Method;

/**
 * 改编自org.springframework.cache.interceptor.CacheExpressionRootObject
 */
record HashCacheExpressionRootObject(Method method, Object[] args, Object target, Class<?> targetClass) {
}