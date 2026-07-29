/*
 *   Copyright 2026 pangju666
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

package io.github.pangju666.framework.boot.ocr.exception;

import org.jspecify.annotations.Nullable;
import org.springframework.core.NestedRuntimeException;

/**
 * OCR引擎异常类。
 * <p>
 * 继承自{@link org.springframework.core.NestedRuntimeException}，
 * 用于表示OCR引擎初始化或执行过程中发生的异常。
 * 通常与OCR引擎本身的配置、可用性或执行错误相关。
 * </p>
 *
 * @since 2.1.0
 */
public class OcrEngineException extends NestedRuntimeException {
	/**
	 * 构造函数。
	 *
	 * @param message 异常消息（可为null）
	 * @since 2.1.0
	 */
	public OcrEngineException(@Nullable String message) {
		super(message);
	}

	/**
	 * 构造函数。
	 *
	 * @param message 异常消息（可为null）
	 * @param cause   原始异常（可为null）
	 * @since 2.1.0
	 */
	public OcrEngineException(@Nullable String message, @Nullable Throwable cause) {
		super(message, cause);
	}
}
