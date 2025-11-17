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

package io.github.pangju666.framework.boot.autoconfigure.web.advice.exception;

import io.github.pangju666.framework.boot.image.exception.ImageDamageException;
import io.github.pangju666.framework.boot.image.exception.UnSupportImageTypeException;
import io.github.pangju666.framework.boot.image.exception.UncheckedGMException;
import io.github.pangju666.framework.boot.image.exception.UncheckedGMServiceException;
import io.github.pangju666.framework.web.model.Result;
import jakarta.servlet.Servlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;

@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, Result.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.advice", value = "exception", matchIfMissing = true)
@RestControllerAdvice
public class GlobalImageExceptionAdvice {
	/**
	 * 日志记录器
	 *
	 * @since 1.0.0
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalImageExceptionAdvice.class);

	@ExceptionHandler(value = ImageDamageException.class)
	public Result<Void> handleImageDamageException(ImageDamageException e) {
		LOGGER.error("图片损坏异常", e);
		return Result.fail(-1220, "图片解析失败");
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = UncheckedGMException.class)
	public Result<Void> handleUncheckedGMException(UncheckedGMException e) {
		LOGGER.error("GraphicsMagick 命令执行异常", e);
		return Result.fail("服务器内部错误");
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = UncheckedGMServiceException.class)
	public Result<Void> handleUncheckedGMServiceException(UncheckedGMServiceException e) {
		LOGGER.error("GraphicsMagick 进程通信异常", e);
		return Result.fail("服务器内部错误");
	}

	@ExceptionHandler(value = UnSupportImageTypeException.class)
	public Result<Void> handleUnSupportImageTypeException(UnSupportImageTypeException e) {
		return Result.fail(-1210, e.getMessage());
	}
}
