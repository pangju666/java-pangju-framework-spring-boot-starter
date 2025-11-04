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

package io.github.pangju666.framework.boot.autoconfigure.web.advice;

import io.github.pangju666.framework.boot.autoconfigure.web.advice.bind.RequestParamBindingAdvice;
import io.github.pangju666.framework.boot.autoconfigure.web.advice.exception.GlobalExceptionAdvice;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Web增强功能配置属性类
 * <p>
 * 用于配置Web层面的各种增强功能。配置前缀为{@code pangju.web.advice}。
 * 通过该配置类可以控制请求参数绑定、全局异常处理等功能的启用或禁用。
 * </p>
 * <p>
 * 支持的配置项：
 * <ul>
 *     <li>binding - 请求参数绑定增强功能</li>
 *     <li>exception - 全局异常处理功能</li>
 * </ul>
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * pangju:
 *   web:
 *     advice:
 *       binding: true    # 启用请求参数绑定增强（默认为true）
 *       exception: true  # 启用全局异常处理（默认为true）
 * </pre>
 * </p>
 * <p>
 * 功能说明：
 * <ul>
 *     <li>
 *         <strong>binding配置</strong>
 *         <p>
 *         用于控制是否启用请求参数自动绑定增强功能。
 *         启用后，会自动将请求参数中的时间戳转换为Date、LocalDate、LocalDateTime对象。
 *         由{@link RequestParamBindingAdvice}处理。
 *         </p>
 *     </li>
 *     <li>
 *         <strong>exception配置</strong>
 *         <p>
 *         用于控制是否启用全局异常处理功能。
 *         启用后，会统一处理应用中抛出的各种异常，并返回统一的错误响应格式。
 *         由{@link GlobalExceptionAdvice}处理。
 *         </p>
 *     </li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see RequestParamBindingAdvice
 * @see GlobalExceptionAdvice
 * @see AdviceAutoConfiguration
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "pangju.web.advice")
public class AdviceProperties {
	/**
	 * 请求参数绑定增强功能开关
	 * <p>
	 * 默认值为true，表示启用请求参数的自动绑定增强。
	 * 启用后，会自动将请求参数中的时间戳（毫秒级）转换为Date、LocalDate、LocalDateTime对象。
	 * </p>
	 * <p>
	 * 配置该功能需依赖{@link RequestParamBindingAdvice}的实现。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private boolean binding = true;
	/**
	 * 全局异常处理增强功能开关
	 * <p>
	 * 默认值为true，表示启用全局异常处理。
	 * 启用后，会统一处理应用中抛出的各种异常，并返回统一格式的错误响应。
	 * </p>
	 * <p>
	 * 配置该功能需依赖{@link GlobalExceptionAdvice}的实现。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private boolean exception = true;

	public boolean isBinding() {
		return binding;
	}

	public void setBinding(boolean binding) {
		this.binding = binding;
	}

	public boolean isException() {
		return exception;
	}

	public void setException(boolean exception) {
		this.exception = exception;
	}
}
