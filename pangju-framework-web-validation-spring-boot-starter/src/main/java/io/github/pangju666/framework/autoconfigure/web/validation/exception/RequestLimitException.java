package io.github.pangju666.framework.autoconfigure.web.validation.exception;

import io.github.pangju666.framework.autoconfigure.web.validation.annotation.RateLimit;
import io.github.pangju666.framework.web.exception.base.ServiceException;

public class RequestLimitException extends ServiceException {
	public RequestLimitException(RateLimit annotation) {
		super(annotation.message());
		//this.setHttpStatus(HttpStatus.TOO_MANY_REQUESTS.value());
	}

	public RequestLimitException(String message) {
		super(message);
		//this.setHttpStatus(HttpStatus.TOO_MANY_REQUESTS.value());
	}

	public RequestLimitException(String message, Throwable cause) {
		super(message, cause);
		//this.setHttpStatus(HttpStatus.TOO_MANY_REQUESTS.value());
	}
}
