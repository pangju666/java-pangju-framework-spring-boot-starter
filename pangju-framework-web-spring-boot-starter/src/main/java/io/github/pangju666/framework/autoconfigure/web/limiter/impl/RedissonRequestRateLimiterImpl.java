package io.github.pangju666.framework.autoconfigure.web.limiter.impl;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.RateLimit;
import io.github.pangju666.framework.autoconfigure.web.limiter.RequestRateLimiter;
import io.github.pangju666.framework.autoconfigure.web.properties.RequestRateLimitProperties;
import io.github.pangju666.framework.core.lang.pool.ConstantPool;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class RedissonRequestRateLimiterImpl implements RequestRateLimiter {
	private static final Logger log = LoggerFactory.getLogger(RedissonRequestRateLimiterImpl.class);

	private final RequestRateLimitProperties properties;
	private final RedissonClient redissonClient;

	public RedissonRequestRateLimiterImpl(RequestRateLimitProperties properties, BeanFactory beanFactory) {
		this.properties = properties;
		if (StringUtils.isNotBlank(properties.getRedisson().getBeanName())) {
			this.redissonClient = beanFactory.getBean(properties.getRedisson().getBeanName(), RedissonClient.class);
		} else {
			this.redissonClient = beanFactory.getBean(RedissonClient.class);
		}
	}

	@Override
	public boolean tryAcquire(RateLimit annotation, HttpServletRequest request) {
		String key = generateKey(annotation, request, ConstantPool.REDIS_PATH_DELIMITER);
		if (StringUtils.isNotBlank(properties.getRedisson().getKeyPrefix())) {
			key = properties.getRedisson().getKeyPrefix() + ConstantPool.REDIS_PATH_DELIMITER + key;
		}
		RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
		if (!rateLimiter.isExists()) {
			if (!initRateLimiter(rateLimiter, annotation)) {
				log.error("redisson速率限制器初始化失败，key：{}", key);
			}
			long expireMillis = annotation.timeUnit().toMillis(annotation.interval());
			Duration expireDuration = properties.getRedisson().getExpire().plusMillis(expireMillis);
			redissonClient.getBucket(key).expire(expireDuration);
		}
		return rateLimiter.tryAcquire(1);
	}

	private boolean initRateLimiter(RRateLimiter rateLimiter, RateLimit rateLimit) {
		return switch (rateLimit.timeUnit()) {
			case DAYS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), rateLimit.interval(), RateIntervalUnit.DAYS);
			case HOURS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), rateLimit.interval(), RateIntervalUnit.HOURS);
			case MINUTES ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), rateLimit.interval(), RateIntervalUnit.MINUTES);
			case SECONDS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), rateLimit.interval(), RateIntervalUnit.SECONDS);
			case MILLISECONDS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), rateLimit.interval(), RateIntervalUnit.MILLISECONDS);
			case MICROSECONDS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), TimeUnit.MICROSECONDS.toMillis(rateLimit.interval()), RateIntervalUnit.MILLISECONDS);
			case NANOSECONDS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), TimeUnit.NANOSECONDS.toMillis(rateLimit.interval()), RateIntervalUnit.MILLISECONDS);
		};
	}
}
