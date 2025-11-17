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

import io.github.pangju666.framework.web.model.Result;
import jakarta.servlet.Servlet;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.Objects;

/**
 * 全局处理上传文件大小超过限制的异常。
 *
 * <p><strong>启用条件</strong></p>
 * <ul>
 *   <li>Servlet Web 应用，类路径存在 {@code Servlet}、{@code DispatcherServlet}、{@code SizeLimitExceededException}</li>
 *   <li>配置项 {@code pangju.web.advice.exception=true}（默认启用）</li>
 * </ul>
 *
 * <p><strong>行为说明</strong></p>
 * <ul>
 *   <li>捕获 {@link MaxUploadSizeExceededException} 并返回 HTTP 400</li>
 *   <li>当 {@code getMaxUploadSize() == -1} 时，尝试从嵌套的 {@link SizeLimitExceededException}
 *       读取允许大小；否则直接使用 {@code getMaxUploadSize()}</li>
 *   <li>使用 {@link Result#fail(String)} 返回错误消息，不向客户端暴露服务端堆栈</li>
 * </ul>
 *
 * <p><strong>优先级</strong></p>
 * <ul>
 *   <li>{@link Ordered#HIGHEST_PRECEDENCE} + 2</li>
 * </ul>
 *
 * <p><strong>相关说明</strong></p>
 * <ul>
 *   <li>在 Tomcat 环境下，上传大小限制通常以 {@link SizeLimitExceededException} 作为嵌套原因提供细粒度信息</li>
 * </ul>
 *
 * @see MaxUploadSizeExceededException
 * @see SizeLimitExceededException
 * @see Result
 * @see HttpStatus#BAD_REQUEST
 * @see RestControllerAdvice
 * @since 1.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, SizeLimitExceededException.class, Result.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.advice", value = "exception", matchIfMissing = true)
@RestControllerAdvice
public class GlobalTomcatFileUploadExceptionAdvice {
    /**
     * 用于记录文件上传异常的日志。
     *
     * @since 1.0.0
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalTomcatFileUploadExceptionAdvice.class);

    /**
     * 处理上传文件大小超过限制异常。
     *
     * <p><strong>行为</strong></p>
     * <ul>
     *   <li>记录 ERROR 级别日志（包含异常堆栈）</li>
     *   <li>返回统一失败响应，HTTP 400（{@link HttpStatus#BAD_REQUEST}）</li>
     *   <li>当 {@code e.getMaxUploadSize() == -1} 时，尝试解析嵌套的 {@link SizeLimitExceededException}
     *       以获取允许大小；否则使用 {@code e.getMaxUploadSize()}</li>
     *   <li>大小按 MB 展示（{@link DataSize#ofBytes(long)} 转换后 {@code toMegabytes()}）</li>
     * </ul>
     *
     * @param e 上传大小超限异常
     * @return 统一失败响应，状态码 400
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

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = SizeLimitExceededException.class)
	public Result<Void> handleSizeLimitExceededException(SizeLimitExceededException e) {
		LOGGER.error("上传文件大小超过限制", e);
		return Result.fail("上传文件大小超过" + DataSize.ofBytes(e.getPermittedSize()).toMegabytes() + "MB");
	}
}
