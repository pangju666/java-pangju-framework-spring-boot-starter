package io.github.pangju666.framework.autoconfigure.cache;

import io.github.pangju666.framework.autoconfigure.cache.registrar.DynamicRedisCacheManagerRegistrar;
import io.github.pangju666.framework.autoconfigure.data.redis.DynamicRedisAutoConfiguration;
import io.github.pangju666.framework.autoconfigure.data.redis.properties.DynamicRedisProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

@AutoConfiguration(before = CacheAutoConfiguration.class, after = DynamicRedisAutoConfiguration.class)
@ConditionalOnClass(CacheManager.class)
@ConditionalOnProperty(prefix = DynamicRedisProperties.PREFIX, name = "enabled", havingValue = "true")
@EnableConfigurationProperties(CacheProperties.class)
@Import(DynamicRedisCacheManagerRegistrar.class)
public class DynamicRedisCacheAutoConfiguration {
}
