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

import org.gm4java.engine.GMException;
import org.gm4java.im4java.GMOperation;
import org.springframework.core.NestedRuntimeException;

/**
 * 非受检 GraphicsMagick 异常包装。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>用于将 {@link GMException} 转换为运行时异常，便于在业务层统一处理。</li>
 *   <li>适用于 GraphicsMagick 命令执行过程中出现的非 I/O 类错误。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class UncheckedGMException extends NestedRuntimeException {
	/**
	 * 使用消息与 GM 异常原因构造运行时异常。
	 *
	 * @param msg   异常消息
	 * @param cause 原始 GM 异常
	 * @since 1.0.0
	 */
	public UncheckedGMException(String msg, GMException cause) {
		super(msg, cause);
	}

	/**
	 * 使用 GM 操作对象与异常原因构造运行时异常。
	 *
	 * @param operation 触发异常的 GM 操作
	 * @param cause     原始 GM 异常
	 * @since 1.0.0
	 */
	public UncheckedGMException(GMOperation operation, GMException cause) {
		super("GM命令: " + operation + " 执行失败", cause);
	}
}
