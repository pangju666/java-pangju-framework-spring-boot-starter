package io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.properties;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = DynamicMongoProperties.PREFIX)
public class DynamicMongoProperties {
	public static final String PREFIX = "spring.data.mongodb.dynamic";

	private String primary;
	private Map<String, MongoProperties> databases;

	public Map<String, MongoProperties> getDatabases() {
		return databases;
	}

	public void setDatabases(Map<String, MongoProperties> databases) {
		this.databases = databases;
	}

	public String getPrimary() {
		return primary;
	}

	public void setPrimary(String primary) {
		this.primary = primary;
	}
}
