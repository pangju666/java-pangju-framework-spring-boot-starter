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

package io.github.pangju666.framework.boot.web.idempotent.exception;

import io.github.pangju666.framework.boot.web.idempotent.annotation.Idempotent;
import io.github.pangju666.framework.web.annotation.HttpException;
import io.github.pangju666.framework.web.enums.HttpExceptionType;
import io.github.pangju666.framework.web.exception.base.ValidationException;

/**
 * 幂等性验证异常
 * <p>
 * 当请求被判定为重复请求（违反幂等性约束）时抛出该异常。
 * 该异常被标记为HTTP异常。
 * </p>
 * <p>
 * 异常特性：
 * <ul>
 *     <li>异常错误码：420</li>
 *     <li>异常类型：VALIDATION（验证类异常）</li>
 *     <li>日志记录：false（该异常不会被记录到应用日志中）</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *     <li>通知客户端该请求已处理过，不应重复提交</li>
 * </ul>
 * </p>
 * <p>
 * 客户端响应示例：
 * <pre>
 * {@code
 * {
 *   "code": 4420,
 *   "message": "您的请求已处理，请勿重复提交"
 * }
 * }
 * </pre>
 * </p>
 *
 * @author pangju666
 * @see Idempotent
 * @see ValidationException
 * @since 1.0.0
 */
@HttpException(code = 420, type = HttpExceptionType.VALIDATION, description = "接口幂等性错误", log = false)
public class IdempotentException extends ValidationException {
	/**
	 * 使用错误消息构造异常
	 *
	 * @param message 错误消息，用于提示用户这是重复请求。
	 *                该消息会被返回给客户端，建议提供友好的提示信息
	 * @since 1.0.0
	 */
	public IdempotentException(String message) {
		super(message);
	}

	/**
	 * 使用幂等性注解构造异常
	 *
	 * @param annotation {@link Idempotent}注解实例，提供幂等性配置和错误消息。从{@link Idempotent#message()}方法获取消息
	 * @since 1.0.0
	 */
	public IdempotentException(Idempotent annotation) {
		super(annotation.message());
	}
}
