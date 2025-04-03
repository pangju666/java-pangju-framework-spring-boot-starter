package io.github.pangju666.framework.autoconfigure.web.limiter.impl;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.RateLimit;
import io.github.pangju666.framework.autoconfigure.web.limiter.RequestRateLimiter;
import io.github.pangju666.framework.autoconfigure.web.properties.RequestRateLimitProperties;
import io.github.pangju666.framework.data.redis.lang.pool.RedisConstants;
import io.github.pangju666.framework.web.exception.base.ServerException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.BeanFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public class RedissonRequestRateLimiterImpl implements RequestRateLimiter {
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
	public boolean tryAcquire(String key, RateLimit annotation, HttpServletRequest request) {
		String rateLimitKey = key;
		if (StringUtils.isBlank(rateLimitKey)) {
			rateLimitKey = generateKey(annotation, request, RedisConstants.REDIS_PATH_DELIMITER);
		}
		if (StringUtils.isNotBlank(properties.getRedisson().getKeyPrefix())) {
			rateLimitKey = StringUtils.join(Arrays.asList(properties.getRedisson().getKeyPrefix(), rateLimitKey),
				RedisConstants.REDIS_PATH_DELIMITER);
		}
		RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimitKey);
		if (!rateLimiter.isExists()) {
			if (!initRateLimiter(rateLimiter, annotation)) {
				throw new ServerException("redisson速率限制器初始化失败，key：%s".formatted(rateLimitKey));
			}
			long expireMillis = annotation.timeUnit().toMillis(annotation.interval());
			Duration expireDuration = properties.getRedisson().getExpire().plusMillis(expireMillis);
			redissonClient.getBucket(rateLimitKey).expire(expireDuration);
		}
		return rateLimiter.tryAcquire(1);
	}

	private boolean initRateLimiter(RRateLimiter rateLimiter, RateLimit rateLimit) {
		return switch (rateLimit.timeUnit()) {
			case DAYS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), Duration.ofDays(rateLimit.interval()));
			case HOURS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), Duration.ofHours(rateLimit.interval()));
			case MINUTES ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), Duration.ofMinutes(rateLimit.interval()));
			case SECONDS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), Duration.ofSeconds(rateLimit.interval()));
			case MILLISECONDS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), Duration.ofMillis(rateLimit.interval()));
			case MICROSECONDS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), Duration.of(rateLimit.interval(), ChronoUnit.NANOS));
			case NANOSECONDS ->
				rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), Duration.ofNanos(rateLimit.interval()));
		};
	}
}
