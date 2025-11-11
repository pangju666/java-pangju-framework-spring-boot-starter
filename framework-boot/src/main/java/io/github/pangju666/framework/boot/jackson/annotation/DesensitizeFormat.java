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

package io.github.pangju666.framework.boot.jackson.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.pangju666.framework.boot.jackson.enums.DesensitizedType;
import io.github.pangju666.framework.boot.jackson.serializer.DesensitizedJsonSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据脱敏注解，用于在JSON序列化过程中保护敏感信息
 * <p>
 * 该注解可用于标记需要脱敏处理的字段，支持多种脱敏策略：
 * <ul>
 *   <li>预设的脱敏类型 - 如身份证号、手机号、邮箱等</li>
 *   <li>正则表达式替换 - 通过regex和format参数配合使用</li>
 *   <li>前缀后缀保留 - 通过prefix和suffix参数控制</li>
 * </ul>
 * 在序列化过程中，会自动应用{@link DesensitizedJsonSerializer}对字段值进行脱敏处理。
 * </p>
 *
 * @author pangju666
 * @see DesensitizedJsonSerializer
 * @see DesensitizedType
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = DesensitizedJsonSerializer.class)
public @interface DesensitizeFormat {
	/**
	 * 脱敏策略类型
	 * <p>
	 * 指定要应用的脱敏策略。当选择预设类型时，将应用对应的内置脱敏算法；
	 * 当选择{@link DesensitizedType#CUSTOM}时，需要通过其他参数自定义脱敏规则。
	 * </p>
	 *
	 * @return 脱敏类型
	 * @see DesensitizedType
	 * @since 1.0.0
	 */
	DesensitizedType type() default DesensitizedType.CUSTOM;

	/**
	 * 替换格式字符串
	 * <p>
	 * 当使用正则表达式脱敏时，此参数指定用于替换匹配内容的格式。
	 * 例如，可以设置为"*"、"****"或"$1***$2"等。
	 * 仅在{@link #type()}为{@link DesensitizedType#CUSTOM}且
	 * 同时设置了{@link #regex()}参数时有效。
	 * </p>
	 *
	 * @return 替换格式字符串
	 * @since 1.0.0
	 */
	String format() default "";

	/**
	 * 匹配模式的正则表达式
	 * <p>
	 * 当使用正则表达式脱敏时，此参数指定用于匹配需要脱敏内容的模式。
	 * 可以包含捕获组，配合{@link #format()}参数使用。
	 * 仅在{@link #type()}为{@link DesensitizedType#CUSTOM}时有效。
	 * </p>
	 *
	 * @return 正则表达式
	 * @since 1.0.0
	 */
	String regex() default "";

	/**
	 * 保留的前缀长度
	 * <p>
	 * 当使用前缀后缀保留方式脱敏时，此参数指定保留原始字符串开头的字符数量。
	 * 例如，对于"13812345678"，如果prefix=3，将保留"138"。
	 * 设置为-1表示不保留前缀。
	 * 仅在{@link #type()}为{@link DesensitizedType#CUSTOM}且
	 * 未设置{@link #regex()}和{@link #format()}时有效。
	 * </p>
	 *
	 * @return 保留前缀长度
	 * @since 1.0.0
	 */
	int prefix() default -1;

	/**
	 * 保留的后缀长度
	 * <p>
	 * 当使用前缀后缀保留方式脱敏时，此参数指定保留原始字符串末尾的字符数量。
	 * 例如，对于"13812345678"，如果suffix=4，将保留"5678"。
	 * 设置为-1表示不保留后缀。
	 * 仅在{@link #type()}为{@link DesensitizedType#CUSTOM}且
	 * 未设置{@link #regex()}和{@link #format()}时有效。
	 * </p>
	 *
	 * @return 保留后缀长度
	 * @since 1.0.0
	 */
	int suffix() default -1;
}