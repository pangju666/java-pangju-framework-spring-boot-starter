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

package io.github.pangju666.framework.autoconfigure.web.signature;

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
