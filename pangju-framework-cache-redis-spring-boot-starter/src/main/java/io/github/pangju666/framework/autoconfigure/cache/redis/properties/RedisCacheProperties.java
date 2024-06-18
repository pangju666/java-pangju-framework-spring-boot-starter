package io.github.pangju666.framework.autoconfigure.cache.redis.properties;

import io.github.pangju666.framework.autoconfigure.cache.redis.enums.RedisSerializerType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pangju.cache.redis")
public class RedisCacheProperties {
    private boolean cacheNullValues = true;
    private String keyPrefix;
    private boolean useKeyPrefix = true;
    private RedisSerializerType valueSerializer = RedisSerializerType.JSON;
    private RedisSerializerType hashValueSerializer = RedisSerializerType.JSON;

    public boolean isCacheNullValues() {
        return cacheNullValues;
    }

    public void setCacheNullValues(boolean cacheNullValues) {
        this.cacheNullValues = cacheNullValues;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public boolean isUseKeyPrefix() {
        return useKeyPrefix;
    }

    public void setUseKeyPrefix(boolean useKeyPrefix) {
        this.useKeyPrefix = useKeyPrefix;
    }

    public RedisSerializerType getValueSerializer() {
        return valueSerializer;
    }

    public void setValueSerializer(RedisSerializerType valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    public RedisSerializerType getHashValueSerializer() {
        return hashValueSerializer;
    }

    public void setHashValueSerializer(RedisSerializerType hashValueSerializer) {
        this.hashValueSerializer = hashValueSerializer;
    }
}
