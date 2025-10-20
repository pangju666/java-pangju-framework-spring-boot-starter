package io.github.pangju666.framework.autoconfigure.web.limit.limiter.impl;

import io.github.pangju666.framework.autoconfigure.web.limit.RateLimit;
import io.github.pangju666.framework.autoconfigure.web.limit.limiter.RequestRateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

public class Resilience4JRequestRateLimiterImpl implements RequestRateLimiter {
	private final RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();

	public Resilience4JRequestRateLimiterImpl() {
	}

	@Override
	public boolean tryAcquire(String key, RateLimit annotation, HttpServletRequest request) {
		String rateLimitKey = key;
		if (StringUtils.isBlank(rateLimitKey)) {
			rateLimitKey = generateKey(annotation, request, "_");
		}
		long refreshMillis = annotation.timeUnit().toMillis(annotation.interval());
		RateLimiterConfig config = RateLimiterConfig.custom()
			.limitRefreshPeriod(Duration.ofMillis(refreshMillis))
			.limitForPeriod(annotation.rate())
			.timeoutDuration(Duration.ZERO)
			.build();
		return rateLimiterRegistry.rateLimiter(rateLimitKey, config).acquirePermission();
	}
}
