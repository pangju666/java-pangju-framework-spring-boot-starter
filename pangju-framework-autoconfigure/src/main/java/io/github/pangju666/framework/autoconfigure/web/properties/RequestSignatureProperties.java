package io.github.pangju666.framework.autoconfigure.web.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "pangju.web.signature")
public class RequestSignatureProperties {
	private String signatureHeaderName = "Api-Signature";
	private String appIdHeaderName = "Api-App-Id";
	private String timestampHeaderName = "Api-Timestamp";
	private String signatureParamName = "ApiSignature";
	private String appIdParamName = "ApiAppId";
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
