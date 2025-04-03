package io.github.pangju666.framework.autoconfigure.web.validation.repeater;

import io.github.pangju666.framework.autoconfigure.web.validation.annotation.Repeat;
import io.github.pangju666.framework.web.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;

public interface RequestRepeater {
	boolean tryAcquire(String key, Repeat repeat, HttpServletRequest request);

	default String generateKey(String key, String delimiter, Repeat annotation, HttpServletRequest request) {
		StringBuilder keyBuilder = new StringBuilder()
			.append(RequestUtils.getRequestPath(request))
			.append(delimiter)
			.append(request.getMethod());
		if (!annotation.global()) {
			keyBuilder
				.append(delimiter)
				.append(RequestUtils.getIpAddress(request));
		}
		keyBuilder
			.append(delimiter)
			.append(key);
		return keyBuilder.toString();
	}
}