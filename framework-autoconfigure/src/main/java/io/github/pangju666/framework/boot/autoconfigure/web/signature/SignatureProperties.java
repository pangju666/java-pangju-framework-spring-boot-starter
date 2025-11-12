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

package io.github.pangju666.framework.boot.autoconfigure.web.signature;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 签名功能的配置属性类。
 * <p>
 * 通过配置前缀 {@code pangju.web.signature}，自动从配置文件中加载签名相关的属性。
 * 该配置类定义了签名校验中使用的请求头名称、参数名称以及密钥信息。
 * </p>
 *
 * <p>配置项包括：</p>
 * <ul>
 *     <li>{@code signatureHeaderName}：HTTP 请求头中签名字段的名称，默认值为 {@code X-Signature}。</li>
 *     <li>{@code appIdHeaderName}：HTTP 请求头中应用 ID 字段的名称，默认值为 {@code X-App-Id}。</li>
 *     <li>{@code timestampHeaderName}：HTTP 请求头中时间戳字段的名称，默认值为 {@code X-Timestamp}。</li>
 *     <li>{@code signatureParamName}：HTTP 请求参数中的签名字段名称，默认值为 {@code apiSignature}。</li>
 *     <li>{@code appIdParamName}：HTTP 请求参数中的应用 ID 字段名称，默认值为 {@code apiAppId}。</li>
 *     <li>{@code secretKeys}：应用 ID 及对应密钥的配置。</li>
 * </ul>
 *
 * <p>适用场景：</p>
 * <ul>
 *     <li>定义 HTTP 请求中签名相关字段名称，支持灵活自定义。</li>
 *     <li>配置应用 ID 与其对应密钥，用于校验请求签名的正确性。</li>
 * </ul>
 *
 * <p>配置示例（YAML）：</p>
 * <pre><code>
 * pangju:
 *   web:
 *     signature:
 *       signature-header-name: X-Signature
 *       app-id-header-name: X-App-Id
 *       timestamp-header-name: X-Timestamp
 *       signature-param-name: apiSignature
 *       app-id-param-name: apiAppId
 *       secret-keys:
 *         app1: secretKey1
 *         app2: secretKey2
 * </code></pre>
 *
 * @author pangju666
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "pangju.web.signature")
public class SignatureProperties {
	/**
	 * HTTP 请求头中签名字段的名称。
	 * <p>默认值为 {@code X-Signature}。</p>
	 *
	 * @since 1.0.0
	 */
	private String signatureHeaderName = "X-Signature";
	/**
	 * HTTP 请求头中应用 ID 字段的名称。
	 * <p>默认值为 {@code X-App-Id}。</p>
	 *
	 * @since 1.0.0
	 */
	private String appIdHeaderName = "X-App-Id";
	/**
	 * HTTP 请求头中时间戳字段的名称。
	 * <p>默认值为 {@code X-Timestamp}。</p>
	 *
	 * @since 1.0.0
	 */
	private String timestampHeaderName = "X-Timestamp";
	/**
	 * HTTP 请求参数中的签名字段名称。
	 * <p>默认值为 {@code apiSignature}。</p>
	 *
	 * @since 1.0.0
	 */
	private String signatureParamName = "apiSignature";
	/**
	 * HTTP 请求参数中的应用 ID 字段名称。
	 * <p>默认值为 {@code apiAppId}。</p>
	 *
	 * @since 1.0.0
	 */
	private String appIdParamName = "apiAppId";
	/**
	 * 配置应用 ID 及对应的密钥信息。
	 * <p>
	 * 例如（YAML）：
	 * <pre><code>
	 * pangju:
	 *   web:
	 *     signature:
	 *       secret-keys:
	 *         app1: secretKey1
	 *         app2: secretKey2
	 * </code></pre>
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private Map<String, String> secretKeys;

	public Map<String, String> getSecretKeys() {
		return secretKeys;
	}

	public void setSecretKeys(Map<String, String> secretKeys) {
		this.secretKeys = secretKeys;
	}

	public String getSignatureHeaderName() {
		return signatureHeaderName;
	}

	public void setSignatureHeaderName(String signatureHeaderName) {
		this.signatureHeaderName = signatureHeaderName;
	}

	public String getAppIdHeaderName() {
		return appIdHeaderName;
	}

	public void setAppIdHeaderName(String appIdHeaderName) {
		this.appIdHeaderName = appIdHeaderName;
	}

	public String getTimestampHeaderName() {
		return timestampHeaderName;
	}

	public void setTimestampHeaderName(String timestampHeaderName) {
		this.timestampHeaderName = timestampHeaderName;
	}

	public String getSignatureParamName() {
		return signatureParamName;
	}

	public void setSignatureParamName(String signatureParamName) {
		this.signatureParamName = signatureParamName;
	}

	public String getAppIdParamName() {
		return appIdParamName;
	}

	public void setAppIdParamName(String appIdParamName) {
		this.appIdParamName = appIdParamName;
	}
}
