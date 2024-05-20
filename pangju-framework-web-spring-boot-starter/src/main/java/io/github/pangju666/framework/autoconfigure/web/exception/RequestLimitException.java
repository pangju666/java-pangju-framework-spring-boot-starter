package io.github.pangju666.framework.autoconfigure.web.exception;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.RateLimit;
import io.github.pangju666.framework.core.exception.base.ServiceException;
import org.springframework.http.HttpStatus;

public class RequestLimitException extends ServiceException {
	public RequestLimitException(RateLimit annotation) {
		super(annotation.message());
		this.setHttpStatus(HttpStatus.TOO_MANY_REQUESTS.value());
	}

	public RequestLimitException(String message) {
		super(message);
		this.setHttpStatus(HttpStatus.TOO_MANY_REQUESTS.value());
	}

	public RequestLimitException(String message, Throwable cause) {
		super(message, cause);
		this.setHttpStatus(HttpStatus.TOO_MANY_REQUESTS.value());
	}
}
