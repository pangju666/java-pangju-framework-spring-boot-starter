package io.github.pangju666.framework.autoconfigure.web.validation.exception;

import io.github.pangju666.framework.autoconfigure.web.validation.annotation.Repeat;
import io.github.pangju666.framework.web.exception.base.ServiceException;

public class RequestRepeatException extends ServiceException {
	public RequestRepeatException(Repeat annotation) {
		super(annotation.message());
		//this.setHttpStatus(HttpStatus.TOO_MANY_REQUESTS.value());
	}

	public RequestRepeatException(String message) {
		super(message);
		//this.setHttpStatus(HttpStatus.TOO_MANY_REQUESTS.value());
	}

	public RequestRepeatException(String message, Throwable cause) {
		super(message, cause);
		//this.setHttpStatus(HttpStatus.TOO_EARLY.value());
	}
}
