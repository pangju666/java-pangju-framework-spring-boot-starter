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

package io.github.pangju666.framework.autoconfigure.web.limiter.limiter.impl;

import io.github.pangju666.framework.autoconfigure.web.limiter.annotation.RateLimit;
import io.github.pangju666.framework.autoconfigure.web.limiter.limiter.RequestRateLimiter;
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
