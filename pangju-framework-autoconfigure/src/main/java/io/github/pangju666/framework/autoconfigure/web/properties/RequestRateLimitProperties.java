package io.github.pangju666.framework.autoconfigure.web.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "pangju.web.request.rate-limit")
public class RequestRateLimitProperties {
	private Type type = Type.RESILIENCE4J;
	private Redisson redisson = new Redisson();

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Redisson getRedisson() {
		return redisson;
	}

	public void setRedisson(Redisson redisson) {
		this.redisson = redisson;
	}

	public enum Type {
		REDISSON,
		RESILIENCE4J
	}

	public static class Redisson {
		private String beanName;
		private String keyPrefix = "request-limit";
		private Duration expire = Duration.ofSeconds(5);

		public String getBeanName() {
			return beanName;
		}

		public void setBeanName(String beanName) {
			this.beanName = beanName;
		}

		public String getKeyPrefix() {
			return keyPrefix;
		}

		public void setKeyPrefix(String keyPrefix) {
			this.keyPrefix = keyPrefix;
		}

		public Duration getExpire() {
			return expire;
		}

		public void setExpire(Duration expire) {
			this.expire = expire;
		}
	}
}
