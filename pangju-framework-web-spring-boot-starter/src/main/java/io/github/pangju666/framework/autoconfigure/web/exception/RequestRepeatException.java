package io.github.pangju666.framework.autoconfigure.web.exception;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.RequestRepeat;
import io.github.pangju666.framework.core.exception.base.ServiceException;
import org.springframework.http.HttpStatus;

public class RequestRepeatException extends ServiceException {
	public RequestRepeatException(RequestRepeat annotation) {
		super(-41200, annotation.message(), HttpStatus.TOO_MANY_REQUESTS.value());
	}

	public RequestRepeatException(String message) {
		super(-41200, message, HttpStatus.TOO_MANY_REQUESTS.value());
	}

	public RequestRepeatException(String message, Throwable cause) {
		super(-41200, message, HttpStatus.TOO_MANY_REQUESTS.value(), cause);
	}
}
