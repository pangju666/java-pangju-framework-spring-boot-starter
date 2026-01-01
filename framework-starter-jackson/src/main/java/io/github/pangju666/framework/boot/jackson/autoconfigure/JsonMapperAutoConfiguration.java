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

package io.github.pangju666.framework.boot.jackson.autoconfigure;

import io.github.pangju666.framework.boot.jackson.serializer.DateSerializer;
import io.github.pangju666.framework.boot.jackson.serializer.InstantSerializer;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.time.Instant;
import java.util.Date;

/**
 * Jackson 自动配置。
 * <p>
 * 自定义 Jackson 的类型处理；当前注册 {@link Instant} 的序列化/反序列化：
 * <ul>
 * </ul>
 * 优先级：在 Spring Boot 默认 Jackson 自动配置之前执行，确保自定义处理生效。
 * 条件：当配置项 {@code pangju.jackson.instant-as-timestamp=true} 或缺省时启用。
 * </p>
 *
 * @author pangju666
 * @see Instant
 * @since 1.0.0
 */
@AutoConfiguration(after = JacksonAutoConfiguration.class)
@ConditionalOnClass(JsonMapper.class)
public class JsonMapperAutoConfiguration {
	@ConditionalOnBooleanProperty(prefix = "pangju.jackson", name = "write-bigdecimal-as-plain", matchIfMissing = true)
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@Bean
	public JsonMapperBuilderCustomizer decimalCustomizer() {
		return jsonMapperBuilder -> jsonMapperBuilder
			.enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN);
	}

	@ConditionalOnBooleanProperty(prefix = "pangju.jackson", name = "write-date-as-timestamps", matchIfMissing = true)
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@Bean
	public JsonMapperBuilderCustomizer dateCustomizer(JacksonProperties properties) {
		return builder -> {
			if (properties.getDateFormat() == null && BooleanUtils.isTrue(properties.getDatatype().getDatetime()
				.get(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS))) {
				return;
			}
			SimpleModule module = new SimpleModule();
			module.addSerializer(Date.class, new DateSerializer());
			builder.addModule(module);
		};
	}

	@ConditionalOnBooleanProperty(prefix = "pangju.jackson", name = "write-instant-as-timestamp", matchIfMissing = true)
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@Bean
	public JsonMapperBuilderCustomizer instantCustomizer(JacksonProperties properties) {
		return builder -> {
			if (properties.getDateFormat() == null && BooleanUtils.isTrue(properties.getDatatype().getDatetime()
				.get(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS))) {
				return;
			}
			SimpleModule module = new SimpleModule();
			module.addSerializer(Instant.class, new InstantSerializer());
			builder.addModule(module);
		};
	}
}
