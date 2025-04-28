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

package io.github.pangju666.framework.autoconfigure.cache.hash.redis;

import io.github.pangju666.framework.autoconfigure.cache.hash.HashCacheManager;
import io.github.pangju666.framework.data.redis.utils.RedisUtils;
import io.github.pangju666.framework.spring.utils.ReflectionUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class RedisHashCacheManager implements HashCacheManager {
	private final RedisTemplate<String, Object> redisTemplate;
	private final String cacheNamePrefix;
	private final boolean cacheNullValues;

	public RedisHashCacheManager(RedisTemplate<String, Object> redisTemplate, String cacheNamePrefix, boolean cacheNullValues) {
		this.redisTemplate = redisTemplate;
		this.cacheNullValues = cacheNullValues;
		this.cacheNamePrefix = StringUtils.defaultIfBlank(cacheNamePrefix, StringUtils.EMPTY);
	}

	public RedisTemplate<String, Object> getRedisTemplate() {
		return redisTemplate;
	}

	public boolean existCache(String cacheName) {
		return redisTemplate.hasKey(getCacheName(cacheName));
	}

	public boolean exist(String cacheName, String key) {
		return redisTemplate.opsForHash().hasKey(getCacheName(cacheName), key);
	}

	public Object get(String cacheName, String key) {
		return redisTemplate.opsForHash().get(getCacheName(cacheName), key);
	}

	public List<Object> multiGet(String cacheName, Collection<String> keys, int batchSize) {
		Set<Object> hashKeys = CollectionUtils.emptyIfNull(keys)
			.stream()
			.filter(StringUtils::isNotBlank)
			.collect(Collectors.toSet());
		if (CollectionUtils.isEmpty(hashKeys)) {
			return Collections.emptyList();
		}
		return ListUtils.partition(new ArrayList<>(hashKeys), batchSize)
			.stream()
			.map(part -> redisTemplate.opsForHash().multiGet(getCacheName(cacheName), hashKeys))
			.flatMap(List::stream)
			.toList();
	}

	public List<Object> getAll(String cacheName) {
		return redisTemplate.opsForHash().values(getCacheName(cacheName));
	}

	public void put(String cacheName, String key, Object value) {
		if (Objects.nonNull(value) || cacheNullValues) {
			redisTemplate.opsForHash().put(getCacheName(cacheName), key, value);
		}
	}

	public void putAll(String cacheName, @Nullable String keyFieldName, Collection<?> values, int batchSize) {
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
			ListUtils.partition(new ArrayList<>(map.entrySet()), batchSize)
				.parallelStream()
				.forEach(part -> {
					redisTemplate.opsForHash().putAll(getCacheName(cacheName), Map.ofEntries(part.toArray(Map.Entry[]::new)));
				});
			/*for (List<Map.Entry<String, Object>> part : ListUtils.partition(new ArrayList<>(map.entrySet()), batchSize)) {
				redisTemplate.opsForHash().putAll(getCacheName(cacheName), Map.ofEntries(part.toArray(Map.Entry[]::new)));
			}*/
		}
	}

	public void evict(String cacheName, String key) {
		redisTemplate.opsForHash().delete(getCacheName(cacheName), key);
	}

	public void evictAll(String cacheName, @Nullable String keyFieldName, Collection<?> keys, int batchSize) {
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
			for (List<?> part : ListUtils.partition(new ArrayList<>(hashKeys), batchSize)) {
				redisTemplate.opsForHash().delete(getCacheName(cacheName), part.toArray(Object[]::new));
			}
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
			return RedisUtils.computeKey(cacheNamePrefix, cacheName);
		}
		return cacheName;
	}
}