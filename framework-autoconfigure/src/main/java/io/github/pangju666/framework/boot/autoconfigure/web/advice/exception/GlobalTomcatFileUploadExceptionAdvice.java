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

import io.github.pangju666.framework.web.model.common.Result;
import jakarta.servlet.Servlet;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.Objects;

@Order(Ordered.LOWEST_PRECEDENCE - 1)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, DataAccessException.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.advice", value = "exception", matchIfMissing = true)
@RestControllerAdvice
public class GlobalTomcatFileUploadExceptionAdvice {
	/**
	 * 日志记录器
	 *
	 * @since 1.0.0
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalTomcatFileUploadExceptionAdvice.class);

	/**
	 * 处理上传文件大小超过限制异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码400
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MaxUploadSizeExceededException.class)
	public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
		LOGGER.error("上传文件大小超过限制", e);
		if (e.getMaxUploadSize() == -1) {
			if (Objects.nonNull(e.getCause()) &&
				e.getCause() instanceof IllegalStateException illegalStateException &&
				Objects.nonNull(illegalStateException.getCause()) &&
				illegalStateException.getCause() instanceof SizeLimitExceededException sizeLimitExceededException) {
				return Result.fail("上传文件大小超过" + DataSize.ofBytes(sizeLimitExceededException.getPermittedSize()).toMegabytes() + "MB");
			}
			return Result.fail("上传文件大小超过限制");
		}
		return Result.fail("上传文件大小超过" + DataSize.ofBytes(e.getMaxUploadSize()).toMegabytes() + "MB");
	}
}
