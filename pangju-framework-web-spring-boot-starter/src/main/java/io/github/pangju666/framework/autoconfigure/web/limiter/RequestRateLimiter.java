package io.github.pangju666.framework.autoconfigure.web.limiter;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.RateLimit;
import io.github.pangju666.framework.web.utils.RequestUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

public interface RequestRateLimiter {
	boolean tryAcquire(RateLimit annotation, HttpServletRequest request);

	default String generateKey(RateLimit annotation, HttpServletRequest request, String delimiter) {
		if (StringUtils.isNotBlank(annotation.key())) {
			return annotation.key();
		}
		StringBuilder keyBuilder = new StringBuilder()
			.append(request.getMethod())
			.append(delimiter)
			.append(RequestUtils.getRequestPath(request));
		if (!annotation.global()) {
			keyBuilder.append(delimiter);
			keyBuilder.append(RequestUtils.getIpAddress(request));
		}
		return keyBuilder.toString();
	}
}
