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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.pangju666.framework.boot.crypto.factory.impl.AES256CryptoFactory;
import io.github.pangju666.framework.boot.crypto.enums.CryptoAlgorithm;
import io.github.pangju666.framework.boot.crypto.enums.Encoding;
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import io.github.pangju666.framework.boot.jackson.deserializer.DecryptJsonDeserializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JSON字段解密注解，用于在JSON反序列化过程中对指定字段进行解密操作
 * <p>
 * 该注解可以应用于类的字段上，指定字段在JSON反序列化时将使用{@link DecryptJsonDeserializer}
 * 进行解密处理。解密过程将根据配置的密钥、算法和编码方式进行。
 * </p>
 * <p>
 * 支持的字段类型（依据 {@link DecryptJsonDeserializer} 的类型分派）：
 * </p>
 * <ul>
 *   <li>标量类型：{@link String}、<code>byte[]</code>、{@link java.math.BigInteger}、{@link java.math.BigDecimal}</li>
 *   <li>集合类型：{@link java.util.List}&lt;T&gt;、{@link java.util.Set}&lt;T&gt;、{@link java.util.Collection}&lt;T&gt;，其中 T 为上述受支持类型</li>
 *   <li>映射类型：{@link java.util.Map}&lt;String, T&gt;（键为字符串），其中 T 为上述受支持类型</li>
 * </ul>
 * <p>
 * 说明：对于 {@link java.math.BigInteger} 与 {@link java.math.BigDecimal}，既支持字符串输入也支持数值输入；
 * 当 JSON 值为 <code>null</code> 时反序列化结果为 <code>null</code>。
 * </p>
 *
 * @author pangju666
 * @see EncryptFormat
 * @see CryptoAlgorithm
 * @see Encoding
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonDeserialize(using = DecryptJsonDeserializer.class)
public @interface DecryptFormat {
	/**
	 * 明文密钥或占位符。
	 *
	 * <p>支持两种形式：</p>
	 * <ul>
	 *   <li>明文密钥：直接传入密钥字符串，例如 {@code @DecryptFormat(key = "my-secret-key")}</li>
	 *   <li>占位符：使用 {@code ${property.name}} 格式，框架将从 Spring 配置读取实际密钥值，例如 {@code @DecryptFormat(key = "${app.encryption.key}")}</li>
	 * </ul>
	 *
	 * @return 密钥或占位符字符串
	 * @since 1.0.0
	 */
	String key();

	/**
	 * 用于解密的算法
	 * <p>
	 * 默认使用AES256算法
	 * </p>
	 *
	 * @return 解密算法
	 * @since 1.0.0
	 */
	CryptoAlgorithm algorithm() default CryptoAlgorithm.AES256;

	/**
	 * 加密内容的编码方式
	 * <p>
	 * 默认使用BASE64编码。
	 * </p>
	 * <p>
	 * 注意：仅在解密字符串时生效，对二进制与数值类型不适用。
	 * </p>
	 *
	 * @return 编码方式
	 * @since 1.0.0
	 */
	Encoding encoding() default Encoding.BASE64;

	/**
	 * 自定义解密工厂
	 * <p>
	 * 当 {@link CryptoAlgorithm#CUSTOM} 被指定为算法时，使用该工厂提供的实现进行解密；
	 * 其他算法将忽略此配置并使用预设工厂。
	 * </p>
	 * <p>
	 * <strong>要求：</strong>指定的类必须是 Spring Bean（已注册到容器中）
	 * </p>
	 *
	 * @return 自定义解密工厂类型
	 * @since 1.0.0
	 */
	Class<? extends CryptoFactory> factory() default AES256CryptoFactory.class;
}
