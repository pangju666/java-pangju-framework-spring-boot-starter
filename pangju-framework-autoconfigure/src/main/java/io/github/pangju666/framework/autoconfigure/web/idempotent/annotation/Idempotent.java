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

package io.github.pangju666.framework.autoconfigure.web.idempotent.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性校验注解。
 * <p>
 * 用于标注需要进行幂等性校验的方法。通过配置相关属性，可灵活实现对请求的幂等性控制，防止重复请求。
 * 适用于例如订单提交、支付操作等需要幂等性的场景。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *     <li>生成幂等性校验的唯一键（通过 SpEL 表达式动态解析）。</li>
 *     <li>设置请求的过期时间，避免长时间内重复请求无效。</li>
 *     <li>指定重复请求提示信息，在请求被判定为重复时返回给客户端。</li>
 * </ul>
 *
 * <p>使用说明：</p>
 * <ul>
 *     <li>在目标方法上添加该注解，并配置相关属性。</li>
 *     <li>框架会基于该注解动态解析请求上下文生成校验键，并调用幂等校验逻辑。</li>
 *     <li>当请求判定为重复时，会抛出 {@link io.github.pangju666.framework.autoconfigure.web.idempotent.exception.IdempotentException} 异常。</li>
 * </ul>
 *
 * <p>示例代码：</p>
 * <pre>
 * {@code
 * @PostMapping("/order")
 * @Idempotent(
 *     key = "#p0.orderId",   // 基于方法第一个参数解析订单ID作为校验键
 *     interval = 5,         // 请求的有效期为5秒
 *     timeUnit = TimeUnit.SECONDS,
 *     message = "订单已处理，请勿重复提交"  // 如果请求重复，返回该提示信息
 * )
 * public ResponseEntity<?> createOrder(@RequestBody Order order) {
 *     return ResponseEntity.ok("Order created");
 * }
 * }
 * </pre>
 *
 * @author pangju666
 * @see io.github.pangju666.framework.autoconfigure.web.idempotent.aspect.IdempotentAspect
 * @see io.github.pangju666.framework.autoconfigure.web.idempotent.validator.IdempotentValidator
 * @see io.github.pangju666.framework.autoconfigure.web.idempotent.exception.IdempotentException
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Idempotent {
	/**
	 * 生成校验键的前缀。
	 * <p>
	 * 用于为校验键设置统一前缀，避免不同模块/请求的键值冲突。
	 * </p>
	 *
	 * @return 校验键的前缀，默认为空字符串。
	 * @since 1.0.0
	 */
	String prefix() default "";

	/**
	 * 生成唯一校验键的 SpEL 表达式。
	 * <p>
	 * 通过该表达式动态解析方法参数，生成唯一键用于幂等性校验。
	 * </p>
	 *
	 * @return SpEL 表达式。
	 * @since 1.0.0
	 */
	String key();

	/**
	 * 请求重复的有效时间间隔。
	 * <p>
	 * 在该时间间隔内，相同校验键的请求将被视为重复。
	 * </p>
	 *
	 * @return 时间间隔，默认为 1。
	 * @since 1.0.0
	 */
	int interval() default 1;

	/**
	 * 时间间隔的单位。
	 * <p>
	 * 指定 {@code interval} 的时间单位，例如秒、分钟、小时等。
	 * </p>
	 *
	 * @return 时间单位，默认为 {@code TimeUnit.SECONDS}。
	 * @since 1.0.0
	 */
	TimeUnit timeUnit() default TimeUnit.SECONDS;

	/**
	 * 重复请求的提示信息。
	 * <p>
	 * 当请求被判定为重复时，会将该信息返回给客户端。
	 * </p>
	 *
	 * @return 重复请求的提示信息，默认为 "请勿重复请求"。
	 * @since 1.0.0
	 */
	String message() default "请勿重复请求";
}
