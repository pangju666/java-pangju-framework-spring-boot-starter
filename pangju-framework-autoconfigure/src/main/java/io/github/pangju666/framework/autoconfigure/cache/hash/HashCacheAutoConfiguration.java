package io.github.pangju666.framework.autoconfigure.cache.hash;

import io.github.pangju666.framework.autoconfigure.cache.hash.aspect.HashCacheAspect;
import io.github.pangju666.framework.autoconfigure.cache.hash.redis.RedisHashCacheConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration(after = AopAutoConfiguration.class)
@ConditionalOnProperty(value = "pangju.cache.hash")
@Import(RedisHashCacheConfiguration.class)
@EnableConfigurationProperties(HashCacheProperties.class)
public class HashCacheAutoConfiguration {
	@Bean
	@ConditionalOnBean(HashCacheManager.class)
	public HashCacheAspect hashCacheAspect(HashCacheManager hashCacheManager) {
		return new HashCacheAspect(hashCacheManager);
	}
}
