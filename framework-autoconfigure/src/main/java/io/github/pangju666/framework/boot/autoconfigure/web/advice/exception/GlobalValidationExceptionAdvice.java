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
 * 全局处理参数验证失败异常。
 *
 * <p><strong>启用条件</strong></p>
 * <ul>
 *   <li>Servlet Web 应用，类路径存在 {@code Servlet}、{@code DispatcherServlet}、{@code ConstraintViolationException}</li>
 *   <li>配置项 {@code pangju.web.advice.exception=true}（默认启用）</li>
 * </ul>
 *
 * <p><strong>行为说明</strong></p>
 * <ul>
 *   <li>捕获 {@link ConstraintViolationException} 并返回 HTTP 400</li>
 *   <li>优先取第一个约束违例的 {@code message} 作为提示；无可用提示时返回“请求参数验证不合法”</li>
 *   <li>返回 {@link Result#fail(String)} 作为统一错误响应</li>
 * </ul>
 *
 * <p><strong>优先级</strong></p>
 * <ul>
 *   <li>{@link Ordered#HIGHEST_PRECEDENCE} + 2</li>
 * </ul>
 *
 * <p><strong>相关说明</strong></p>
 * <ul>
 *   <li>适用于 Bean Validation 方法参数校验（如 {@code @Validated}、{@code @Valid}）抛出的约束违例</li>
 * </ul>
 *
 * @see RestControllerAdvice
 * @see ExceptionHandler
 * @see ConstraintViolationException
 * @see HttpStatus#BAD_REQUEST
 * @see Result
 * @since 1.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, ConstraintViolationException.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.advice", value = "exception", matchIfMissing = true)
@RestControllerAdvice
public class GlobalValidationExceptionAdvice {
    /**
     * 处理参数验证异常。
     *
     * <p><strong>行为</strong></p>
     * <ul>
     *   <li>返回统一失败响应，HTTP 400（{@link HttpStatus#BAD_REQUEST}）</li>
     *   <li>若存在约束违例，优先选择第一个违例的 {@code message} 作为提示</li>
     *   <li>若无可用提示，返回“请求参数验证不合法”</li>
     * </ul>
     *
     * @param e 参数验证异常
     * @return 统一失败响应，状态码 400
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
