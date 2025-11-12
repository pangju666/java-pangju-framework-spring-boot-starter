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
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import io.github.pangju666.framework.boot.crypto.factory.impl.AES256CryptoFactory;
import io.github.pangju666.framework.boot.enums.Algorithm;
import io.github.pangju666.framework.boot.enums.Encoding;
import io.github.pangju666.framework.boot.jackson.serializer.EncryptJsonSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JSON字段加密注解，用于在JSON序列化过程中对指定字段进行加密操作
 * <p>
 * 该注解可以应用于类的字段上，指定字段在JSON序列化时将使用{@link EncryptJsonSerializer}
 * 进行加密处理。加密过程将根据配置的密钥、算法和编码方式进行。
 * </p>
 *
 * @author pangju666
 * @see EncryptJsonSerializer
 * @see Algorithm
 * @see Encoding
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
	 * 用于加密的算法
	 * <p>
	 * 默认使用AES256算法
	 * </p>
	 *
	 * @return 加密算法
	 * @since 1.0.0
	 */
	Algorithm algorithm() default Algorithm.AES256;

	/**
	 * 加密内容的编码方式
	 * <p>
	 * 默认使用BASE64编码。
	 * </p>
	 * <p>
	 * 注意：仅在加密字符串时生效，对二进制与数值类型不适用。
	 * </p>
	 *
	 * @return 编码方式
	 * @since 1.0.0
	 */
	Encoding encoding() default Encoding.BASE64;

	/**
	 * 自定义加密工厂
	 * <p>
	 * 当 {@link Algorithm#CUSTOM} 被指定为算法时，使用该工厂提供的实现进行加密；
	 * 其他算法将忽略此配置并使用预设工厂。
	 * </p>
	 *
	 * @return 自定义加密工厂类型
	 * @since 1.0.0
	 */
	Class<? extends CryptoFactory> factory() default AES256CryptoFactory.class;
}
