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

import io.github.pangju666.framework.boot.crypto.enums.CryptoAlgorithm;
import io.github.pangju666.framework.boot.crypto.enums.Encoding;
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import io.github.pangju666.framework.boot.web.crypto.EncryptRequestParamArgumentResolver;
import io.github.pangju666.framework.web.exception.base.ServiceException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.lang.annotation.*;

/**
 * 加密请求参数注解
 * <p>
 * 标注为加密传输的请求参数，在解析时进行解密并注入到方法参数。
 * 需配合 {@link EncryptRequestParamArgumentResolver} 使用。
 * </p>
 * <p>
 * 适用范围：Spring MVC 控制器方法参数，仅支持 {@link String} 类型。
 * </p>
 * <p>
 * 行为说明：根据 {@link #value()} 指定的名称从 HTTP 请求中读取加密字符串，
 * 按密钥、算法与编码进行解密后作为方法参数值；当 {@link #required()} 为 false 且参数缺失时，返回 {@link #defaultValue()}。
 * 密钥支持明文或占位符（形如 {@code ${...}}，从 Spring 配置解析）。
 * </p>
 * <p>
 * 异常说明：
 * <li>当 {@link #required()} 为 true 且请求中缺少参数时，抛出{@link MissingServletRequestParameterException}。</li>
 * <li>密钥配置缺失或无效、占位符解析失败、加密失败等，将抛出 {@link ServiceException}</li>
 * </p>
 * <p>
 * 注意事项：仅支持字符串类型参数；请确保客户端与服务端在算法与编码方式上保持一致。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * @PostMapping("/secure-data")
 * public ResponseEntity<?> processSecureData(
 *     @EncryptRequestParam(
 *         value = "userData",
 *         key = "${app.encryption.key}",
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
 * @see CryptoAlgorithm
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
	 * {@link MissingServletRequestParameterException}异常。
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
	 * 即使指定了默认值也会抛出{@link MissingServletRequestParameterException}异常。
	 * </p>
	 *
	 * @return 默认值，默认为空字符串
	 * @since 1.0.0
	 */
	String defaultValue() default "";

	/**
	 * 明文密钥或占位符。
	 *
	 * <p>支持两种形式：</p>
	 * <ul>
	 *   <li>明文密钥：直接传入密钥字符串，例如 {@code @EncryptRequestParam(key = "my-secret-key")}</li>
	 *   <li>占位符：使用 {@code ${property.name}} 格式，框架将从 Spring 配置读取实际密钥值，例如 {@code @EncryptRequestParam(key = "${app.encryption.key}")}</li>
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
	 * 解密内容的编码方式
	 * <p>
	 * 默认使用BASE64编码。
	 * </p>
	 *
	 * @return 编码方式
	 * @since 1.0.0
	 */
	Encoding encoding() default Encoding.BASE64;

	/**
	 * 自定义加密工厂。
	 *
	 * <p>优先级：当提供工厂类型时，始终使用该 {@link CryptoFactory} Bean，忽略算法枚举关联的工厂；未提供时按算法枚举使用预设工厂。</p>
	 *
	 * <p><strong>要求</strong>：指定的类必须是 Spring Bean（已注册到容器中）。</p>
	 * <p><strong>默认与行为</strong>：未指定则使用算法默认工厂；如提供多个类型，仅取第一个作为目标工厂。</p>
	 *
	 * @return 自定义加密工厂类型
	 * @since 1.0.0
	 */
	Class<? extends CryptoFactory>[] factory() default {};
}
