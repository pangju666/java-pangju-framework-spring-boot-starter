package io.github.pangju666.framework.autoconfigure.cache;

import io.github.pangju666.framework.autoconfigure.cache.registrar.DynamicRedisCacheManagerRegistrar;
import io.github.pangju666.framework.autoconfigure.data.redis.DynamicRedisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheAspectSupport;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@AutoConfiguration(before = CacheAutoConfiguration.class, after = DynamicRedisAutoConfiguration.class)
@ConditionalOnClass({RedisConnectionFactory.class, CacheManager.class})
@ConditionalOnBean({RedisConnectionFactory.class, CacheAspectSupport.class})
@ConditionalOnProperty(prefix = "spring.cache", name = "type", havingValue = "REDIS")
@EnableConfigurationProperties(CacheProperties.class)
@Import(DynamicRedisCacheManagerRegistrar.class)
public class DynamicRedisCacheAutoConfiguration {
}
