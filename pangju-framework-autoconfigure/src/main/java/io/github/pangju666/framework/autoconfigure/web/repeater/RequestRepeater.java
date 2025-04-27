package io.github.pangju666.framework.autoconfigure.web.repeater;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.Repeat;
import io.github.pangju666.framework.web.utils.ServletRequestUtils;
import jakarta.servlet.http.HttpServletRequest;

public interface RequestRepeater {
	boolean tryAcquire(String key, Repeat repeat, HttpServletRequest request);

	default String generateKey(String key, String delimiter, Repeat annotation, HttpServletRequest request) {
		StringBuilder keyBuilder = new StringBuilder()
			.append(request.getRequestURI())
			.append(delimiter)
			.append(request.getMethod());
		if (!annotation.global()) {
			keyBuilder
				.append(delimiter)
				.append(ServletRequestUtils.getIpAddress(request));
		}
		keyBuilder
			.append(delimiter)
			.append(key);
		return keyBuilder.toString();
	}
}