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
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * 全局数据异常处理
 * <p>
 * 基于 {@link RestControllerAdvice} 统一捕获数据访问相关异常，并返回一致的错误响应结构。
 * </p>
 * <p>
 * 启用与条件：
 * <ul>
 *   <li>Servlet 类型 Web 应用，且类路径存在 {@link Servlet}、{@link DispatcherServlet}、{@link DataAccessException}</li>
 *   <li>配置项 {@code pangju.web.advice.exception=true}（默认启用）</li>
 * </ul>
 * </p>
 * <p>
 * 映射与行为：
 * <ul>
 *   <li>{@link DataAccessException} → HTTP 500（{@link ResponseStatus} 标注）</li>
 *   <li>返回统一错误响应：{@link Result#fail(String)}，消息为“数据访问错误”</li>
 *   <li>记录异常日志（级别：ERROR），不泄露详细堆栈到客户端</li>
 * </ul>
 * </p>
 * <p>
 * 执行优先级：{@link Ordered#HIGHEST_PRECEDENCE} + 2。
 * </p>
 *
 * @author pangju666
 * @see RestControllerAdvice
 * @see ExceptionHandler
 * @see DataAccessException
 * @since 1.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, DataAccessException.class, Result.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.advice", value = "exception", matchIfMissing = true)
@RestControllerAdvice
public class GlobalDataExceptionAdvice {
	/**
	 * 日志记录器
	 *
	 * @since 1.0.0
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalDataExceptionAdvice.class);

	// ============ 5xx 服务器错误 ============
	/**
	 * 处理数据访问异常
	 * <p>
	 * 记录异常并返回统一错误响应；通过 {@link ResponseStatus} 指定 HTTP 500。
	 * </p>
	 *
	 * @param e 数据访问异常
	 * @return 统一错误响应（状态码 500）
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = DataAccessException.class)
	public Result<Void> handleDataAccessException(DataAccessException e) {
		LOGGER.error("数据访问异常", e);
		return Result.fail("数据访问错误");
	}
}
