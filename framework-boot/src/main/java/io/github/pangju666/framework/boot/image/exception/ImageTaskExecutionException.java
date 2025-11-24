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

package io.github.pangju666.framework.boot.image.exception;

import org.springframework.core.NestedRuntimeException;

/**
 * 图像任务执行异常。
 * <p>
 * 用于封装在图像处理任务执行过程中发生的运行时异常，继承自 Spring 的
 * {@link org.springframework.core.NestedRuntimeException}，支持携带嵌套异常。
 * 典型场景包括异步执行失败、未知异常包装与跨层传递统一的异常类型。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class ImageTaskExecutionException extends NestedRuntimeException {
	/**
	 * 使用消息构造异常。
	 *
	 * @param message 异常消息
	 * @since 1.0.0
	 */
	public ImageTaskExecutionException(String message) {
		super(message);
	}

	/**
	 * 使用消息与根因构造异常。
	 *
	 * @param message 异常消息
	 * @param cause   根因异常
	 * @since 1.0.0
	 */
	public ImageTaskExecutionException(String message, Throwable cause) {
		super(message, cause);
	}
}
