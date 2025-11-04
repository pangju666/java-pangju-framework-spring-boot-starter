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

package io.github.pangju666.framework.boot.web.signature.enums;

import io.github.pangju666.framework.boot.web.signature.annotation.Signature;

/**
 * 签名校验类型枚举。
 * <p>
 * 定义了签名字段在 HTTP 请求中的存在位置，用于对签名信息的提取和校验。开发者可根据实际需求，选择特定的签名校验类型。
 * </p>
 *
 * <p>支持的校验类型包括：</p>
 * <ul>
 *     <li>{@link #PARAMS}：签名信息从请求参数中获取。</li>
 *     <li>{@link #HEADER}：签名信息从请求头中提取。</li>
 *     <li>{@link #ANY}：签名信息可从请求参数或请求头中获取。</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>配置在 {@link Signature} 注解上，指明签名校验的目标位置。</li>
 *     <li>结合签名拦截器实现对请求的校验。</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * @Signature(
 *     appId = {"my-app-id"},
 *     type = SignatureType.HEADER,  // 从请求头中获取签名信息
 *     algorithm = SignatureAlgorithm.SHA256,
 *     timeout = 5,
 *     timeUnit = TimeUnit.MINUTES
 * )
 * public ResponseEntity<?> handleRequest() {
 *     // 签名校验通过后执行处理逻辑
 *     return ResponseEntity.ok("Success");
 * }
 * }
 * </pre>
 *
 * @author pangju666
 * @see Signature
 * @since 1.0.0
 */
public enum SignatureType {
	/**
	 * 签名信息位于请求参数中。
	 * <p>适合以查询参数或表单提交参数方式发送签名。</p>
	 *
	 * @since 1.0.0
	 */
	PARAMS,
	/**
	 * 签名信息存储在请求头中。
	 * <p>适用于高安全性环境，避免签名信息暴露在请求参数中。</p>
	 *
	 * @since 1.0.0
	 */
	HEADER,
	/**
	 * 签名信息可从请求参数或请求头中获取。
	 * <p>灵活适配请求，允许使用多种方式传递签名信息。</p>
	 *
	 * @since 1.0.0
	 */
	ANY
}
