package io.github.pangju666.framework.autoconfigure.jackson;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pangju.framework.jackson")
public class JacksonProperties {
	private boolean localDateSupport = true;
	private boolean localDateTimeSupport = true;

	public boolean isLocalDateSupport() {
		return localDateSupport;
	}

	public void setLocalDateSupport(boolean localDateSupport) {
		this.localDateSupport = localDateSupport;
	}

	public boolean isLocalDateTimeSupport() {
		return localDateTimeSupport;
	}

	public void setLocalDateTimeSupport(boolean localDateTimeSupport) {
		this.localDateTimeSupport = localDateTimeSupport;
	}
}
