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

package io.github.pangju666.framework.boot.web.annotation;

import io.github.pangju666.framework.boot.web.resolver.EnumRequestParamArgumentResolver;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.lang.annotation.*;

/**
 * 枚举类型请求参数注解
 * <p>
 * 该注解用于标记Spring MVC控制器方法中的枚举类型参数，指示参数解析器应将HTTP请求参数
 * 转换为对应的枚举实例。注解需配合{@link EnumRequestParamArgumentResolver}使用，
 * 该解析器会在请求处理时识别此注解并进行相应的参数解析和转换。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * @GetMapping("/users")
 * public ResponseEntity<?> getUsers(@EnumRequestParam("status") UserStatus status) {
 *     // status 参数将被自动转换为 UserStatus 枚举实例
 *     return ResponseEntity.ok().build();
 * }
 * }
 * </pre>
 * </p>
 *
 * @author pangju666
 * @see EnumRequestParamArgumentResolver
 * @since 1.0.0
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnumRequestParam {
	/**
	 * 请求参数的名称
	 * <p>
	 * 指定HTTP请求中参数的名称。如果不指定或为空字符串，则默认使用方法参数的名称。
	 * </p>
	 *
	 * @return 请求参数名称，默认为空字符串
	 * @since 1.0.0
	 */
	String value() default "";

	/**
	 * 参数是否为必需
	 * <p>
	 * 当设置为true时，如果请求中缺少该参数且未配置默认值，则抛出
	 * {@link MissingServletRequestParameterException}异常。
	 * 当设置为false时，缺失的参数返回null。
	 * </p>
	 *
	 * @return 是否为必需参数，默认为true
	 * @since 1.0.0
	 */
	boolean required() default true;

	/**
	 * 参数的默认值
	 * <p>
	 * 当请求中未提供参数值时，使用该默认值。默认值应为有效的枚举名称（不区分大小写）。
	 * 如果指定了默认值，则即使{@link #required()}为true，参数缺失时也会使用默认值而不会抛出异常。
	 * </p>
	 *
	 * @return 默认值，默认为空字符串（表示无默认值）
	 * @since 1.0.0
	 */
	String defaultValue() default "";

	/**
	 * 参数的描述信息
	 * <p>
	 * 用于描述该枚举参数的含义，在参数验证失败时会在错误消息中使用。
	 * 当无效的枚举值被提交时，异常消息将为："无效的{description}"。
	 * </p>
	 *
	 * @return 参数描述，默认为"枚举值"
	 * @since 1.0.0
	 */
	String description() default "枚举值";
}