package io.github.pangju666.framework.autoconfigure.cache.hash;

import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

public interface HashCacheManager {
	int DEFAULT_BATCH_SIZE = 100000;

	boolean existCache(String cacheName);

	boolean exist(String cacheName, String key);

	Object get(String cacheName, String key);

	default List<Object> multiGet(String cacheName, Collection<String> keys) {
		return multiGet(cacheName, keys, DEFAULT_BATCH_SIZE);
	}

	List<Object> multiGet(String cacheName, Collection<String> keys, int batchSize);

	List<Object> getAll(String cacheName);

	void put(String cacheName, String key, Object value);

	default void putAll(String cacheName, @Nullable String keyFieldName, Collection<?> values) {
		putAll(cacheName, keyFieldName, values, DEFAULT_BATCH_SIZE);
	}

	void putAll(String cacheName, @Nullable String keyFieldName, Collection<?> values, int batchSize);

	void evict(String cacheName, String key);

	default void evictAll(String cacheName, @Nullable String keyFieldName, Collection<?> keys) {
		evictAll(cacheName, keyFieldName, keys, DEFAULT_BATCH_SIZE);
	}

	void evictAll(String cacheName, @Nullable String keyFieldName, Collection<?> keys, int batchSize);

	void clear(String cacheName);

	void clearAll(String... cacheNames);

	void clearAll(Collection<String> cacheNames);
}