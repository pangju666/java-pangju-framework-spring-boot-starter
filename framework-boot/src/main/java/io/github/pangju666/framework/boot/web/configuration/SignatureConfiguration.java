package io.github.pangju666.framework.boot.web.configuration;

import io.github.pangju666.framework.boot.web.annotation.Signature;
import io.github.pangju666.framework.boot.web.enums.SignatureAlgorithm;

/**
 * HTTP 签名配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>用于统一定义签名、应用 ID、时间戳的请求头/参数名，供签名校验组件读取。</li>
 *   <li>与拦截器、过滤器或切面等签名处理器配合使用。</li>
 * </ul>
 *
 * <p><strong>字段说明</strong></p>
 * <ul>
 *   <li>{@link #signatureHeaderName}：HTTP 请求头中的签名字段名称。</li>
 *   <li>{@link #appIdHeaderName}：HTTP 请求头中的应用 ID 字段名称。</li>
 *   <li>{@link #timestampHeaderName}：HTTP 请求头中的时间戳字段名称。</li>
 *   <li>{@link #signatureParamName}：HTTP 请求参数中的签名字段名称。</li>
 *   <li>{@link #appIdParamName}：HTTP 请求参数中的应用 ID 字段名称。</li>
 * </ul>
 *
 * <p><strong>使用示例</strong></p>
 * <pre><code>
 * SignatureConfiguration config = new SignatureConfiguration();
 * config.setSignatureHeaderName("X-Signature");
 * config.setAppIdHeaderName("X-App-Id");
 * config.setTimestampHeaderName("X-Timestamp");
 * config.setSignatureParamName("sign");
 * config.setAppIdParamName("appId");
 * </code></pre>
 *
 * <p><strong>注意事项</strong></p>
 * <ul>
 *   <li>建议与客户端约定统一字段名，避免头与参数混用导致歧义。</li>
 *   <li>具体读取优先级（Header 或 Param）由签名处理器实现决定。</li>
 *   <li>摘要计算建议使用 {@link SignatureAlgorithm#SHA256} 或 {@link SignatureAlgorithm#SHA512}。</li>
 * </ul>
 *
 * @author pangju666
 * @see Signature
 * @see SignatureAlgorithm
 * @since 1.0.0
 */
public class SignatureConfiguration {
	/**
	 * HTTP 请求头中签名字段的名称。
	 *
	 * @since 1.0.0
	 */
	private String signatureHeaderName;
	/**
	 * HTTP 请求头中应用 ID 字段的名称。
	 *
	 * @since 1.0.0
	 */
	private String appIdHeaderName;
	/**
	 * HTTP 请求头中时间戳字段的名称。
	 *
	 * @since 1.0.0
	 */
	private String timestampHeaderName;
	/**
	 * HTTP 请求参数中的签名字段名称。
	 *
	 * @since 1.0.0
	 */
	private String signatureParamName;
	/**
	 * HTTP 请求参数中的应用 ID 字段名称。
	 *
	 * @since 1.0.0
	 */
	private String appIdParamName;

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
