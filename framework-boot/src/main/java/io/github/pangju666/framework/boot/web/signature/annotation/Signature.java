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

package io.github.pangju666.framework.boot.web.signature.annotation;

import io.github.pangju666.framework.boot.web.signature.enums.SignatureAlgorithm;
import io.github.pangju666.framework.boot.web.signature.enums.SignatureType;
import io.github.pangju666.framework.boot.web.signature.interceptor.SignatureInterceptor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 接口签名校验注解。
 * <p>
 * 用于标注需要进行签名校验的类或方法，通过对请求的数据进行签名计算与校验，保证接口调用的安全性。
 * 该注解支持灵活配置如应用 ID、签名算法、签名位置、超时时间与签名有效期等。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *     <li>指定应用 ID（{@code appId}）列表，用于匹配请求的合法调用方。</li>
 *     <li>配置签名字段的存在位置（参数或请求头）。</li>
 *     <li>支持多种哈希算法生成签名，如 {@link SignatureAlgorithm#SHA256}。</li>
 *     <li>设置请求的签名校验有效期，避免重复请求或时效性攻击。</li>
 * </ul>
 *
 * <p>适用场景：</p>
 * <ul>
 *     <li>保护敏感接口避免被非法访问，例如支付接口、用户管理接口等。</li>
 *     <li>校验第三方客户端发起的请求是否被篡改。</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * @Signature(
 *     appId = {"my-app-id", "another-app-id"},  // 指定支持的应用 ID
 *     type = SignatureType.HEADER,  // 从请求头中读取签名
 *     algorithm = SignatureAlgorithm.SHA256,  // 使用SHA256算法计算签名
 *     timeout = 5,  // 签名超时时间为5分钟
 *     timeUnit = TimeUnit.MINUTES  // 超时时间单位为分钟
 * )
 * public ResponseEntity<?> secureEndpoint(@RequestBody Map<String, Object> data) {
 *     return ResponseEntity.ok("Success");
 * }
 * }
 * </pre>
 *
 * @author pangju666
 * @see SignatureType
 * @see SignatureAlgorithm
 * @see SignatureInterceptor
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Signature {
	/**
	 * 指定支持的应用 ID 列表。
	 * <p>
	 * 请求中应用 ID 必须与此处配置的 {@code appId} 匹配，校验方可通过。
	 * </p>
	 *
	 * @return 应用 ID 数组。
	 * @since 1.0.0
	 */
	String[] appId();

	/**
	 * 签名的存在位置。
	 * <p>
	 * 可选值包括：
	 * <ul>
	 *     <li>{@link SignatureType#PARAMS}：从请求参数中读取签名。</li>
	 *     <li>{@link SignatureType#HEADER}：从请求头中读取签名。</li>
	 *     <li>{@link SignatureType#ANY}：允许从请求参数或请求头中读取签名。</li>
	 * </ul>
	 * 默认值为 {@link SignatureType#ANY}。
	 * </p>
	 *
	 * @return 签名校验类型。
	 * @since 1.0.0
	 */
	SignatureType type() default SignatureType.ANY;

	/**
	 * 签名算法。
	 * <p>
	 * 指定用于生成校验签名的哈希算法，例如 SHA256 或 MD5。推荐使用 {@link SignatureAlgorithm#SHA256}。
	 * 默认值为 {@link SignatureAlgorithm#SHA256}。
	 * </p>
	 *
	 * @return 签名校验算法。
	 * @since 1.0.0
	 */
	SignatureAlgorithm algorithm() default SignatureAlgorithm.SHA256;

	/**
	 * 签名超时时间。
	 * <p>
	 * 请求的时间戳超过此时间将被拒绝，单位由 {@code timeUnit} 决定。
	 * </p>
	 *
	 * @return 签名超时时间。
	 * @since 1.0.0
	 */
	long timeout() default 1;

	/**
	 * 超时单位。
	 * <p>
	 * 指定 {@code timeout} 的时间单位，例如秒、分钟等。
	 * 默认值为分钟（{@link TimeUnit#MINUTES}）。
	 * </p>
	 *
	 * @return 超时单位。
	 * @since 1.0.0
	 */
	TimeUnit timeUnit() default TimeUnit.MINUTES;
}