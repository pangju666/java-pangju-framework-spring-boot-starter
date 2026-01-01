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

package io.github.pangju666.framework.boot.autoconfigure.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pangju666.framework.boot.jackson.deserializer.DateJsonDeserializer;
import io.github.pangju666.framework.boot.jackson.deserializer.InstantJsonDeserializer;
import io.github.pangju666.framework.boot.jackson.serializer.DateJsonSerializer;
import io.github.pangju666.framework.boot.jackson.serializer.InstantJsonSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.Instant;
import java.util.Date;

/**
 * Jackson 自动配置。
 * <p>
 * 提供 Jackson 的自定义配置，主要用于增强时间类型的序列化与反序列化行为：
 * <ul>
 *   <li>{@link Date} 类型：可通过配置 {@code pangju.jackson.date-as-timestamps} 控制是否作为时间戳处理（默认开启）。</li>
 *   <li>{@link Instant} 类型：可通过配置 {@code pangju.jackson.instant-as-timestamp} 控制是否作为时间戳处理（默认开启）。</li>
 * </ul>
 * 自动配置逻辑：
 * <ul>
 *   <li>在 Spring Boot 默认的 {@link org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration} 之前执行，确保自定义配置优先生效。</li>
 * </ul>
 *
 * @author pangju666
 * @see DateJsonSerializer
 * @see DateJsonDeserializer
 * @see InstantJsonSerializer
 * @see InstantJsonDeserializer
 * @since 1.0.0
 */
@AutoConfiguration(before = org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class)
@ConditionalOnClass({Jackson2ObjectMapperBuilder.class, ObjectMapper.class})
public class JacksonAutoConfiguration {
	/**
	 * 配置 Date 类型的自定义序列化器与反序列化器。
	 * <p>
	 * 通过配置属性 {@code pangju.jackson.date-as-timestamps} 控制（默认为 true）。
	 *
	 * @return 定制器 Lambda
	 */
	@ConditionalOnBooleanProperty(prefix = "pangju.jackson", name = "date-as-timestamps", matchIfMissing = true)
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer dateCustomizer() {
		return builder -> builder
			.deserializerByType(Date.class, new DateJsonDeserializer())
			.serializerByType(Date.class, new DateJsonSerializer());
	}

	/**
	 * 配置 Instant 类型的自定义序列化器与反序列化器。
	 * <p>
	 * 通过配置属性 {@code pangju.jackson.instant-as-timestamp} 控制（默认为 true）。
	 *
	 * @return 定制器 Lambda
	 */
	@ConditionalOnBooleanProperty(prefix = "pangju.jackson", name = "instant-as-timestamp", matchIfMissing = true)
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer instantCustomizer() {
		return builder -> builder
			.deserializerByType(Instant.class, new InstantJsonDeserializer())
			.serializerByType(Instant.class, new InstantJsonSerializer());
	}
}
