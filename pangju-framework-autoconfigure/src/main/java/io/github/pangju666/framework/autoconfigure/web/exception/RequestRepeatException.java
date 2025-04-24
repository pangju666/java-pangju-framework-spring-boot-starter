package io.github.pangju666.framework.autoconfigure.web.exception;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.Repeat;
import io.github.pangju666.framework.web.annotation.HttpException;
import io.github.pangju666.framework.web.enums.HttpExceptionType;
import io.github.pangju666.framework.web.exception.base.ValidationException;
import org.springframework.http.HttpStatus;

@HttpException(code = 420, type = HttpExceptionType.VALIDATION, log = false, status = HttpStatus.TOO_MANY_REQUESTS)
public class RequestRepeatException extends ValidationException {
	public RequestRepeatException(String message) {
		super(message);
	}

	public RequestRepeatException(Repeat annotation) {
		super(annotation.message());
	}
}
