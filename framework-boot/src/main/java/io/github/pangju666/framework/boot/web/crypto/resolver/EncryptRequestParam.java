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

package io.github.pangju666.framework.boot.web.crypto.resolver;

import io.github.pangju666.framework.boot.enums.Algorithm;
import io.github.pangju666.framework.boot.enums.Encoding;

import java.lang.annotation.*;

/**
 * 加密请求参数注解
 * <p>
 * 该注解用于标记Spring MVC控制器方法中的加密字符串类型参数，指示参数解析器应将HTTP请求中的加密参数
 * 解密后注入方法。注解需配合{@link EncryptRequestParamArgumentResolver}使用，
 * 该解析器会在请求处理时识别此注解并进行相应的参数解析和解密。
 * </p>
 * <p>
 * 支持的加密算法：
 * <ul>
 *     <li>{@link Algorithm#AES256} - AES-256对称加密（默认）</li>
 *     <li>{@link Algorithm#RSA} - RSA非对称加密</li>
 * </ul>
 * </p>
 * <p>
 * 支持的编码方式：
 * <ul>
 *     <li>{@link Encoding#BASE64} - Base64编码（默认）</li>
 *     <li>{@link Encoding#HEX} - 十六进制编码</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * @PostMapping("/secure-data")
 * public ResponseEntity<?> processSecureData(
 *     @EncryptRequestParam(
 *         value = "userData",
 *         key = "app.encryption.key",
 *         algorithm = Algorithm.AES256,
 *         encoding = Encoding.BASE64,
 *         required = true
 *     ) String decryptedUserData
 * ) {
 *     // decryptedUserData 参数将被自动解密
 *     return ResponseEntity.ok(decryptedUserData);
 * }
 * }
 * </pre>
 * </p>
 *
 * @author pangju666
 * @see EncryptRequestParamArgumentResolver
 * @see Algorithm
 * @see Encoding
 * @since 1.0.0
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EncryptRequestParam {
	/**
	 * 请求参数的名称
	 * <p>
	 * 指定HTTP请求中参数的名称。如果不指定或为空字符串，则默认使用方法参数的名称。
	 * 参数解析器会根据此名称从请求中提取加密的参数值。
	 * </p>
	 *
	 * @return 请求参数名称，默认为空字符串（使用方法参数名）
	 * @since 1.0.0
	 */
	String value() default "";

	/**
	 * 参数是否为必需
	 * <p>
	 * 当设置为true时，如果请求中缺少该参数，则抛出
	 * {@link org.springframework.web.bind.MissingServletRequestParameterException}异常。
	 * 当设置为false时，缺失的参数返回{@link #defaultValue()}指定的默认值。
	 * </p>
	 *
	 * @return 是否为必需参数，默认为true
	 * @since 1.0.0
	 */
	boolean required() default true;

	/**
	 * 参数的默认值
	 * <p>
	 * 当请求中未提供参数值时，使用该默认值。仅当{@link #required()}为false时，
	 * 缺失的参数才会返回此默认值。如果{@link #required()}为true且参数缺失，
	 * 即使指定了默认值也会抛出{@link org.springframework.web.bind.MissingServletRequestParameterException}异常。
	 * </p>
	 *
	 * @return 默认值，默认为空字符串
	 * @since 1.0.0
	 */
	String defaultValue() default "";

	/**
	 * 密钥配置属性名称
	 * <p>
	 * 指定应用配置文件中密钥的属性名。解析器将从Spring的Environment中获取该属性对应的密钥值。
	 * 该密钥用于解密请求参数。
	 * </p>
	 * <p>
	 * 仅当加密算法为{@link Algorithm#AES256}或{@link Algorithm#RSA}时需要指定此属性。
	 * 如果指定的属性不存在或为空，则抛出{@link io.github.pangju666.framework.web.exception.base.ServerException}异常。
	 * </p>
	 * <p>
	 * 示例：
	 * <pre>
	 * {@code
	 * // 配置文件中
	 * app.encryption.key=your-secret-key-here
	 *
	 * // 注解中
	 * @EncryptRequestParam(key = "app.encryption.key")
	 * }
	 * </pre>
	 * </p>
	 *
	 * @return 密钥属性名，默认为空字符串
	 * @since 1.0.0
	 */
	String key() default "";

	/**
	 * 解密算法
	 * <p>
	 * 指定用于解密请求参数的算法。支持的算法包括：
	 * <ul>
	 *     <li>{@link Algorithm#AES256} - AES-256对称加密（默认）</li>
	 *     <li>{@link Algorithm#RSA} - RSA非对称加密</li>
	 * </ul>
	 * 不同算法有不同的密钥要求和性能特性。
	 * </p>
	 *
	 * @return 解密算法，默认为{@link Algorithm#AES256}
	 * @see Algorithm
	 * @since 1.0.0
	 */
	Algorithm algorithm() default Algorithm.AES256;

	/**
	 * 密文编码方式
	 * <p>
	 * 指定请求参数中密文的编码方式。支持的编码方式包括：
	 * <ul>
	 *     <li>{@link Encoding#BASE64} - Base64编码（默认，推荐）</li>
	 *     <li>{@link Encoding#HEX} - 十六进制编码</li>
	 * </ul>
	 * 解析器将先使用此编码方式对密文进行解码，然后再进行解密操作。
	 * </p>
	 *
	 * @return 密文编码方式，默认为{@link Encoding#BASE64}
	 * @see Encoding
	 * @since 1.0.0
	 */
	Encoding encoding() default Encoding.BASE64;
}
