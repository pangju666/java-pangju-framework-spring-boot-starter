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

package io.github.pangju666.framework.autoconfigure.web.limiter.handler;

import io.github.pangju666.framework.autoconfigure.web.limiter.annotation.RateLimit;
import io.github.pangju666.framework.web.utils.ServletRequestUtils;
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
				.append(ServletRequestUtils.getIpAddress(request));
		}
		return keyBuilder.toString();
	}
}
