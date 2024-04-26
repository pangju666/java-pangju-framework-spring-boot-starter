package io.github.pangju666.framework.autoconfigure.web.interceptor;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.Repeat;
import io.github.pangju666.framework.autoconfigure.web.exception.RequestRepeatException;
import io.github.pangju666.framework.autoconfigure.web.repeater.RequestRepeater;
import io.github.pangju666.framework.web.interceptor.BaseRequestInterceptor;
import io.github.pangju666.framework.web.utils.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;

import java.util.Objects;

public class RequestRepeatInterceptor extends BaseRequestInterceptor {
	private final RequestRepeater requestRepeater;

	public RequestRepeatInterceptor(RequestRepeater requestRepeater) {
		this.requestRepeater = requestRepeater;
	}

	@Override
	public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
		if (handler instanceof HandlerMethod handlerMethod) {
			Repeat annotation = handlerMethod.getMethodAnnotation(Repeat.class);
			if (Objects.isNull(annotation)) {
				return true;
			}
			if (!requestRepeater.tryAcquire(annotation, request)) {
				ResponseUtils.writeExceptionToResponse(new RequestRepeatException(annotation), response);
				return false;
			}
		}
		return true;
	}
}
