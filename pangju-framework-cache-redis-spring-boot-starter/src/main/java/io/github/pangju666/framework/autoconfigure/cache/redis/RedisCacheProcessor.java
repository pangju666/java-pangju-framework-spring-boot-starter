package io.github.pangju666.framework.autoconfigure.cache.redis;

public interface RedisCacheProcessor {
    void init(RedisCacheManager cacheManager);

    void destroy(RedisCacheManager cacheManager);
}
