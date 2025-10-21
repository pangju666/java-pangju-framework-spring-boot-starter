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

import io.github.pangju666.framework.autoconfigure.cache.hash.aspect.HashCacheAspect;
import io.github.pangju666.framework.autoconfigure.cache.hash.redis.RedisHashCacheManager;
import io.github.pangju666.framework.data.redis.utils.RedisUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@AutoConfiguration(after = {RedisAutoConfiguration.class, AopAutoConfiguration.class})
@EnableConfigurationProperties(HashCacheProperties.class)
public class HashCacheAutoConfiguration {
	@Bean
	@ConditionalOnBean(HashCacheManager.class)
	public HashCacheAspect hashCacheAspect(HashCacheManager hashCacheManager) {
		return new HashCacheAspect(hashCacheManager);
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(RedisOperations.class)
	@ConditionalOnProperty(prefix = "pangju.cache.hash", value = "type", havingValue = "REDIS", matchIfMissing = true)
	static class RedisHashCacheConfiguration {
		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnSingleCandidate(RedisConnectionFactory.class)
		public HashCacheManager hashCacheManager(RedisConnectionFactory redisConnectionFactory, HashCacheProperties properties) {
			RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
			redisTemplate.setConnectionFactory(redisConnectionFactory);
			redisTemplate.setKeySerializer(RedisSerializer.string());
			redisTemplate.setValueSerializer(RedisUtils.getSerializer(
				properties.getRedis().getValueSerializer()));
			redisTemplate.setHashKeySerializer(RedisSerializer.string());
			redisTemplate.setHashValueSerializer(RedisUtils.getSerializer(
				properties.getRedis().getValueSerializer()));
			redisTemplate.afterPropertiesSet();
			return new RedisHashCacheManager(redisTemplate, properties.getRedis().getKeyPrefix(),
				properties.getRedis().isCacheNullValues());
		}
	}
}
