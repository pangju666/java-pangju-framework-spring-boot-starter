package io.github.pangju666.framework.autoconfigure.core.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pangju666.framework.autoconfigure.core.properties.JacksonProperties;
import io.github.pangju666.framework.core.jackson.databind.deserializer.EnumJsonDeserializer;
import io.github.pangju666.framework.core.jackson.databind.deserializer.TimestampJsonDeserializer;
import io.github.pangju666.framework.core.jackson.databind.serializer.DateJsonSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.Date;

@AutoConfiguration(before = org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class)
@ConditionalOnClass(ObjectMapper.class)
@EnableConfigurationProperties(JacksonProperties.class)
public class JacksonAutoConfiguration {
	@ConditionalOnMissingBean(ObjectMapper.class)
	@Bean
	public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder, JacksonProperties properties) {
		if (properties.isDateToTimestampDeserializer()) {
			builder.serializerByType(Date.class, new DateJsonSerializer());
		}
		if (properties.isTimestampToDateSerializer()) {
			builder.deserializerByType(Date.class, new TimestampJsonDeserializer());
		}
		if (properties.isStringToEnumDeserializer()) {
			builder.deserializerByType(Enum.class, new EnumJsonDeserializer());
		}
		return builder
			.createXmlMapper(false)
			.build();
	}
}
