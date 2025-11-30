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
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.exception.base.ServiceException;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.*;

/**
 * 请求体解密注解。
 *
 * <p><strong>适用范围</strong></p>
 * <ul>
 *   <li>标注在控制器方法参数上（通常与 {@link org.springframework.web.bind.annotation.RequestBody} 联合使用）。</li>
 *   <li>仅在 Servlet Web 环境中、请求解密处理器启用时生效。</li>
 * </ul>
 *
 * <p><strong>行为说明</strong></p>
 * <ul>
 *   <li>按注解配置选择解密算法与编码方式，对请求体解密。</li>
 *   <li>支持请求体类型：String、JSON 字符串（空体保留为空或替换为空 JSON）。</li>
 *   <li>密钥支持明文或占位符（如 {@code ${app.encryption.key}}），占位符从 Spring 配置解析。</li>
 * </ul>
 *
 * <p><strong>异常说明</strong></p>
 * <ul>
 *   <li>密钥为空或占位符解析失败：抛出 {@link ServerException}。</li>
 *   <li>解密失败或编码解码失败：抛出 {@link ServiceException}。</li>
 *   <li>请求体读取异常或密钥格式非法：抛出 {@link ServerException}。</li>
 * </ul>
 *
 * <p><strong>注意事项</strong></p>
 * <ul>
 *   <li>仅支持 JSON 和 String 请求体。</li>
 *   <li>客户端需与服务端保持算法与编码一致。</li>
 *   <li>建议将密钥外置配置，避免硬编码</li>
 * </ul>
 *
 * <p><strong>使用示例</strong></p>
 * <pre>
 * {@code
 * @PostMapping("/submit")
 * public User submit(
 *     @DecryptRequestBody(
 *         key = "${app.encryption.key}",
 *         algorithm = Algorithm.AES256,
 *         encoding = Encoding.BASE64
 *     ) @RequestBody String string
 * ) {
 *     // string 为已解密的明文字符串
 *     return string;
 * }
 *
 * @PostMapping("/create")
 * public User create(
 *     @DecryptRequestBody(key = "${app.encryption.key}") @RequestBody User user
 * ) {
 *  	// user 为已解密的JSON字符串反序列化后的对象
 *     return user;
 * }
 * }
 * </pre>
 *
 * @author pangju666
 * @see CryptoAlgorithm
 * @see Encoding
 * @see RequestBody
 * @see EncryptResponseBody
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface DecryptRequestBody {
	/**
	 * 明文密钥或占位符。
	 *
	 * <p>支持两种形式：</p>
	 * <ul>
	 *   <li>明文密钥：直接传入密钥字符串，例如 {@code @DecryptRequestBody(key = "my-secret-key")}</li>
	 *   <li>占位符：使用 {@code ${property.name}} 格式，框架将从 Spring 配置读取实际密钥值，例如 {@code @DecryptRequestBody(key = "${app.encryption.key}")}</li>
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
