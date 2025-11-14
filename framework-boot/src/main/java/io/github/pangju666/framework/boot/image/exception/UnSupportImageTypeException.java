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

import io.github.pangju666.framework.web.annotation.HttpException;
import io.github.pangju666.framework.web.enums.HttpExceptionType;
import io.github.pangju666.framework.web.exception.base.ServiceException;

/**
 * 不受支持的图片类型异常。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>当输入的图片类型（MIME 类型或扩展名）不在系统支持范围内时抛出。</li>
 *   <li>继承 {@link ServiceException}，作为服务层业务异常，并通过 {@link HttpException} 注解映射为 HTTP 错误。</li>
 * </ul>
 *
 * <p><b>HTTP 映射</b></p>
 * <ul>
 *   <li>错误码：{@code 210}</li>
 *   <li>类型：{@link HttpExceptionType#SERVICE}</li>
 *   <li>描述：{@code 不受支持的图片类型错误}</li>
 *   <li>日志：{@code false}（默认不记录 HTTP 层日志，可以由业务层决定是否记录）</li>
 * </ul>
 *
 * <p><b>示例</b></p>
 * <pre>
 * String type = "image/unknown";
 * throw new UnSupportImageTypeException(type);
 * </pre>
 *
 * @author pangju666
 * @since 1.0.0
 */
@HttpException(code = 210, type = HttpExceptionType.SERVICE, description = "不受支持的图片类型错误", log = false)
public class UnSupportImageTypeException extends ServiceException {
	public UnSupportImageTypeException(String message, String reason) {
		super(message, reason);
	}

	public UnSupportImageTypeException(String message) {
		super(message);
	}
}