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

import java.io.File;
import java.util.Objects;

/**
 * OCR异常类。
 * <p>
 * 继承自{@link org.springframework.core.NestedRuntimeException}，
 * 用于表示OCR处理过程中发生的异常。
 * 支持携带文件信息和异常原因，便于错误定位和排查。
 * </p>
 *
 * @since 2.1.0
 */
public class OcrException extends NestedRuntimeException {
	/**
	 * 构造函数。
	 * <p>
	 * 根据文件和原因构造异常信息，如果文件不为空则包含文件路径。
	 * </p>
	 *
	 * @param file 处理的文件（可为null）
	 * @param reason 异常原因（可为null）
	 * @since 2.1.0
	 */
	public OcrException(@Nullable File file, @Nullable String reason) {
		super(Objects.nonNull(file) ? reason + "，文件路径：" + file.getAbsolutePath() : reason);
	}

	/**
	 * 构造函数。
	 *
	 * @param message 异常消息（可为null）
	 * @since 2.1.0
	 */
	public OcrException(@Nullable String message) {
		super(message);
	}

	/**
	 * 构造函数。
	 * <p>
	 * 根据文件、原因和原始异常构造异常信息，如果文件不为空则包含文件路径。
	 * </p>
	 *
	 * @param file 处理的文件（可为null）
	 * @param reason 异常原因（可为null）
	 * @param cause 原始异常（可为null）
	 * @since 2.1.0
	 */
	public OcrException(@Nullable File file, @Nullable String reason, @Nullable Throwable cause) {
		super(Objects.nonNull(file) ? reason + "，文件路径：" + file.getAbsolutePath() : reason, cause);
	}

	/**
	 * 构造函数。
	 *
	 * @param message 异常消息（可为null）
	 * @param cause 原始异常（可为null）
	 * @since 2.1.0
	 */
	public OcrException(@Nullable String message, @Nullable Throwable cause) {
		super(message, cause);
	}
}
