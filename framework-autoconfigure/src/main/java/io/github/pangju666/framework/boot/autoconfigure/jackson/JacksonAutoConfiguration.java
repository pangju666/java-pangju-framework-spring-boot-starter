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
import io.github.pangju666.framework.boot.jackson.deserializer.InstantJsonDeserializer;
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

/**
 * Jackson 自动配置。
 * <p>
 * 自定义 Jackson 的类型处理；当前注册 {@link Instant} 的序列化/反序列化：
 * <ul>
 *   <li>序列化：{@link InstantJsonSerializer} 将 {@link Instant} 写为毫秒时间戳</li>
 *   <li>反序列化：{@link InstantJsonDeserializer} 将毫秒时间戳读为 {@link Instant}</li>
 * </ul>
 * 优先级：在 Spring Boot 默认 Jackson 自动配置之前执行，确保自定义处理生效。
 * 条件：当配置项 {@code pangju.jackson.instant-as-timestamp=true} 或缺省时启用。
 * </p>
 *
 * @author pangju666
 * @see Instant
 * @see InstantJsonSerializer
 * @see InstantJsonDeserializer
 * @since 1.0.0
 */
@AutoConfiguration(before = org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class)
@ConditionalOnBooleanProperty(prefix = "pangju.jackson", name = "instant-as-timestamp", matchIfMissing = true)
@ConditionalOnClass({Jackson2ObjectMapperBuilder.class, ObjectMapper.class})
public class JacksonAutoConfiguration {
    /**
     * 创建并注册 Jackson 构建定制器。
     * <p>
     * 将 {@link Instant} 的序列化/反序列化器注册到 {@link Jackson2ObjectMapperBuilder}。
     * </p>
     *
     * @return Jackson2ObjectMapperBuilder 的自定义器实例
     * @since 1.0.0
     */
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
		return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder
			.serializerByType(Instant.class, new InstantJsonSerializer())
			.deserializerByType(Instant.class, new InstantJsonDeserializer());
	}
}
