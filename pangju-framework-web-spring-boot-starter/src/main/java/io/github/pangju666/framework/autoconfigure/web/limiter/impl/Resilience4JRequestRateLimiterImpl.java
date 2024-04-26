package io.github.pangju666.framework.autoconfigure.web.limiter.impl;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.RateLimit;
import io.github.pangju666.framework.autoconfigure.web.limiter.RequestRateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Duration;

public class Resilience4JRequestRateLimiterImpl implements RequestRateLimiter {
	private final RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();

	public Resilience4JRequestRateLimiterImpl() {
	}

	@Override
	public boolean tryAcquire(RateLimit annotation, HttpServletRequest request) {
		String key = generateKey(annotation, request, "_");
		long refreshMillis = annotation.timeUnit().toMillis(annotation.interval());
		RateLimiterConfig config = RateLimiterConfig.custom()
			.limitRefreshPeriod(Duration.ofMillis(refreshMillis))
			.limitForPeriod(annotation.rate())
			.timeoutDuration(Duration.ZERO)
			.build();
		return rateLimiterRegistry.rateLimiter(key, config).acquirePermission();
	}
}
