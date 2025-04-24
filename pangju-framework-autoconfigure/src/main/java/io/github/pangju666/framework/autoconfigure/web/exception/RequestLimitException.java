package io.github.pangju666.framework.autoconfigure.web.exception;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.RateLimit;
import io.github.pangju666.framework.web.annotation.HttpException;
import io.github.pangju666.framework.web.enums.HttpExceptionType;
import io.github.pangju666.framework.web.exception.base.ValidationException;
import org.springframework.http.HttpStatus;

@HttpException(code = 410, type = HttpExceptionType.VALIDATION, log = false, status = HttpStatus.TOO_MANY_REQUESTS)
public class RequestLimitException extends ValidationException {
	public RequestLimitException(String message) {
		super(message);
	}

	public RequestLimitException(RateLimit annotation) {
		super(annotation.message());
	}
}
