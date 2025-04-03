package io.github.pangju666.framework.autoconfigure.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pangju666.framework.autoconfigure.http.jackson.deserializer.*;
import io.github.pangju666.framework.autoconfigure.http.jackson.serializer.DateJsonSerializer;
import io.github.pangju666.framework.autoconfigure.http.jackson.serializer.LocalDateJsonSerializer;
import io.github.pangju666.framework.autoconfigure.http.jackson.serializer.LocalDateTimeJsonSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@AutoConfiguration(before = org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class)
@ConditionalOnClass(ObjectMapper.class)
public class JacksonAutoConfiguration {
	@ConditionalOnBean(Jackson2ObjectMapperBuilder.class)
	@ConditionalOnMissingBean(ObjectMapper.class)
	@Bean
	public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
		return builder
			.serializerByType(Date.class, new DateJsonSerializer())
			.serializerByType(LocalDate.class, new LocalDateJsonSerializer())
			.serializerByType(LocalDateTime.class, new LocalDateTimeJsonSerializer())
			.deserializerByType(Date.class, new DateJsonDeserializer())
			.deserializerByType(LocalDate.class, new LocalDateJsonDeserializer())
			.deserializerByType(LocalDateTime.class, new LocalDateTimeJsonDeserializer())
			.deserializerByType(Class.class, new ClassJsonDeserializer())
			.deserializerByType(Enum.class, new EnumJsonDeserializer())
			.deserializerByType(BigDecimal.class, new BigDecimalJsonDeserializer())
			.deserializerByType(BigInteger.class, new BigIntegerJsonDeserializer())
			.createXmlMapper(false)
			.build();
	}
}
