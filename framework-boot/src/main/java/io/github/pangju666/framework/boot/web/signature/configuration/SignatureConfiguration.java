package io.github.pangju666.framework.boot.web.signature.configuration;

public class SignatureConfiguration {
	/**
	 * HTTP 请求头中签名字段的名称。
	 * <p>默认值为 {@code Api-Signature}。</p>
	 *
	 * @since 1.0.0
	 */
	private String signatureHeaderName = "Api-Signature";
	/**
	 * HTTP 请求头中应用 ID 字段的名称。
	 * <p>默认值为 {@code Api-App-Id}。</p>
	 *
	 * @since 1.0.0
	 */
	private String appIdHeaderName = "Api-App-Id";
	/**
	 * HTTP 请求头中时间戳字段的名称。
	 * <p>默认值为 {@code Api-Timestamp}。</p>
	 *
	 * @since 1.0.0
	 */
	private String timestampHeaderName = "Api-Timestamp";
	/**
	 * HTTP 请求参数中的签名字段名称。
	 * <p>默认值为 {@code ApiSignature}。</p>
	 *
	 * @since 1.0.0
	 */
	private String signatureParamName = "ApiSignature";
	/**
	 * HTTP 请求参数中的应用 ID 字段名称。
	 * <p>默认值为 {@code ApiAppId}。</p>
	 *
	 * @since 1.0.0
	 */
	private String appIdParamName = "ApiAppId";

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
