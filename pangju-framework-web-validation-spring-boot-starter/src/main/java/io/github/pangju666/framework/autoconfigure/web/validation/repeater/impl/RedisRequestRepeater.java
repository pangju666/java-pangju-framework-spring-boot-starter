package io.github.pangju666.framework.autoconfigure.web.validation.repeater.impl;

import io.github.pangju666.framework.autoconfigure.web.validation.annotation.Repeat;
import io.github.pangju666.framework.autoconfigure.web.validation.properties.RequestRepeatProperties;
import io.github.pangju666.framework.autoconfigure.web.validation.repeater.RequestRepeater;
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
