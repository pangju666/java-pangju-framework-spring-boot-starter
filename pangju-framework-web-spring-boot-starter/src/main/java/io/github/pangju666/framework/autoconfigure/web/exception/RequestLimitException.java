package io.github.pangju666.framework.autoconfigure.web.exception;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.RequestLimit;
import io.github.pangju666.framework.core.exception.base.ServiceException;
import org.springframework.http.HttpStatus;

public class RequestLimitException extends ServiceException {
	public RequestLimitException(RequestLimit annotation) {
		super(-41100, annotation.message(), HttpStatus.TOO_MANY_REQUESTS.value());
	}

	public RequestLimitException(String message) {
		super(-41100, message, HttpStatus.TOO_MANY_REQUESTS.value());
	}

	public RequestLimitException(String message, Throwable cause) {
		super(-41100, message, HttpStatus.TOO_MANY_REQUESTS.value(), cause);
	}
}
