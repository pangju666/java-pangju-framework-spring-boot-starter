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

package io.github.pangju666.framework.autoconfigure.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pangju666.framework.autoconfigure.jackson.deserializer.ClassJsonDeserializer;
import io.github.pangju666.framework.autoconfigure.jackson.deserializer.EnumJsonDeserializer;
import io.github.pangju666.framework.autoconfigure.jackson.deserializer.LocalDateJsonDeserializer;
import io.github.pangju666.framework.autoconfigure.jackson.deserializer.LocalDateTimeJsonDeserializer;
import io.github.pangju666.framework.autoconfigure.jackson.serializer.LocalDateJsonSerializer;
import io.github.pangju666.framework.autoconfigure.jackson.serializer.LocalDateTimeJsonSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AutoConfiguration(before = org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class)
@ConditionalOnClass({Jackson2ObjectMapperBuilder.class, ObjectMapper.class})
@EnableConfigurationProperties(JacksonProperties.class)
public class JacksonAutoConfiguration {
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
		return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder
			.deserializerByType(Class.class, new ClassJsonDeserializer())
			.deserializerByType(Enum.class, new EnumJsonDeserializer());
	}

	@ConditionalOnProperty(prefix = "pangju.framework.jackson", name = "local-date-support", havingValue = "true", matchIfMissing = true)
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer localDateJackson2ObjectMapperBuilderCustomizer() {
		return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder
			.serializerByType(LocalDate.class, new LocalDateJsonSerializer())
			.deserializerByType(LocalDate.class, new LocalDateJsonDeserializer());
	}

	@ConditionalOnProperty(prefix = "pangju.framework.jackson", name = "local-date-time-support", havingValue = "true", matchIfMissing = true)
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer localDateTimeJackson2ObjectMapperBuilderCustomizer() {
		return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder
			.serializerByType(LocalDateTime.class, new LocalDateTimeJsonSerializer())
			.deserializerByType(LocalDateTime.class, new LocalDateTimeJsonDeserializer());
	}
}
