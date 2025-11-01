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

package io.github.pangju666.framework.autoconfigure.web.limit.exception;

import io.github.pangju666.framework.autoconfigure.web.limit.annotation.RateLimit;
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
 *     <li>在限流拦截器中检测到请求超过限制时抛出</li>
 *     <li>通知客户端请求过于频繁，需要进行退避</li>
 *     <li>与{@link RateLimit}注解配合使用</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * // 方式1：使用自定义错误消息
 * if (isRateLimited()) {
 *     throw new RateLimitException("请求次数已达上限，请稍候再试");
 * }
 *
 * // 方式2：使用限流注解配置的消息
 * RateLimit annotation = method.getAnnotation(RateLimit.class);
 * if (isRateLimited()) {
 *     throw new RateLimitException(annotation);
 * }
 * }
 * </pre>
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
	 * <p>
	 * 创建一个包含自定义错误消息的限流异常。
	 * 该异常会被转换为HTTP 429响应，并附带指定的错误消息。
	 * </p>
	 * <p>
	 * 使用示例：
	 * <pre>
	 * {@code
	 * throw new RateLimitException("您的请求过于频繁，请在1分钟后重试");
	 * }
	 * </pre>
	 * </p>
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
	 * <p>
	 * 从{@link RateLimit}注解中提取错误消息，创建限流异常。
	 * 注解中的message属性将作为异常的错误消息。
	 * 这种构造方式适用于在拦截器等地方检测到限流时，
	 * 直接使用控制器方法上配置的限流注解来创建异常。
	 * </p>
	 * <p>
	 * 工作流程：
	 * <ol>
	 *     <li>获取方法上的{@link RateLimit}注解</li>
	 *     <li>从注解的message属性中提取错误消息</li>
	 *     <li>使用该消息创建异常</li>
	 *     <li>异常被处理器捕获并转换为HTTP响应</li>
	 * </ol>
	 * </p>
	 * <p>
	 * 使用示例：
	 * <pre>
	 * {@code
	 * @GetMapping("/api/data")
	 * @RateLimit(rate = 100)
	 * public ResponseEntity<?> getData() {
	 *     return ResponseEntity.ok("data");
	 * }
	 *
	 * // 在拦截器中
	 * RateLimit annotation = method.getAnnotation(RateLimit.class);
	 * if (isRateLimited()) {
	 *     throw new RateLimitException(annotation);  // 使用注解配置的消息
	 * }
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param annotation {@link RateLimit}注解实例，提供限流配置和错误消息。
	 *                   从{@link RateLimit#message()}方法获取消息
	 * @since 1.0.0
	 */
	public RateLimitException(RateLimit annotation) {
		super(annotation.message());
	}
}
