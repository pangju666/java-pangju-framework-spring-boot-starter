/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
