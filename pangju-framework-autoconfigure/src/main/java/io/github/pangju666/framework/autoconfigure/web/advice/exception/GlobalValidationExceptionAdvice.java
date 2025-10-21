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

package io.github.pangju666.framework.autoconfigure.web.advice.exception;

import io.github.pangju666.framework.web.model.common.Result;
import jakarta.servlet.Servlet;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.StringUtils;
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

import java.util.Set;

/**
 * 全局参数验证异常处理器
 * <p>
 * 该类用于统一处理Web应用中抛出的各种异常。
 * 通过{@link RestControllerAdvice}和{@link ExceptionHandler}注解，
 * 为应用中的异常提供统一的错误响应格式和HTTP状态码映射。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>处理参数验证异常</li>
 * </ul>
 * </p>
 * <p>
 * 配置条件：
 * <ul>
 *     <li>应用必须是Servlet类型的Web应用</li>
 *     <li>Classpath中必须存在Servlet、DispatcherServlet和ConstraintViolationException类</li>
 *     <li>配置属性{@code pangju.web.advice.exception}必须为true（默认为true）</li>
 * </ul>
 * </p>
 * <p>
 * 支持的异常类型和HTTP状态码映射：
 * <ul>
 *     <li>
 *         <strong>ConstraintViolationException</strong> - 400 Bad Request
 *         <p>请求参数约束验证失败</p>
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
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, ConstraintViolationException.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.advice", value = "exception", matchIfMissing = true)
@RestControllerAdvice
public class GlobalValidationExceptionAdvice {
	/**
	 * 处理参数验证异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码400
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = ConstraintViolationException.class)
	public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
		Set<ConstraintViolation<?>> constraints = e.getConstraintViolations();
		if (!constraints.isEmpty()) {
			ConstraintViolation<?> constraint = constraints.iterator().next();
			return Result.fail(StringUtils.defaultString(constraint.getMessage()));
		}
		return Result.fail("请求参数验证不合法");
	}
}
