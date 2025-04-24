package io.github.pangju666.framework.autoconfigure.web.limiter;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.RateLimit;
import io.github.pangju666.framework.web.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;

public interface RequestRateLimiter {
	boolean tryAcquire(@Nullable String key, RateLimit annotation, HttpServletRequest request);

	default String generateKey(RateLimit annotation, HttpServletRequest request, String delimiter) {
		StringBuilder keyBuilder = new StringBuilder()
			.append(request.getRequestURI())
			.append(delimiter)
			.append(request.getMethod());
		if (!annotation.global()) {
			keyBuilder
				.append(delimiter)
				.append(RequestUtils.getIpAddress(request));
		}
		return keyBuilder.toString();
	}
}
