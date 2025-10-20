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

package io.github.pangju666.framework.autoconfigure.web.limiter.interceptor;

import io.github.pangju666.framework.autoconfigure.web.limiter.annotation.RateLimit;
import io.github.pangju666.framework.autoconfigure.web.limiter.enums.RateLimitMethod;
import io.github.pangju666.framework.autoconfigure.web.limiter.exception.RequestLimitException;
import io.github.pangju666.framework.autoconfigure.web.limiter.handler.RequestRateLimiter;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.interceptor.BaseHttpHandlerInterceptor;
import io.github.pangju666.framework.web.utils.ServletResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;

import java.util.Collections;
import java.util.Objects;

public class RequestRateLimitInterceptor extends BaseHttpHandlerInterceptor {
	private final RequestRateLimiter requestRateLimiter;

	public RequestRateLimitInterceptor(RequestRateLimiter requestLimitHandlers) {
		super(Collections.singleton("/**"), Collections.emptySet());
		this.requestRateLimiter = requestLimitHandlers;
	}

	@Override
	public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
		if (handler instanceof HandlerMethod handlerMethod) {
			RateLimit annotation = handlerMethod.getMethodAnnotation(RateLimit.class);
			if (Objects.isNull(annotation)) {
				Class<?> targetClass = handlerMethod.getBeanType();
				annotation = targetClass.getAnnotation(RateLimit.class);
			}
			if (Objects.isNull(annotation)) {
				return true;
			}
			if (annotation.method() != RateLimitMethod.REQUEST) {
				return true;
			}

			boolean result;
			try {
				result = requestRateLimiter.tryAcquire(annotation.key(), annotation, request);
			} catch (Exception e) {
				ServletResponseUtils.writeHttpExceptionToResponse(new ServerException(e), response);
				return false;
			}
			if (!result) {
				ServletResponseUtils.writeHttpExceptionToResponse(new RequestLimitException(annotation), response);
				return false;
			}
		}
		return true;
	}
}
