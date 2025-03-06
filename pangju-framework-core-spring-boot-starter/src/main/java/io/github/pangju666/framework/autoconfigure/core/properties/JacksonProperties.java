package io.github.pangju666.framework.autoconfigure.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pangju.jackson")
public class JacksonProperties {
	private boolean timestampToDateSerializer = true;
	private boolean dateToTimestampDeserializer = true;
	private boolean stringToEnumDeserializer = true;

	public boolean isTimestampToDateSerializer() {
		return timestampToDateSerializer;
	}

	public void setTimestampToDateSerializer(boolean timestampToDateSerializer) {
		this.timestampToDateSerializer = timestampToDateSerializer;
	}

	public boolean isDateToTimestampDeserializer() {
		return dateToTimestampDeserializer;
	}

	public void setDateToTimestampDeserializer(boolean dateToTimestampDeserializer) {
		this.dateToTimestampDeserializer = dateToTimestampDeserializer;
	}

	public boolean isStringToEnumDeserializer() {
		return stringToEnumDeserializer;
	}

	public void setStringToEnumDeserializer(boolean stringToEnumDeserializer) {
		this.stringToEnumDeserializer = stringToEnumDeserializer;
	}
}
