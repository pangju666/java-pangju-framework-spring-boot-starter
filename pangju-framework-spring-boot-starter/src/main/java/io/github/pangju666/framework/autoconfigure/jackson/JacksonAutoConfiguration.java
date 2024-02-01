package io.github.pangju666.framework.autoconfigure.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pangju666.framework.core.jackson.databind.deserializer.TimestampJsonDeserializer;
import io.github.pangju666.framework.core.jackson.databind.serializer.DateJsonSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.Date;

@AutoConfiguration(before = org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class)
@ConditionalOnClass(ObjectMapper.class)
public class JacksonAutoConfiguration {
	@ConditionalOnMissingBean(ObjectMapper.class)
	@Bean
	public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
		return builder
			.serializerByType(Date.class, new DateJsonSerializer())
			.deserializerByType(Date.class, new TimestampJsonDeserializer())
			.createXmlMapper(false)
			.build();
	}
}
