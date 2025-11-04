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
 * 全局数据异常处理器
 * <p>
 * 该类用于统一处理Web应用中抛出的各种异常。
 * 通过{@link RestControllerAdvice}和{@link ExceptionHandler}注解，
 * 为应用中的异常提供统一的错误响应格式和HTTP状态码映射。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>处理数据访问异常</li>
 * </ul>
 * </p>
 * <p>
 * 配置条件：
 * <ul>
 *     <li>应用必须是Servlet类型的Web应用</li>
 *     <li>Classpath中必须存在Servlet、DispatcherServlet和DataAccessException类</li>
 *     <li>配置属性{@code pangju.web.advice.exception}必须为true（默认为true）</li>
 * </ul>
 * </p>
 * <p>
 * 支持的异常类型和HTTP状态码映射：
 * <ul>
 *     <li>
 *         <strong>DataAccessException</strong> - 500 Internal Server Error
 *         <p>数据访问异常</p>
 *     </li>
 * </ul>
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * pangju:
 *   web:
 *     advice:
 *       exception: true  # 默认为true，启用全局异常处理
 * </pre>
 * </p>
 * <p>
 * 响应格式：
 * <p>
 * 所有异常处理方法都返回统一的错误响应格式：
 * <pre>
 * {
 *   "code": "错误代码",
 *   "message": "错误描述信息",
 *   "data": null
 * }
 * </pre>
 * </p>
 * </p>
 * <p>
 * 日志记录：
 * <p>
 * 该类会对某些异常进行日志记录，便于问题排查和监控。
 * 记录级别为ERROR，可通过日志系统进行追踪。
 * </p>
 * </p>
 *
 * @author pangju666
 * @see RestControllerAdvice
 * @see ExceptionHandler
 * @since 1.0.0
 */
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, DataAccessException.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.advice", value = "exception", matchIfMissing = true)
@RestControllerAdvice
public class GlobalDataExceptionAdvice {
	/**
	 * 日志记录器
	 *
	 * @since 1.0.0
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalDataExceptionAdvice.class);

	/**
	 * 处理数据访问异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码500
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = DataAccessException.class)
	public Result<Void> handleDataAccessException(DataAccessException e) {
		LOGGER.error("数据访问异常", e);
		return Result.fail("数据访问错误");
	}
}
