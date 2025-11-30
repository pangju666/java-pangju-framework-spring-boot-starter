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
import io.github.pangju666.framework.boot.autoconfigure.web.advice.exception.*;
import io.github.pangju666.framework.boot.autoconfigure.web.advice.wrapper.ResponseBodyWrapperAdvice;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Web增强功能配置属性
 * <p>
 * 配置前缀：{@code pangju.web.advice}。用于启用或禁用Web层的增强功能。
 * </p>
 * <p>
 * 支持的配置项与默认值：
 * <ul>
 *   <li>binder（默认：true）- 请求参数绑定增强，自动将毫秒时间戳绑定为日期/时间类型</li>
 *   <li>exception（默认：true）- 全局异常处理，统一异常响应结构</li>
 *   <li>wrapper（默认：true）- 响应体统一包装，输出一致的API结构</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * pangju:
 *   web:
 *     advice:
 *       binder: true
 *       exception: true
 *       response-wrapper: true
 * </pre>
 * </p>
 *
 * @author pangju666
 * @see AdviceAutoConfiguration
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "pangju.web.advice")
public class AdviceProperties {
	/**
	 * 是否启用请求参数绑定增强（默认：true）
	 * <p>
	 * 启用后自动将毫秒时间戳绑定为 {@code Date}、{@code LocalDate}、{@code LocalDateTime}。
	 * 由 {@link RequestParamBindingAdvice} 生效。
	 * </p>
	 */
	private boolean enableBinder = true;

	/**
	 * 是否启用全局异常处理（默认：true）
	 * <p>
	 * 启用后统一处理应用异常并返回一致的错误响应结构。
	 * 由 {@link GlobalTomcatFileUploadExceptionAdvice}、{@link GlobalValidationExceptionAdvice}、
	 * {@link GlobalDataExceptionAdvice}、{@link GlobalWebExceptionAdvice}、{@link GlobalInternalExceptionAdvice} 生效。
	 * </p>
	 */
	private boolean enableException = true;

	/**
	 * 是否启用响应体统一包装（默认：true）
	 * <p>
	 * 控制 {@link ResponseBodyWrapperAdvice} 是否生效。
	 * </p>
	 */
	private boolean enableWrapper = true;

	public boolean isEnableBinder() {
		return enableBinder;
	}

	public void setEnableBinder(boolean enableBinder) {
		this.enableBinder = enableBinder;
	}

	public boolean isEnableException() {
		return enableException;
	}

	public void setEnableException(boolean enableException) {
		this.enableException = enableException;
	}

	public boolean isEnableWrapper() {
		return enableWrapper;
	}

	public void setEnableWrapper(boolean enableWrapper) {
		this.enableWrapper = enableWrapper;
	}
}
