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

package io.github.pangju666.framework.boot.web.exception;

import io.github.pangju666.framework.boot.web.annotation.RateLimit;
import io.github.pangju666.framework.web.annotation.HttpException;
import io.github.pangju666.framework.web.enums.HttpExceptionType;
import io.github.pangju666.framework.web.exception.base.ValidationException;
import org.springframework.http.HttpStatus;

/**
 * 速率限制异常
 * <p>
 * 当请求超过设定的限流阈值时抛出该异常。
 * 该异常被标记为HTTP异常，会被自动转换为HTTP 429（Too Many Requests）响应。
 * </p>
 * <p>
 * 异常特性：
 * <ul>
 *     <li>HTTP状态码：429 Too Many Requests</li>
 *     <li>异常代码：410</li>
 *     <li>异常类型：VALIDATION（验证类异常）</li>
 *     <li>日志记录：false（该异常不会被记录到应用日志中）</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *     <li>通知客户端请求过于频繁，需要进行退避</li>
 * </ul>
 * </p>
 * <p>
 * 客户端响应示例：
 * <pre>
 * {@code
 * {
 *   "code": 4410,
 *   "message": "请求次数已达上限，请稍候再试"
 * }
 * }
 * </pre>
 * </p>
 *
 * @author pangju666
 * @see RateLimit
 * @see ValidationException
 * @since 1.0.0
 */
@HttpException(code = 410, type = HttpExceptionType.VALIDATION, description = "接口限流错误", log = false, status = HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitException extends ValidationException {
	/**
	 * 使用错误消息构造异常
	 *
	 * @param message 错误消息，用于提示用户请求被限流。
	 *                该消息会被返回给客户端，建议提供友好的提示信息
	 * @since 1.0.0
	 */
	public RateLimitException(String message) {
		super(message);
	}

	/**
	 * 使用限流注解构造异常
	 *
	 * @param annotation {@link RateLimit}注解实例，提供限流配置和错误消息。从{@link RateLimit#message()}方法获取消息
	 * @since 1.0.0
	 */
	public RateLimitException(RateLimit annotation) {
		super(annotation.message());
	}
}
