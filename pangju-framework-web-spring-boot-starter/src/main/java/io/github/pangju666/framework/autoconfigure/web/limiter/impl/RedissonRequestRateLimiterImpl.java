package io.github.pangju666.framework.autoconfigure.web.limiter.impl;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.RateLimit;
import io.github.pangju666.framework.autoconfigure.web.limiter.RequestRateLimiter;
import io.github.pangju666.framework.autoconfigure.web.properties.RequestRateLimitProperties;
import io.github.pangju666.framework.core.lang.pool.Constants;
import io.github.pangju666.framework.data.redis.utils.RedisUtils;
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
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
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
	public boolean tryAcquire(String key, RateLimit annotation, HttpServletRequest request) {
		String rateLimitKey = key;
		if (StringUtils.isBlank(rateLimitKey)) {
			rateLimitKey = generateKey(annotation, request, Constants.REDIS_PATH_DELIMITER);
			if (StringUtils.isNotBlank(properties.getRedisson().getKeyPrefix())) {
				rateLimitKey = RedisUtils.computeKey(properties.getRedisson().getKeyPrefix(), rateLimitKey);
			}
		}
		if (StringUtils.isNotBlank(properties.getRedisson().getKeyPrefix())) {
			rateLimitKey = RedisUtils.computeKey(properties.getRedisson().getKeyPrefix(), rateLimitKey);
		}
		RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimitKey);
		if (!rateLimiter.isExists()) {
			if (!initRateLimiter(rateLimiter, annotation)) {
				log.error("redisson速率限制器初始化失败，key：{}", rateLimitKey);
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
