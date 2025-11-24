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
import io.github.pangju666.framework.boot.crypto.enums.CryptoAlgorithm;
import io.github.pangju666.framework.boot.crypto.enums.Encoding;
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import io.github.pangju666.framework.boot.jackson.serializer.EncryptJsonSerializer;
import io.github.pangju666.framework.boot.jackson.utils.CryptoFactoryRegistry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JSON 字段加密注解，用于在序列化过程中对指定字段执行加密。
 * <p>
 * 标注于字段后，序列化阶段由 {@link EncryptJsonSerializer} 根据密钥、编码与工厂进行加密输出。
 * 支持明文密钥或占位符形式；编码仅作用于字符串加密；工厂解析优先使用注解指定类型，其次回退到算法枚举关联工厂。
 * </p>
 *
 * <p>支持类型（依据 {@link EncryptJsonSerializer} 的类型分派）：</p>
 * <ul>
 *   <li>标量：{@link String}、<code>byte[]</code>、{@link java.math.BigInteger}、{@link java.math.BigDecimal}</li>
 *   <li>集合：{@link java.util.List}&lt;T&gt;、{@link java.util.Set}&lt;T&gt;、{@link java.util.Collection}&lt;T&gt; 及其他 {@link Iterable}&lt;T&gt;，其中 T 为上述受支持类型</li>
 *   <li>映射：{@link java.util.Map}&lt;?, T&gt;，其中 T 为上述受支持类型</li>
 * </ul>
 * <p>行为：不在列表的类型按 POJO 原样输出；值为 {@code null} 时输出 JSON null；字符串为空白（空或仅空白字符）时原样输出不加密。</p>
 *
 * @author pangju666
 * @see DecryptFormat
 * @see CryptoAlgorithm
 * @see Encoding
 * @see CryptoFactory
 * @see io.github.pangju666.framework.boot.jackson.utils.CryptoFactoryRegistry
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = EncryptJsonSerializer.class)
public @interface EncryptFormat {
	/**
	 * 明文密钥或占位符。
	 *
	 * <p>支持两种形式：</p>
	 * <ul>
	 *   <li>明文密钥：直接传入密钥字符串，例如 {@code @EncryptFormat(key = "my-secret-key")}</li>
	 *   <li>占位符：使用 {@code ${property.name}} 格式，框架将从 Spring 配置读取实际密钥值，例如 {@code @EncryptFormat(key = "${app.encryption.key}")}</li>
	 * </ul>
	 *
	 * @return 密钥或占位符字符串
	 * @since 1.0.0
	 */
	String key();

    /**
     * 加密算法。
     * <p>默认使用 AES256 算法。</p>
     *
     * @return 加密算法
     * @since 1.0.0
     */
	CryptoAlgorithm algorithm() default CryptoAlgorithm.AES256;

    /**
     * 字符串加密输出的编码方式。
     * <p>默认使用 BASE64；仅在加密字符串时生效，对二进制与数值类型不适用。</p>
     *
     * @return 编码方式
     * @since 1.0.0
     */
	Encoding encoding() default Encoding.BASE64;

	/**
	 * 自定义加密工厂（必须存在可访问的无参构造方法）。
	 *
	 * <p>优先级：当提供工厂类型时，优先使用该类型；未提供时按算法枚举关联的工厂。</p>
	 * <p>获取策略：优先从 Spring 容器获取 Bean；当容器不可用或获取失败时回退到直接构造。</p>
	 * <p>构造要求与原因：为保证在容器不可用或无 Bean 定义时能够通过{@link CryptoFactoryRegistry}的反射回退路径创建实例
	 * （调用无参构造），自定义工厂必须提供可访问的无参构造方法（必须为 public）。
	 * 若缺失或不可访问，将导致回退构造失败并抛出异常。</p>
	 * <p>默认与行为：未指定则使用算法默认工厂；如提供多个类型，仅取第一个。</p>
	 *
	 * @return 自定义加密工厂类型
	 * @since 1.0.0
	 * @see io.github.pangju666.framework.boot.jackson.utils.CryptoFactoryRegistry
	 */
	Class<? extends CryptoFactory>[] factory() default {};
}
