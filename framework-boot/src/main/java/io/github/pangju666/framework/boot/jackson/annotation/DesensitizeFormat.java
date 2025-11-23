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
 * 数据脱敏注解，用于在 JSON 序列化过程中保护敏感信息。
 *
 * <p>支持两类策略：</p>
 * <ul>
 *   <li>内置类型：选择 {@link DesensitizedType} 中的内置类型（如身份证号、手机号、邮箱等），直接应用内置算法。</li>
 *   <li>自定义类型（{@link DesensitizedType#CUSTOM}）：使用 {@link #prefix()} 与 {@link #suffix()} 控制前后缀保留与中间隐藏。</li>
 * </ul>
 *
 * <p>在序列化过程中由 {@link DesensitizedJsonSerializer} 按类型与参数自动选择具体实现。</p>
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
     * 脱敏策略类型。
     *
     * <p>当选择内置类型时，应用对应的内置算法；当选择 {@link DesensitizedType#CUSTOM} 时，
     * 使用 {@link #prefix()} 与 {@link #suffix()} 控制前后缀保留与中间隐藏。</p>
     *
     * @return 脱敏类型
     * @see DesensitizedType
     * @since 1.0.0
     */
	DesensitizedType type();

    /**
     * 保留的前缀长度。
     *
     * <p>当 {@link #type()} 为 {@link DesensitizedType#CUSTOM} 时生效；指定保留原始字符串开头的字符数量（&le;-1 表示不保留）。</p>
     *
     * @return 保留前缀长度
     * @since 1.0.0
     */
	int prefix() default -1;

    /**
     * 保留的后缀长度。
     *
     * <p>当 {@link #type()} 为 {@link DesensitizedType#CUSTOM} 时生效；指定保留原始字符串末尾的字符数量（&le;-1 表示不保留）。</p>
     *
     * @return 保留后缀长度
     * @since 1.0.0
     */
	int suffix() default -1;
}