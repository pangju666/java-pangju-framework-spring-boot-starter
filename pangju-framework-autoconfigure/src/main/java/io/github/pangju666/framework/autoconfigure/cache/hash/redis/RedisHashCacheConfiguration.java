package io.github.pangju666.framework.autoconfigure.cache.hash.redis;

import io.github.pangju666.framework.autoconfigure.cache.hash.HashCacheManager;
import io.github.pangju666.framework.autoconfigure.cache.hash.HashCacheProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisConnectionFactory.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@ConditionalOnBean(RedisConnectionFactory.class)
@ConditionalOnMissingBean(HashCacheManager.class)
@ConditionalOnProperty(prefix = "pangju.cache.hash", value = "type", havingValue = "REDIS")
public class RedisHashCacheConfiguration {
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnSingleCandidate(RedisConnectionFactory.class)
	public HashCacheManager hashCacheManager(RedisConnectionFactory redisConnectionFactory,
											 HashCacheProperties hashCacheProperties) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(RedisSerializer.string());
		redisTemplate.setValueSerializer(hashCacheProperties.getRedis().getValueSerializer().getSerializer());
		redisTemplate.setHashKeySerializer(RedisSerializer.string());
		redisTemplate.setHashValueSerializer(hashCacheProperties.getRedis().getValueSerializer().getSerializer());
		redisTemplate.afterPropertiesSet();
		return new RedisHashCacheManager(redisTemplate, hashCacheProperties);
	}
}