package io.github.pangju666.framework.autoconfigure.web.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pangju.web.request.repeat")
public class RequestRepeatProperties {
	private Type type = Type.EXPIRE_MAP;
	private Redis redis = new Redis();

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Redis getRedis() {
		return redis;
	}

	public void setRedis(Redis redis) {
		this.redis = redis;
	}

	public enum Type {
		REDIS,
		EXPIRE_MAP
	}

	public static class Redis {
		private String templateBeanName;
		private String keyPrefix = "request-repeat";

		public String getTemplateBeanName() {
			return templateBeanName;
		}

		public void setTemplateBeanName(String templateBeanName) {
			this.templateBeanName = templateBeanName;
		}

		public String getKeyPrefix() {
			return keyPrefix;
		}

		public void setKeyPrefix(String keyPrefix) {
			this.keyPrefix = keyPrefix;
		}
	}
}
