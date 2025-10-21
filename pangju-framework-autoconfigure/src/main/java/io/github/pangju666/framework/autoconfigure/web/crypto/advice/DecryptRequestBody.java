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

package io.github.pangju666.framework.autoconfigure.web.crypto.advice;

import io.github.pangju666.framework.autoconfigure.enums.Algorithm;
import io.github.pangju666.framework.autoconfigure.enums.Encoding;

import java.lang.annotation.*;

/**
 * 请求体解密注解
 * <p>
 * 该注解用于标记Spring MVC控制器方法中的请求体参数，指示框架应在反序列化前
 * 对加密的HTTP请求体进行解密处理。注解需配合{@link RequestBodyDecryptAdvice}使用，
 * 该通知类会在请求处理时识别此注解并进行相应的请求体解密。
 * </p>
 * <p>
 * 工作流程：
 * <ol>
 *     <li>客户端发送加密的JSON请求体</li>
 *     <li>Spring MVC框架接收到请求，识别方法参数上的该注解</li>
 *     <li>{@link RequestBodyDecryptAdvice}拦截请求在反序列化前</li>
 *     <li>根据注解配置的算法、密钥和编码方式进行解密</li>
 *     <li>将解密后的明文传递给JSON反序列化器</li>
 *     <li>最终将反序列化后的对象注入到控制器方法中</li>
 * </ol>
 * </p>
 * <p>
 * 主要特点：
 * <ul>
 *     <li>支持多种加密算法（AES256、RSA等）</li>
 *     <li>支持多种编码方式（Base64、十六进制）</li>
 *     <li>支持密钥的动态获取和配置</li>
 *     <li>提供详细的错误信息便于调试</li>
 *     <li>与Spring MVC标准的{@link org.springframework.web.bind.annotation.RequestBody}注解配合使用</li>
 * </ul>
 * </p>
 * <p>
 * 支持的加密算法和编码方式：
 * <ul>
 *     <li>{@link Algorithm#AES256} + {@link Encoding#BASE64} - AES-256加密+Base64编码（推荐）</li>
 *     <li>{@link Algorithm#AES256} + {@link Encoding#HEX} - AES-256加密+十六进制编码</li>
 *     <li>{@link Algorithm#RSA} + {@link Encoding#BASE64} - RSA非对称加密+Base64编码</li>
 *     <li>{@link Algorithm#RSA} + {@link Encoding#HEX} - RSA非对称加密+十六进制编码</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * @PostMapping("/secure-data")
 * public ResponseEntity<String> processSecureData(
 *     @DecryptRequestBody(
 *         key = "app.encryption.key",
 *         algorithm = Algorithm.AES256,
 *         encoding = Encoding.BASE64
 *     )
 *     @RequestBody UserRequest request
 * ) {
 *     // request 对象包含解密后的明文数据
 *     return ResponseEntity.ok("Data processed: " + request.getName());
 * }
 *
 * // 配置文件示例
 * // application.yml
 * // app:
 * //   encryption:
 * //     key: your-secret-key-here
 * }
 * </pre>
 * </p>
 * <p>
 * 配置对应关系：
 * <p>
 * 注解中指定的密钥属性名必须与应用配置文件（application.yml或application.properties）中的属性名相对应。
 * 框架会通过Spring的Environment从配置中动态获取密钥值。
 * </p>
 * </p>
 * <p>
 * 异常处理：
 * <ul>
 *     <li>密钥配置无效 - 抛出{@link io.github.pangju666.framework.web.exception.base.ServerException}</li>
 *     <li>密钥不存在 - 抛出{@link io.github.pangju666.framework.web.exception.base.ServerException}</li>
 *     <li>请求体读取失败 - 抛出{@link io.github.pangju666.framework.web.exception.base.ServerException}</li>
 *     <li>解密失败 - 抛出{@link io.github.pangju666.framework.web.exception.base.ServiceException}</li>
 *     <li>十六进制解码失败 - 抛出{@link io.github.pangju666.framework.web.exception.base.ServiceException}</li>
 *     <li>密钥格式错误 - 抛出{@link io.github.pangju666.framework.web.exception.base.ServerException}</li>
 * </ul>
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *     <li>注解必须标注在使用@RequestBody的方法参数上</li>
 *     <li>仅支持JSON格式的请求体</li>
 *     <li>仅在Servlet Web环境中生效</li>
 *     <li>密钥属性名不能为空（若算法需要密钥）</li>
 *     <li>建议将密钥等敏感信息存储在外部配置中，而不是硬编码</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see RequestBodyDecryptAdvice
 * @see Algorithm
 * @see Encoding
 * @see org.springframework.web.bind.annotation.RequestBody
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface DecryptRequestBody {
	/**
	 * 密钥配置属性名称
	 * <p>
	 * 指定应用配置文件中密钥的属性名。框架将从Spring的Environment中获取该属性对应的密钥值，
	 * 用于解密HTTP请求体。
	 * </p>
	 * <p>
	 * 仅当加密算法为{@link Algorithm#AES256}或{@link Algorithm#RSA}等需要密钥的算法时，
	 * 该属性才是必需的。如果指定的属性不存在或为空，则在请求处理时抛出
	 * {@link io.github.pangju666.framework.web.exception.base.ServerException}异常。
	 * </p>
	 * <p>
	 * 配置示例：
	 * <pre>
	 * {@code
	 * # application.yml
	 * app:
	 *   encryption:
	 *     key: my-secret-key-12345
	 *
	 * # 注解中
	 * @DecryptRequestBody(key = "app.encryption.key")
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
	 * 指定用于解密HTTP请求体的算法。支持的算法包括：
	 * <ul>
	 *     <li>{@link Algorithm#AES256} - AES-256对称加密（默认，推荐）</li>
	 *     <li>{@link Algorithm#RSA} - RSA非对称加密</li>
	 * </ul>
	 * 不同算法有不同的性能特性和应用场景。对称加密通常性能更好，适合高频通信；
	 * 非对称加密安全性更高但性能较差，适合敏感数据或初始密钥交换。
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
	 * 指定HTTP请求体中密文的编码方式。支持的编码方式包括：
	 * <ul>
	 *     <li>{@link Encoding#BASE64} - Base64编码（默认，推荐）</li>
	 *     <li>{@link Encoding#HEX} - 十六进制编码</li>
	 * </ul>
	 * 框架将先使用此编码方式对密文进行解码，将其转换为二进制数据，
	 * 然后再进行算法解密操作。
	 * </p>
	 * <p>
	 * Base64编码具有更好的可读性和传输效率，十六进制编码更易于调试。
	 * </p>
	 *
	 * @return 密文编码方式，默认为{@link Encoding#BASE64}
	 * @see Encoding
	 * @since 1.0.0
	 */
	Encoding encoding() default Encoding.BASE64;
}
