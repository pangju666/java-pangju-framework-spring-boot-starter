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

package io.github.pangju666.framework.autoconfigure.web.repeater.handler.impl;

import io.github.pangju666.framework.autoconfigure.web.repeater.RequestRepeatProperties;
import io.github.pangju666.framework.autoconfigure.web.repeater.annotation.Repeat;
import io.github.pangju666.framework.autoconfigure.web.repeater.handler.RequestRepeater;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;

public class RedisRequestRepeater implements RequestRepeater {
	private static final String REDIS_PATH_DELIMITER = "::";

	private final RequestRepeatProperties properties;
	private final RedisTemplate<String, Object> redisTemplate;

	@SuppressWarnings("unchecked")
	public RedisRequestRepeater(RequestRepeatProperties properties, BeanFactory beanFactory) {
		this.properties = properties;
		if (StringUtils.isNotBlank(properties.getRedis().getBeanName())) {
			this.redisTemplate = beanFactory.getBean(properties.getRedis().getBeanName(), RedisTemplate.class);
		} else {
			this.redisTemplate = beanFactory.getBean("redisTemplate", RedisTemplate.class);
		}
	}

	@Override
	public boolean tryAcquire(String key, Repeat repeat, HttpServletRequest request) {
		String repeatKey = generateKey(key, REDIS_PATH_DELIMITER, repeat, request);
		if (StringUtils.isNotBlank(properties.getRedis().getKeyPrefix())) {
			repeatKey = StringUtils.join(Arrays.asList(properties.getRedis().getKeyPrefix(), repeatKey),
				REDIS_PATH_DELIMITER);
		}
		if (Boolean.TRUE.equals(redisTemplate.hasKey(repeatKey))) {
			return false;
		}
		redisTemplate.opsForValue().set(repeatKey, Boolean.TRUE, repeat.interval(), repeat.timeUnit());
		return true;
	}
}
