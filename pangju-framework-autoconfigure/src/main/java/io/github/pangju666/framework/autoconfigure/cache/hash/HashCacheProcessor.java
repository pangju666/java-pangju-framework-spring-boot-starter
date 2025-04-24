package io.github.pangju666.framework.autoconfigure.cache.hash;

public interface HashCacheProcessor {
	void init(HashCacheManager hashCacheManager);

	void destroy(HashCacheManager hashCacheManager);
}
