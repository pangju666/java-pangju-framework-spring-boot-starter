package io.github.pangju666.framework.autoconfigure.web.exception;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.RequestLimit;
import io.github.pangju666.framework.core.exception.base.ServiceException;
import io.github.pangju666.framework.core.lang.pool.ConstantPool;
import org.springframework.http.HttpStatus;

public class RequestLimitException extends ServiceException {
	public RequestLimitException(RequestLimit annotation) {
		super(ConstantPool.VALIDATION_ERROR_RESPONSE_CODE, annotation.message(), HttpStatus.TOO_MANY_REQUESTS.value());
	}

	public RequestLimitException(String message) {
		super(ConstantPool.VALIDATION_ERROR_RESPONSE_CODE, message, HttpStatus.TOO_MANY_REQUESTS.value());
	}

	public RequestLimitException(String message, Throwable cause) {
		super(ConstantPool.VALIDATION_ERROR_RESPONSE_CODE, message, HttpStatus.TOO_MANY_REQUESTS.value(), cause);
	}
}
