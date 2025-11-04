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
 *     <li>在幂等性检查拦截器中检测到重复请求时抛出</li>
 *     <li>通知客户端该请求已处理过，不应重复提交</li>
 *     <li>与{@link Idempotent}注解配合使用</li>
 * </ul>
 * </p>
 * <p>
 * 幂等性检查流程：
 * <ol>
 *     <li>拦截器识别方法上的{@link Idempotent}注解</li>
 *     <li>生成基于请求和配置的幂等键</li>
 *     <li>检查该键是否已存在于幂等性存储中</li>
 *     <li>如果键已存在，说明这是重复请求，抛出该异常</li>
 *     <li>如果键不存在，说明这是首次请求，继续处理</li>
 * </ol>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * // 方式1：使用自定义错误消息
 * if (isDuplicateRequest()) {
 *     throw new IdempotentException("您的请求已处理，请勿重复提交");
 * }
 *
 * // 方式2：使用幂等注解配置的消息
 * Idempotent annotation = method.getAnnotation(Idempotent.class);
 * if (isDuplicateRequest()) {
 *     throw new IdempotentException(annotation);
 * }
 * }
 * </pre>
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
 * <p>
 * 幂等性存储支持：
 * <ul>
 *     <li>Redis存储 - 分布式幂等性检查</li>
 *     <li>内存存储 - 单机幂等性检查</li>
 * </ul>
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
	 * <p>
	 * 创建一个包含自定义错误消息的幂等性异常。
	 * </p>
	 * <p>
	 * 使用示例：
	 * <pre>
	 * {@code
	 * throw new IdempotentException("您的请求已在5秒内处理过，请稍后再试");
	 * }
	 * </pre>
	 * </p>
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
	 * <p>
	 * 从{@link Idempotent}注解中提取错误消息，创建幂等性异常。
	 * 注解中的message属性将作为异常的错误消息。
	 * 这种构造方式适用于在拦截器或切面中检测到重复请求时，
	 * 直接使用控制器方法上配置的幂等注解来创建异常。
	 * </p>
	 * <p>
	 * 工作流程：
	 * <ol>
	 *     <li>获取方法上的{@link Idempotent}注解</li>
	 *     <li>从注解的message属性中提取错误消息</li>
	 *     <li>使用该消息创建异常</li>
	 *     <li>异常被处理器捕获并转换为HTTP响应</li>
	 * </ol>
	 * </p>
	 * <p>
	 * 使用示例：
	 * <pre>
	 * {@code
	 * @PostMapping("/order")
	 * @Idempotent(
	 *     key = "#p0.orderId",
	 *     interval = 5,
	 *     timeUnit = TimeUnit.SECONDS,
	 *     message = "订单已处理，请勿重复提交"
	 * )
	 * public ResponseEntity<?> createOrder(@RequestBody Order order) {
	 *     return ResponseEntity.ok("order created");
	 * }
	 *
	 * // 在幂等性检查器中
	 * Idempotent annotation = method.getAnnotation(Idempotent.class);
	 * if (isDuplicateRequest()) {
	 *     throw new IdempotentException(annotation);  // 使用注解配置的消息
	 * }
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param annotation {@link Idempotent}注解实例，提供幂等性配置和错误消息。
	 *                   从{@link Idempotent#message()}方法获取消息
	 * @since 1.0.0
	 */
	public IdempotentException(Idempotent annotation) {
		super(annotation.message());
	}
}
