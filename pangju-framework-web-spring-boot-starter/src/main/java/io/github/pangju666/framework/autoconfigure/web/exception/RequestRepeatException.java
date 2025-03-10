package io.github.pangju666.framework.autoconfigure.web.exception;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.Repeat;
import io.github.pangju666.framework.core.exception.base.ServiceException;
import org.springframework.http.HttpStatus;

public class RequestRepeatException extends ServiceException {
	public RequestRepeatException(Repeat annotation) {
		super(annotation.message());
		this.setHttpStatus(HttpStatus.TOO_MANY_REQUESTS.value());
	}

	public RequestRepeatException(String message) {
		super(message);
		this.setHttpStatus(HttpStatus.TOO_MANY_REQUESTS.value());
	}

	public RequestRepeatException(String message, Throwable cause) {
		super(message, cause);
		this.setHttpStatus(HttpStatus.TOO_EARLY.value());
	}
}
