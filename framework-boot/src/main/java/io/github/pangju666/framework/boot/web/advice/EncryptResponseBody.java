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

package io.github.pangju666.framework.boot.web.advice;

import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import io.github.pangju666.framework.boot.crypto.factory.impl.AES256CryptoFactory;
import io.github.pangju666.framework.boot.enums.Algorithm;
import io.github.pangju666.framework.boot.enums.Encoding;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.model.Result;

import java.lang.annotation.*;

/**
 * 响应体加密注解。
 *
 * <p><strong>适用范围</strong></p>
 * <ul>
 *   <li>用于标注在控制器类或方法上，配合响应体处理器在序列化 HTTP 响应时进行加密</li>
 *   <li>仅在 Servlet Web 环境中、响应加密处理器启用时生效</li>
 * </ul>
 *
 * <p><strong>行为说明</strong></p>
 * <ul>
 *   <li>按照注解配置选择加密算法与编码方式，对响应体进行加密</li>
 *   <li>支持的响应类型：
 *     <ul>
 *       <li>{@code String}：直接加密字符串</li>
 *       <li>{@code byte[]}：直接加密字节数组</li>
 *       <li>{@link Result}：仅加密 {@code data} 字段，保留 {@code code} 与 {@code msg}</li>
 *       <li>其他对象：先序列化为 JSON 字符串后加密</li>
 *     </ul>
 *   </li>
 *   <li>空响应体不加密</li>
 * </ul>
 *
 * <p><strong>异常说明</strong></p>
 * <ul>
 *   <li>密钥配置缺失或无效、占位符解析失败、加密失败等，将抛出 {@link ServerException}</li>
 * </ul>
 *
 * <p><strong>注意事项</strong></p>
 * <ul>
 *   <li>类或方法上均可使用；标注在类上时，作用于该类的所有处理方法</li>
 *   <li>客户端需使用与服务端一致的算法与编码进行解密</li>
 *   <li>建议将密钥存储在外部配置中，避免硬编码</li>
 * </ul>
 *
 * @author pangju666
 * @see DecryptRequestBody
 * @see Algorithm
 * @see Encoding
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface EncryptResponseBody {
	/**
	 * 明文密钥或占位符。
	 *
	 * <p>支持两种形式：</p>
	 * <ul>
	 *   <li>明文密钥：直接传入密钥字符串，例如 {@code @EncryptResponseBody(key = "my-secret-key")}</li>
	 *   <li>占位符：使用 {@code ${property.name}} 格式，框架将从 Spring 配置读取实际密钥值，例如 {@code @EncryptResponseBody(key = "${app.encryption.key}")}</li>
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
	 * @return 解密算法
	 * @since 1.0.0
	 */
	Algorithm algorithm() default Algorithm.AES256;

	/**
	 * 加密内容的编码方式
	 * <p>
	 * 默认使用BASE64编码。
	 * </p>
	 * <p>
	 * 注意：仅在加密字符串时生效，对二进制类型不适用。
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
