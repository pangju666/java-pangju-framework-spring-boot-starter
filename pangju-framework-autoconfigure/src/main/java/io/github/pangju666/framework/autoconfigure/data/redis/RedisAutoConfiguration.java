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

package io.github.pangju666.framework.autoconfigure.data.redis;

import io.github.pangju666.framework.autoconfigure.data.redis.utils.RedisSerializerUtils;
import io.github.pangju666.framework.data.redis.bean.JavaScanRedisTemplate;
import io.github.pangju666.framework.data.redis.bean.JsonScanRedisTemplate;
import io.github.pangju666.framework.data.redis.bean.ScanRedisTemplate;
import io.github.pangju666.framework.data.redis.bean.StringScanRedisTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;

@AutoConfiguration(after = org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class)
@ConditionalOnClass({RedisOperations.class, ScanRedisTemplate.class})
@EnableConfigurationProperties(RedisProperties.class)
public class RedisAutoConfiguration {
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnSingleCandidate(RedisConnectionFactory.class)
	public StringScanRedisTemplate stringScanRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return new StringScanRedisTemplate(redisConnectionFactory);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnSingleCandidate(RedisConnectionFactory.class)
	public JavaScanRedisTemplate javaScanRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return new JavaScanRedisTemplate(redisConnectionFactory);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnSingleCandidate(RedisConnectionFactory.class)
	public JsonScanRedisTemplate jsonScanRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return new JsonScanRedisTemplate(redisConnectionFactory);
	}

	@Bean
	@ConditionalOnMissingBean(name = "scanRedisTemplate")
	@ConditionalOnSingleCandidate(RedisConnectionFactory.class)
	public ScanRedisTemplate<Object, Object> scanRedisTemplate(RedisProperties redisProperties,
															   RedisConnectionFactory redisConnectionFactory) {
		ScanRedisTemplate<Object, Object> redisTemplate = new ScanRedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(RedisSerializerUtils.createSerializer(redisProperties.getKeySerializer()));
		redisTemplate.setHashKeySerializer(RedisSerializerUtils.createSerializer(redisProperties.getHashKeySerializer()));
		redisTemplate.setValueSerializer(RedisSerializerUtils.createSerializer(redisProperties.getValueSerializer()));
		redisTemplate.setHashValueSerializer(RedisSerializerUtils.createSerializer(redisProperties.getHashValueSerializer()));
		return redisTemplate;
	}
}
