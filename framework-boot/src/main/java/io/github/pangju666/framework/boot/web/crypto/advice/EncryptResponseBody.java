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

package io.github.pangju666.framework.boot.web.crypto.advice;

import io.github.pangju666.framework.boot.enums.Algorithm;
import io.github.pangju666.framework.boot.enums.Encoding;
import io.github.pangju666.framework.web.model.common.Result;

import java.lang.annotation.*;

/**
 * 响应体加密注解
 * <p>
 * 该注解用于标记Spring MVC控制器方法，指示框架应在序列化HTTP响应体时对其进行加密处理。
 * 该通知类会在响应处理时识别此注解并进行相应的响应体加密。
 * </p>
 * <p>
 * 工作流程：
 * <ol>
 *     <li>控制器方法返回响应对象</li>
 *     <li>Spring MVC框架识别方法上的该注解</li>
 *     <li>根据注解配置的算法、密钥和编码方式进行加密</li>
 *     <li>将加密后的密文序列化为JSON传递给客户端</li>
 * </ol>
 * </p>
 * <p>
 * 主要特点：
 * <ul>
 *     <li>支持多种加密算法（AES256、RSA等）</li>
 *     <li>支持多种编码方式（Base64、十六进制）</li>
 *     <li>支持密钥的动态获取和配置</li>
 *     <li>支持多种响应类型（String、byte[]、Result、JSON对象等）</li>
 *     <li>提供详细的错误信息便于调试</li>
 *     <li>仅能标注在方法上，不能标注在类上</li>
 * </ul>
 * </p>
 * <p>
 * 支持的响应类型处理：
 * <ul>
 *     <li>String类型 - 直接加密字符串内容，返回加密后的字符串</li>
 *     <li>byte[]类型 - 直接加密字节数组内容，返回加密后的字节数组</li>
 *     <li>{@link Result}类型 - 仅加密Result对象中的data字段，保留code和msg字段不变</li>
 *     <li>其他JSON对象 - 先序列化为JSON字符串，再加密，返回加密后的字节数组</li>
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
 * @RestController
 * @RequestMapping("/api")
 * public class UserController {
 *     // 单个方法加密
 *     @GetMapping("/user/{id}")
 *     @EncryptResponseBody(
 *         key = "app.encryption.key",
 *         algorithm = Algorithm.AES256,
 *         encoding = Encoding.BASE64
 *     )
 *     public Result<UserResponse> getUserById(@PathVariable String id) {
 *         UserResponse user = userService.findById(id);
 *         return Result.ok(user);
 *     }
 *
 *     // 返回String类型的响应
 *     @GetMapping("/token")
 *     @EncryptResponseBody(key = "app.encryption.key")
 *     public String generateToken() {
 *         return "secret-token-12345";
 *     }
 *
 *     // 返回字节数组的响应
 *     @GetMapping("/file")
 *     @EncryptResponseBody(key = "app.encryption.key")
 *     public byte[] downloadEncryptedFile() {
 *         return fileContent;
 *     }
 * }
 * }
 * </pre>
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * {@code
 * # application.yml
 * app:
 *   encryption:
 *     key: your-secret-key-here
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
 *     <li>加密失败 - 抛出{@link io.github.pangju666.framework.web.exception.base.ServerException}</li>
 *     <li>密钥格式错误 - 抛出{@link io.github.pangju666.framework.web.exception.base.ServerException}</li>
 * </ul>
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *     <li>注解只能标注在控制器方法上，不能标注在类或字段上</li>
 *     <li>仅在Servlet Web环境中生效</li>
 *     <li>密钥属性名不能为空（若算法需要密钥）</li>
 *     <li>空响应体将不进行加密处理</li>
 *     <li>对于Result类型响应，仅data字段被加密，code和msg保持不变</li>
 *     <li>建议将密钥等敏感信息存储在外部配置中，而不是硬编码</li>
 *     <li>加密和解密的算法、编码方式必须一致，否则客户端无法解密</li>
 * </ul>
 * </p>
 * <p>
 * 与其他注解的配合：
 * <ul>
 *     <li>可与{@link org.springframework.web.bind.annotation.RequestMapping}配合使用</li>
 *     <li>可与{@link org.springframework.web.bind.annotation.GetMapping}、
 *         {@link org.springframework.web.bind.annotation.PostMapping}等注解配合使用</li>
 *     <li>如需对请求进行解密，可配合{@link DecryptRequestBody}使用</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see DecryptRequestBody
 * @see Algorithm
 * @see Encoding
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface EncryptResponseBody {
	/**
	 * 密钥配置属性名称
	 * <p>
	 * 指定应用配置文件中密钥的属性名。框架将从Spring的Environment中获取该属性对应的密钥值，
	 * 用于加密HTTP响应体。
	 * </p>
	 * <p>
	 * 仅当加密算法为{@link Algorithm#AES256}或{@link Algorithm#RSA}等需要密钥的算法时，
	 * 该属性才是必需的。如果指定的属性不存在或为空，则在响应处理时抛出
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
	 * @EncryptResponseBody(key = "app.encryption.key")
	 * }
	 * </pre>
	 * </p>
	 *
	 * @return 密钥属性名，默认为空字符串
	 * @since 1.0.0
	 */
	String key() default "";

	/**
	 * 加密算法
	 * <p>
	 * 指定用于加密HTTP响应体的算法。支持的算法包括：
	 * <ul>
	 *     <li>{@link Algorithm#AES256} - AES-256对称加密（默认，推荐）</li>
	 *     <li>{@link Algorithm#RSA} - RSA非对称加密</li>
	 * </ul>
	 * 不同算法有不同的性能特性和应用场景。对称加密通常性能更好，适合高频通信；
	 * 非对称加密安全性更高但性能较差，适合敏感数据或初始密钥交换。
	 * </p>
	 * <p>
	 * 加密和解密端必须使用相同的算法，否则客户端无法解密。
	 * </p>
	 *
	 * @return 加密算法，默认为{@link Algorithm#AES256}
	 * @see Algorithm
	 * @since 1.0.0
	 */
	Algorithm algorithm() default Algorithm.AES256;

	/**
	 * 密文编码方式
	 * <p>
	 * 指定HTTP响应体中密文的编码方式。支持的编码方式包括：
	 * <ul>
	 *     <li>{@link Encoding#BASE64} - Base64编码（默认，推荐）</li>
	 *     <li>{@link Encoding#HEX} - 十六进制编码</li>
	 * </ul>
	 * 框架将使用此编码方式对加密后的二进制密文进行编码，以便于在JSON中传输。
	 * Base64编码具有更好的可读性和传输效率，十六进制编码更易于调试。
	 * </p>
	 * <p>
	 * 加密和解密端必须使用相同的编码方式，否则客户端无法正确解码。
	 * </p>
	 *
	 * @return 密文编码方式，默认为{@link Encoding#BASE64}
	 * @see Encoding
	 * @since 1.0.0
	 */
	Encoding encoding() default Encoding.BASE64;
}
