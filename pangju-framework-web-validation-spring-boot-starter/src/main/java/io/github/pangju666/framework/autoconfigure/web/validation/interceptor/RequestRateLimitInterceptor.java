package io.github.pangju666.framework.autoconfigure.web.validation.interceptor;

import io.github.pangju666.framework.autoconfigure.web.validation.annotation.RateLimit;
import io.github.pangju666.framework.autoconfigure.web.validation.enums.RateLimitMethod;
import io.github.pangju666.framework.autoconfigure.web.validation.exception.RequestLimitException;
import io.github.pangju666.framework.autoconfigure.web.validation.limiter.RequestRateLimiter;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.interceptor.BaseHttpHandlerInterceptor;
import io.github.pangju666.framework.web.utils.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;

import java.util.Objects;

public class RequestRateLimitInterceptor extends BaseHttpHandlerInterceptor {
	private final RequestRateLimiter requestRateLimiter;

	public RequestRateLimitInterceptor(RequestRateLimiter requestLimitHandlers) {
		super(null, null);
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
				ResponseUtils.writeHttpExceptionToResponse(new ServerException(e), response);
				return false;
			}
			if (!result) {
				ResponseUtils.writeHttpExceptionToResponse(new RequestLimitException(annotation), response);
				return false;
			}
		}
		return true;
	}
}
