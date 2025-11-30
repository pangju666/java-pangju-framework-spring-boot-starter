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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * 全局处理系统级内部异常。
 *
 * <p><strong>启用条件</strong></p>
 * <ul>
 *   <li>Servlet Web 应用，类路径存在 {@code Servlet}、{@code DispatcherServlet}</li>
 *   <li>配置项 {@code pangju.web.advice.enable-exception=true}（默认启用）</li>
 * </ul>
 *
 * <p><strong>行为说明</strong></p>
 * <ul>
 *   <li>优先处理 {@link NestedRuntimeException}，记录最具体的异常原因；兜底处理其他未被覆盖的 {@link Exception}（含 IO/运行时）。</li>
 *   <li>记录 ERROR 级别日志（包含堆栈）。</li>
 *   <li>返回统一失败响应：{@link Result#fail(String)}，HTTP 500（{@link HttpStatus#INTERNAL_SERVER_ERROR}），消息为通用文案“服务器内部错误”。</li>
 * </ul>
 *
 * <p><strong>优先级</strong></p>
 * <ul>
 *   <li>{@link Ordered#HIGHEST_PRECEDENCE} + 4</li>
 * </ul>
 *
 * <p><strong>相关说明</strong></p>
 * <ul>
 *   <li>作为最终兜底处理器，补位其他异常处理器（如 {@code GlobalSpringExceptionAdvice}、{@code GlobalTomcatFileUploadExceptionAdvice}、{@code GlobalValidationExceptionAdvice}）未覆盖的异常</li>
 *   <li>不向客户端暴露具体异常信息与堆栈，避免泄露实现细节</li>
 * </ul>
 *
 * @see RestControllerAdvice
 * @see ExceptionHandler
 * @see Result
 * @since 1.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 4)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, Result.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.advice", value = "enable-exception", matchIfMissing = true)
@RestControllerAdvice
public class GlobalInternalExceptionAdvice {
	/**
	 * 日志记录器。
	 *
	 * <p>记录 ERROR 级别日志并包含异常堆栈。</p>
	 *
	 * @since 1.0.0
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalInternalExceptionAdvice.class);

	/**
	 * 处理嵌套运行时异常（NestedRuntimeException）。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>记录 ERROR 级别日志，输出最具体的异常原因（{@link NestedRuntimeException#getMostSpecificCause()}）。</li>
	 *   <li>返回统一失败响应，HTTP 500（{@link HttpStatus#INTERNAL_SERVER_ERROR}），提示“服务器内部错误”。</li>
	 * </ul>
	 *
	 * @param e 嵌套运行时异常
	 * @return 统一失败响应
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = NestedRuntimeException.class)
	public Result<Void> handleNestedRuntimeException(NestedRuntimeException e) {
		LOGGER.error("嵌套运行时异常", e.getMostSpecificCause());
		return Result.fail("服务器内部错误");
	}

	/**
	 * 处理所有其他异常（兜底处理）。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>记录 ERROR 级别日志（包含堆栈）。</li>
	 *   <li>返回统一失败响应，HTTP 500（{@link HttpStatus#INTERNAL_SERVER_ERROR}）。</li>
	 *   <li>不暴露具体异常细节，统一文案“服务器内部错误”。</li>
	 * </ul>
	 *
	 * @param e 异常实例
	 * @return 统一失败响应
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = Exception.class)
	public Result<Void> handleException(Exception e) {
		LOGGER.error("系统级异常", e);
		return Result.fail("服务器内部错误");
	}
}
