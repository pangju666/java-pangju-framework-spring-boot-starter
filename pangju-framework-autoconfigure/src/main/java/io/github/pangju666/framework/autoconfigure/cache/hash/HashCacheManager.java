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

package io.github.pangju666.framework.autoconfigure.cache.hash;

import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

public interface HashCacheManager {
	int DEFAULT_MULTI_GET_SIZE = 500;
	int DEFAULT_BATCH_SIZE = 1000;

	boolean existCache(String cacheName);

	boolean exist(String cacheName, String hashKey);

	Object get(String cacheName, String hashKey);

	default List<Object> multiGet(String cacheName, Collection<String> hashKeys) {
		return multiGet(cacheName, hashKeys, DEFAULT_MULTI_GET_SIZE);
	}

	List<Object> multiGet(String cacheName, Collection<String> hashKeys, int batchSize);

	List<Object> getAll(String cacheName);

	void put(String cacheName, String hashKey, Object value);

	default void putAll(String cacheName, @Nullable String keyFieldName, Collection<?> values) {
		putAll(cacheName, keyFieldName, values, DEFAULT_BATCH_SIZE);
	}

	void putAll(String cacheName, @Nullable String keyFieldName, Collection<?> values, int batchSize);

	void evict(String cacheName, String hashKeys);

	default void evictAll(String cacheName, @Nullable String keyFieldName, Collection<?> hashKeys) {
		evictAll(cacheName, keyFieldName, hashKeys, DEFAULT_BATCH_SIZE);
	}

	void evictAll(String cacheName, @Nullable String keyFieldName, Collection<?> hashKeys, int batchSize);

	void clear(String cacheName);

	void clearAll(String... cacheNames);

	void clearAll(Collection<String> cacheNames);
}