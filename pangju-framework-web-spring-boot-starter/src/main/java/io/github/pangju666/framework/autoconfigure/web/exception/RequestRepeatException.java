package io.github.pangju666.framework.autoconfigure.web.exception;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.RequestRepeat;
import io.github.pangju666.framework.core.exception.base.ServiceException;
import io.github.pangju666.framework.core.lang.pool.ConstantPool;
import org.springframework.http.HttpStatus;

public class RequestRepeatException extends ServiceException {
	public RequestRepeatException(RequestRepeat annotation) {
		super(ConstantPool.VALIDATION_ERROR_RESPONSE_CODE, annotation.message(), HttpStatus.TOO_MANY_REQUESTS.value());
	}

	public RequestRepeatException(String message) {
		super(ConstantPool.VALIDATION_ERROR_RESPONSE_CODE, message, HttpStatus.TOO_MANY_REQUESTS.value());
	}

	public RequestRepeatException(String message, Throwable cause) {
		super(ConstantPool.VALIDATION_ERROR_RESPONSE_CODE, message, HttpStatus.TOO_MANY_REQUESTS.value(), cause);
	}
}
