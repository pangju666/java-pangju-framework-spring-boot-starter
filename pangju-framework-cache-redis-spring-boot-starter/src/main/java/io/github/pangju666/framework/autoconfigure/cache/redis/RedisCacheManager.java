package io.github.pangju666.framework.autoconfigure.cache.redis;

import io.github.pangju666.commons.lang.utils.ReflectionUtils;
import io.github.pangju666.framework.autoconfigure.cache.redis.properties.RedisCacheProperties;
import io.github.pangju666.framework.data.redis.utils.RedisUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class RedisCacheManager {
    private final RedisTemplate<String, Object> redisTemplate;
    private final String cacheNamePrefix;
    private final boolean cacheNullValues;

    public RedisCacheManager(RedisTemplate<String, Object> redisTemplate, RedisCacheProperties properties) {
        this.redisTemplate = redisTemplate;
        this.cacheNullValues = properties.isCacheNullValues();
        this.cacheNamePrefix = properties.isUseCachePrefix() ? properties.getCachePrefix() : StringUtils.EMPTY;
    }

    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    public boolean existCache(String cacheName) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(getCacheName(cacheName)));
    }

    public boolean exist(String cacheName, String key) {
        return redisTemplate.opsForHash().hasKey(getCacheName(cacheName), key);
    }

    public Object get(String cacheName, String key) {
        return redisTemplate.opsForHash().get(getCacheName(cacheName), key);
    }

    public List<Object> multiGet(String cacheName, Collection<String> keys) {
        Set<Object> hashKeys = CollectionUtils.emptyIfNull(keys)
                .stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(hashKeys)) {
            return Collections.emptyList();
        }
        return redisTemplate.opsForHash().multiGet(getCacheName(cacheName), hashKeys);
    }

    public List<Object> getAll(String cacheName) {
        return redisTemplate.opsForHash().values(getCacheName(cacheName));
    }

    public void put(String cacheName, String key, Object value) {
        if (Objects.nonNull(value) || cacheNullValues) {
            redisTemplate.opsForHash().put(getCacheName(cacheName), key, value);
        }
    }

    public void putAll(String cacheName, @Nullable String keyFieldName, Collection<?> values) {
        Map<String, Object> map;
        if (StringUtils.isBlank(keyFieldName)) {
            map = CollectionUtils.emptyIfNull(values)
                    .stream()
                    .filter(value -> ObjectUtils.isNotEmpty(value) || cacheNullValues)
                    .collect(Collectors.toMap(Object::toString, item -> item));
        } else {
            map = CollectionUtils.emptyIfNull(values)
                    .stream()
                    .filter(Objects::nonNull)
                    .map(item -> Pair.of(ReflectionUtils.getFieldValue(item, keyFieldName), item))
                    .filter(pair -> ObjectUtils.isNotEmpty(pair.getKey()) && (Objects.nonNull(pair.getValue()) || cacheNullValues))
                    .collect(Collectors.toMap(pair -> pair.getKey().toString(), Pair::getValue));
        }
        if (MapUtils.isNotEmpty(map)) {
            redisTemplate.opsForHash().putAll(getCacheName(cacheName), map);
        }
    }

    public void evict(String cacheName, String key) {
        redisTemplate.opsForHash().delete(getCacheName(cacheName), key);
    }

    public void evictAll(String cacheName, @Nullable String keyFieldName, Collection<?> keys) {
        Set<Object> hashKeys;
        if (StringUtils.isBlank(keyFieldName)) {
            hashKeys = keys.stream()
                    .filter(ObjectUtils::isNotEmpty)
                    .map(Object::toString)
                    .collect(Collectors.toSet());
        } else {
            hashKeys = keys.stream()
                    .filter(ObjectUtils::isNotEmpty)
                    .map(item -> ReflectionUtils.getFieldValue(item, keyFieldName).toString())
                    .collect(Collectors.toSet());
        }
        if (CollectionUtils.isNotEmpty(hashKeys)) {
            redisTemplate.opsForHash().delete(getCacheName(cacheName), hashKeys);
        }
    }

    public void clear(String cacheName) {
        redisTemplate.delete(getCacheName(cacheName));
    }

    public void clearAll(String... cacheNames) {
        Set<String> keys = Arrays.stream(cacheNames)
                .filter(StringUtils::isNotBlank)
                .map(this::getCacheName)
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    public void clearAll(Collection<String> cacheNames) {
        Set<String> keys = CollectionUtils.emptyIfNull(cacheNames)
                .stream()
                .filter(StringUtils::isNotBlank)
                .map(this::getCacheName)
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    private String getCacheName(String cacheName) {
        if (StringUtils.isNotBlank(cacheNamePrefix)) {
            return RedisUtils.generateKey(cacheNamePrefix, cacheName);
        }
        return cacheName;
    }
}
