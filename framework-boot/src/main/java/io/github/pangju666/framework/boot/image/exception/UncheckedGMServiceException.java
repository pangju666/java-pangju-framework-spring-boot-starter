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

import org.gm4java.engine.GMServiceException;
import org.springframework.core.NestedRuntimeException;

/**
 * 非受检 GraphicsMagick 服务异常包装。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>用于将 {@link GMServiceException} 转换为运行时异常，常见于 GraphicsMagick 连接、进程管理等服务异常。</li>
 *   <li>便于传播到业务层或统一异常处理组件。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class UncheckedGMServiceException extends NestedRuntimeException {
	/**
	 * 默认异常消息：图片解析失败。
	 *
	 * @since 1.0.0
	 */
	private static final String DEFAULT_MESSAGE = "GM连接出现错误";

	/**
	 * 使用默认消息与 GraphicsMagick 服务异常原因构造运行时异常。
	 *
	 * @param cause 原始 GM 服务异常
	 * @since 1.0.0
	 */
	public UncheckedGMServiceException(GMServiceException cause) {
		super(DEFAULT_MESSAGE, cause);
	}

	/**
	 * 使用消息与 GraphicsMagick 服务异常原因构造运行时异常。
	 *
	 * @param msg   异常消息
	 * @param cause 原始 GM 服务异常
	 * @since 1.0.0
	 */
	public UncheckedGMServiceException(String msg, GMServiceException cause) {
		super(msg, cause);
	}
}
