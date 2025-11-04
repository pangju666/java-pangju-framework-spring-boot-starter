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
import io.github.pangju666.framework.boot.jackson.deserializer.ClassJsonDeserializer;
import io.github.pangju666.framework.boot.jackson.deserializer.EnumJsonDeserializer;
import io.github.pangju666.framework.boot.jackson.deserializer.LocalDateJsonDeserializer;
import io.github.pangju666.framework.boot.jackson.deserializer.LocalDateTimeJsonDeserializer;
import io.github.pangju666.framework.boot.jackson.serializer.LocalDateJsonSerializer;
import io.github.pangju666.framework.boot.jackson.serializer.LocalDateTimeJsonSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Jackson自动配置类
 * <p>
 * 该配置类用于自定义Jackson的序列化和反序列化行为，主要处理以下类型：
 * <ul>
 *   <li>Class类型 - 使用{@link ClassJsonDeserializer}进行反序列化</li>
 *   <li>枚举类型 - 使用{@link EnumJsonDeserializer}进行反序列化，支持不区分大小写的匹配</li>
 *   <li>LocalDateTime类型 - 使用{@link LocalDateTimeJsonSerializer}和{@link LocalDateTimeJsonDeserializer}进行处理</li>
 *   <li>LocalDate类型 - 使用{@link LocalDateJsonSerializer}和{@link LocalDateJsonDeserializer}进行处理</li>
 * </ul>
 * 该配置在Spring Boot的Jackson自动配置之前执行，确保自定义的序列化器和反序列化器被正确应用。
 * </p>
 *
 * @author pangju666
 * @see ClassJsonDeserializer
 * @see EnumJsonDeserializer
 * @see LocalDateJsonSerializer
 * @see LocalDateJsonDeserializer
 * @see LocalDateTimeJsonSerializer
 * @see LocalDateTimeJsonDeserializer
 * @since 1.0.0
 */
@AutoConfiguration(before = org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class)
@ConditionalOnClass({Jackson2ObjectMapperBuilder.class, ObjectMapper.class})
public class JacksonAutoConfiguration {
	/**
	 * 创建Jackson2ObjectMapperBuilderCustomizer bean，用于自定义ObjectMapper的构建过程
	 * <p>
	 * 该定制器为特定类型注册自定义的序列化器和反序列化器，包括：
	 * <ul>
	 *   <li>为Class类型注册反序列化器，支持从字符串类名转换为Class对象</li>
	 *   <li>为枚举类型注册反序列化器，支持不区分大小写的枚举值匹配</li>
	 *   <li>为LocalDateTime和LocalDate类型注册序列化器和反序列化器，处理日期时间与时间戳的转换</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 仅在未定义同类型bean的情况下生效。
	 * </p>
	 *
	 * @return Jackson2ObjectMapperBuilder的自定义器实例
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(Jackson2ObjectMapperBuilderCustomizer.class)
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
		return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder
			.deserializerByType(Class.class, new ClassJsonDeserializer())
			.deserializerByType(Enum.class, new EnumJsonDeserializer())
			.serializerByType(LocalDateTime.class, new LocalDateTimeJsonSerializer())
			.deserializerByType(LocalDateTime.class, new LocalDateTimeJsonDeserializer())
			.serializerByType(LocalDate.class, new LocalDateJsonSerializer())
			.deserializerByType(LocalDate.class, new LocalDateJsonDeserializer());
	}
}
