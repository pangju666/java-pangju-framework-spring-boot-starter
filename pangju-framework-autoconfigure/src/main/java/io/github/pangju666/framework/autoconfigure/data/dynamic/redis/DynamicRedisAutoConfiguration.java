package io.github.pangju666.framework.autoconfigure.data.dynamic.redis;

import io.github.pangju666.framework.autoconfigure.data.dynamic.redis.properties.DynamicRedisProperties;
import io.github.pangju666.framework.autoconfigure.data.dynamic.redis.registrar.DynamicRedisRegistrar;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisOperations;

@AutoConfiguration(before = RedisAutoConfiguration.class, after = ClientResourcesAutoConfiguration.class)
@ConditionalOnClass(RedisOperations.class)
@EnableConfigurationProperties(DynamicRedisProperties.class)
@Import(DynamicRedisRegistrar.class)
public class DynamicRedisAutoConfiguration {
}
