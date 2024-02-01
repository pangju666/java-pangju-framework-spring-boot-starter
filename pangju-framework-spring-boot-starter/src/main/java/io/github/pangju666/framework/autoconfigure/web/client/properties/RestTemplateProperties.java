package io.github.pangju666.framework.autoconfigure.web.client.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "chang-tech.web.client")
public class RestTemplateProperties {
	private Duration readTimeout = Duration.ofMinutes(5);
	private Duration connectTimeout = Duration.ofSeconds(60);
	private Boolean bufferRequestBody = null;
	private String rootUri = null;

	public Duration getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(Duration readTimeout) {
		this.readTimeout = readTimeout;
	}

	public Duration getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(Duration connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public Boolean getBufferRequestBody() {
		return bufferRequestBody;
	}

	public void setBufferRequestBody(Boolean bufferRequestBody) {
		this.bufferRequestBody = bufferRequestBody;
	}

	public String getRootUri() {
		return rootUri;
	}

	public void setRootUri(String rootUri) {
		this.rootUri = rootUri;
	}
}
