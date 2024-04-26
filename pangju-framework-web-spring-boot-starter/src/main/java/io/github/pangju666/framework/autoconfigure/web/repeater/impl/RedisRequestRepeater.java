package io.github.pangju666.framework.autoconfigure.web.repeater.impl;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.Repeat;
import io.github.pangju666.framework.autoconfigure.web.properties.RequestRepeatProperties;
import io.github.pangju666.framework.autoconfigure.web.repeater.RequestRepeater;
import io.github.pangju666.framework.core.lang.pool.ConstantPool;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisRequestRepeater extends RequestRepeater {
	private final RequestRepeatProperties properties;
	private final RedisTemplate<String, Object> redisTemplate;

	@SuppressWarnings("unchecked")
	public RedisRequestRepeater(RequestRepeatProperties properties, BeanFactory beanFactory) {
		super(ConstantPool.REDIS_PATH_DELIMITER);
		this.properties = properties;
		if (StringUtils.isNotBlank(properties.getRedis().getTemplateBeanName())) {
			this.redisTemplate = beanFactory.getBean(properties.getRedis().getTemplateBeanName(), RedisTemplate.class);
		} else {
			this.redisTemplate = beanFactory.getBean(RedisTemplate.class);
		}
	}

	@Override
	public boolean tryAcquire(Repeat repeat, HttpServletRequest request) {
		String key = generateKey(repeat, request);
		if (StringUtils.isNotBlank(properties.getRedis().getKeyPrefix())) {
			key = properties.getRedis().getKeyPrefix() + ConstantPool.REDIS_PATH_DELIMITER + key;
		}
		if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
			return false;
		}
		redisTemplate.opsForValue().set(key, Boolean.TRUE, repeat.interval(), repeat.timeUnit());
		return true;
	}
}
