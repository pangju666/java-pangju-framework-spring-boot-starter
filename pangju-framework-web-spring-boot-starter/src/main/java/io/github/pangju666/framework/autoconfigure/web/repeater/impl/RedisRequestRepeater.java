package io.github.pangju666.framework.autoconfigure.web.repeater.impl;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.Repeat;
import io.github.pangju666.framework.autoconfigure.web.properties.RequestRepeatProperties;
import io.github.pangju666.framework.autoconfigure.web.repeater.RequestRepeater;
import io.github.pangju666.framework.core.lang.pool.ConstantPool;
import io.github.pangju666.framework.data.redis.utils.RedisUtils;
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
		if (StringUtils.isNotBlank(properties.getRedis().getBeanName())) {
			this.redisTemplate = beanFactory.getBean(properties.getRedis().getBeanName(), RedisTemplate.class);
		} else {
			this.redisTemplate = beanFactory.getBean("redisTemplate", RedisTemplate.class);
		}
	}

	@Override
	public boolean tryAcquire(String key, Repeat repeat, HttpServletRequest request) {
		String repeatKey = generateKey(key, repeat, request);
		if (StringUtils.isNotBlank(properties.getRedis().getKeyPrefix())) {
			repeatKey = RedisUtils.computeKey(properties.getRedis().getKeyPrefix(), repeatKey);
		}
		if (Boolean.TRUE.equals(redisTemplate.hasKey(repeatKey))) {
			return false;
		}
		redisTemplate.opsForValue().set(repeatKey, Boolean.TRUE, repeat.interval(), repeat.timeUnit());
		return true;
	}
}
