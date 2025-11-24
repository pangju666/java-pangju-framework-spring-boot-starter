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
 * 不支持的图片类型异常。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>当输入或输出的图片格式不在支持集合中时抛出。</li>
 *   <li>常见于格式探测（读取）或写出格式校验流程。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 * @see NestedRuntimeException
 */
public class UnSupportedTypeException extends NestedRuntimeException {
	/**
	 * 通过消息构造异常。
	 *
	 * @param message 异常消息
	 * @since 1.0.0
	 */
	public UnSupportedTypeException(String message) {
		super(message);
	}
}