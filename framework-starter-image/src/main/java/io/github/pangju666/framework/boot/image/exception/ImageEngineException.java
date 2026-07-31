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

package io.github.pangju666.framework.boot.image.exception;

import org.jspecify.annotations.Nullable;
import org.springframework.core.NestedRuntimeException;

/**
 * 图像引擎异常。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>用于标识图像处理引擎本身的错误，而非图像操作或解析错误。</li>
 *   <li>主要表示引擎层面的故障，如进程通信错误。</li>
 *   <li>与{@link ImageOperationException}（图像操作错误）和{@link ImageParsingException}（图像解析错误）区分开。</li>
 * </ul>
 *
 * <p><strong>常见场景</strong></p>
 * <ul>
 *   <li>图像引擎进程启动/通信失败</li>
 * </ul>
 *
 * <p><strong>异常类型</strong></p>
 * <ul>
 *   <li>为运行时异常（继承 {@link org.springframework.core.NestedRuntimeException}），适合在业务层统一捕获与处理。</li>
 * </ul>
 *
 * @author pangju666
 * @see org.springframework.core.NestedRuntimeException
 * @see ImageOperationException
 * @see ImageParsingException
 * @since 2.1.0
 */
public class ImageEngineException extends NestedRuntimeException {
	/**
	 * 使用自定义消息构造异常。
	 *
	 * @param message 异常消息
	 * @since 2.1.0
	 */
	public ImageEngineException(@Nullable String message) {
		super(message);
	}

	/**
	 * 使用自定义消息与原因构造异常。
	 *
	 * @param message 异常消息
	 * @param cause   原始异常原因
	 * @since 2.1.0
	 */
	public ImageEngineException(@Nullable String message, @Nullable Throwable cause) {
		super(message, cause);
	}
}
