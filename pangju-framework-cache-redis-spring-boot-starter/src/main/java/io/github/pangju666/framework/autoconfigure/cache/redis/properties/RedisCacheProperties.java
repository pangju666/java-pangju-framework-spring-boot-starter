package io.github.pangju666.framework.autoconfigure.cache.redis.properties;

import io.github.pangju666.framework.autoconfigure.cache.redis.enums.RedisSerializerType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pangju.cache.redis")
public class RedisCacheProperties {
    private boolean cacheNullValues = true;
    private String cachePrefix;
    private boolean useCachePrefix = true;
    private RedisSerializerType valueSerializer = RedisSerializerType.JSON;

    public boolean isCacheNullValues() {
        return cacheNullValues;
    }

    public void setCacheNullValues(boolean cacheNullValues) {
        this.cacheNullValues = cacheNullValues;
    }

    public String getCachePrefix() {
        return cachePrefix;
    }

    public void setCachePrefix(String cachePrefix) {
        this.cachePrefix = cachePrefix;
    }

    public boolean isUseCachePrefix() {
        return useCachePrefix;
    }

    public void setUseCachePrefix(boolean useCachePrefix) {
        this.useCachePrefix = useCachePrefix;
    }

    public RedisSerializerType getValueSerializer() {
        return valueSerializer;
    }

    public void setValueSerializer(RedisSerializerType valueSerializer) {
        this.valueSerializer = valueSerializer;
    }
}
