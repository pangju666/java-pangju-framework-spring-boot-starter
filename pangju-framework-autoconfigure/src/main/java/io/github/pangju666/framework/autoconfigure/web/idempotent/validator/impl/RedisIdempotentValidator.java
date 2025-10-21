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

package io.github.pangju666.framework.autoconfigure.web.idempotent.validator.impl;

import io.github.pangju666.framework.autoconfigure.web.idempotent.IdempotentProperties;
import io.github.pangju666.framework.autoconfigure.web.idempotent.annotation.Idempotent;
import io.github.pangju666.framework.autoconfigure.web.idempotent.validator.IdempotentValidator;
import io.github.pangju666.framework.data.redis.pool.RedisConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisIdempotentValidator implements IdempotentValidator {
	private final IdempotentProperties properties;
	private final RedisTemplate<String, Object> redisTemplate;

	@SuppressWarnings("unchecked")
	public RedisIdempotentValidator(IdempotentProperties properties, BeanFactory beanFactory) {
		this.properties = properties;
		if (StringUtils.isNotBlank(properties.getRedis().getRedisTemplateBeanName())) {
			this.redisTemplate = beanFactory.getBean(properties.getRedis().getRedisTemplateBeanName(), RedisTemplate.class);
		} else {
			this.redisTemplate = beanFactory.getBean("redisTemplate", RedisTemplate.class);
		}
	}

	@Override
	public boolean validate(String key, Idempotent repeat) {
		String repeatKey = key;
		if (StringUtils.isNotBlank(properties.getRedis().getKeyPrefix())) {
			repeatKey = properties.getRedis().getKeyPrefix() + RedisConstants.REDIS_PATH_DELIMITER + repeatKey;
		}
		Boolean acquired = redisTemplate.opsForValue().setIfAbsent(repeatKey, true, repeat.interval(), repeat.timeUnit());
		return !Boolean.FALSE.equals(acquired);
	}

	@Override
	public void remove(String key, Idempotent repeat) {
		redisTemplate.delete(key);
	}
}
