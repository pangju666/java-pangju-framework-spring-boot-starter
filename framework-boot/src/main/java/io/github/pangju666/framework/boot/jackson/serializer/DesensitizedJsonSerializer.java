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

package io.github.pangju666.framework.boot.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import io.github.pangju666.commons.lang.utils.DesensitizationUtils;
import io.github.pangju666.framework.boot.jackson.annotation.DesensitizeFormat;
import io.github.pangju666.framework.boot.jackson.enums.DesensitizedType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON字段脱敏序列化器
 * <p>
 * 该序列化器用于处理使用{@link DesensitizeFormat}注解标记的字符串字段，在序列化过程中对敏感信息进行脱敏处理。
 * 支持多种脱敏策略：内置的脱敏类型、基于正则表达式的脱敏和基于长度的脱敏。
 * 实现了{@link ContextualSerializer}接口，可根据上下文自动确定处理方式。
 * </p>
 *
 * @author pangju666
 * @see DesensitizeFormat
 * @see DesensitizedType
 * @see ContextualSerializer
 * @since 1.0.0
 */
public class DesensitizedJsonSerializer extends JsonSerializer<String> implements ContextualSerializer {
	/**
	 * 基于脱敏类型的序列化器缓存
	 * <p>
	 * 存储所有内置脱敏类型对应的序列化器实例
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private static final Map<String, DesensitizedJsonSerializer> TYPE_SERIALIZER_MAP;
	/**
	 * 基于正则表达式的序列化器缓存
	 * <p>
	 * 键为正则表达式和替换格式的组合标识，值为对应的序列化器实例
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private static final Map<String, DesensitizedJsonSerializer> REGEX_SERIALIZER_MAP = new ConcurrentHashMap<>(10);
	/**
	 * 基于长度的序列化器缓存
	 * <p>
	 * 键为前缀和后缀长度的组合标识，值为对应的序列化器实例
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private static final Map<String, DesensitizedJsonSerializer> LENGTH_SERIALIZER_MAP = new ConcurrentHashMap<>(10);

	// 静态初始化块，预先创建所有内置脱敏类型的序列化器
	static {
		DesensitizedType[] desensitizedType = DesensitizedType.values();
		TYPE_SERIALIZER_MAP = new HashMap<>(desensitizedType.length);
		for (DesensitizedType type : desensitizedType) {
			TYPE_SERIALIZER_MAP.put(type.name(), new DesensitizedJsonSerializer(type.getConverter()));
		}
	}

	/**
	 * 字符串转换器，用于执行具体的脱敏操作
	 *
	 * @since 1.0.0
	 */
	private final Converter<String, String> converter;

	/**
	 * 默认构造方法，创建一个没有指定转换器的序列化器
	 * <p>
	 * 该构造方法主要用于Jackson初始化，实际使用时会通过{@link #createContextual}方法创建具体配置的序列化器
	 * </p>
	 *
	 * @since 1.0.0
	 */
	public DesensitizedJsonSerializer() {
		this.converter = null;
	}

	/**
	 * 构造方法，创建一个指定转换器的序列化器
	 *
	 * @param converter 字符串转换器，用于执行脱敏操作
	 * @since 1.0.0
	 */
	public DesensitizedJsonSerializer(Converter<String, String> converter) {
		this.converter = converter;
	}

	/**
	 * 获取基于正则表达式的脱敏序列化器
	 * <p>
	 * 根据提供的正则表达式和替换格式创建或复用序列化器实例，用于将匹配正则表达式的内容替换为指定格式
	 * </p>
	 *
	 * @param regex  用于匹配需要脱敏内容的正则表达式
	 * @param format 替换格式，用于替换匹配到的内容
	 * @return 对应的序列化器实例
	 * @since 1.0.0
	 */
	private static JsonSerializer<String> getSerializer(String regex, String format) {
		String key = regex + "_" + format;
		if (REGEX_SERIALIZER_MAP.containsKey(key)) {
			return REGEX_SERIALIZER_MAP.get(key);
		}
		DesensitizedJsonSerializer serializer = new DesensitizedJsonSerializer(value -> {
			if (StringUtils.isBlank(value)) {
				return value;
			}
			return value.replaceAll(regex, format);
		});
		REGEX_SERIALIZER_MAP.put(key, serializer);
		return serializer;
	}

	/**
	 * 获取基于长度的脱敏序列化器
	 * <p>
	 * 根据提供的前缀保留长度和后缀保留长度创建或复用序列化器实例
	 * 当前缀或后缀长度为-1时表示不保留该部分
	 * </p>
	 *
	 * @param left  保留的前缀长度，-1表示不保留前缀
	 * @param right 保留的后缀长度，-1表示不保留后缀
	 * @return 对应的序列化器实例
	 * @since 1.0.0
	 */
	private static JsonSerializer<String> getSerializer(int left, int right) {
		String key = left + "&" + right;
		if (LENGTH_SERIALIZER_MAP.containsKey(key)) {
			return LENGTH_SERIALIZER_MAP.get(key);
		}
		DesensitizedJsonSerializer serializer = new DesensitizedJsonSerializer(value -> {
			if (left == -1) {
				if (right == -1) {
					return value;
				}
				return DesensitizationUtils.hideRight(value, right);
			}
			if (right == -1) {
				return DesensitizationUtils.hideLeft(value, left);
			}
			return DesensitizationUtils.hideRound(value, left, right);
		});
		LENGTH_SERIALIZER_MAP.put(key, serializer);
		return serializer;
	}

	/**
	 * 将字符串进行脱敏处理后序列化到JSON
	 * <p>
	 * 如果转换器不为空且输入字符串不为空白，则使用转换器对字符串进行脱敏处理后输出；
	 * 否则直接输出原字符串。
	 * </p>
	 *
	 * @param s                 要序列化的字符串
	 * @param jsonGenerator     用于生成JSON内容的生成器
	 * @param serializerProvider 序列化器提供者
	 * @throws IOException 如果写入JSON内容时发生I/O错误
	 */
	@Override
	public void serialize(String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		if (Objects.nonNull(converter)) {
			if (StringUtils.isBlank(s)) {
				jsonGenerator.writeString(s);
			} else {
				jsonGenerator.writeString(converter.convert(s));
			}
		}
	}

	/**
	 * 创建上下文相关的序列化器
	 * <p>
	 * 检查当前处理的属性是否标记了{@link DesensitizeFormat}注解，以及属性类型是否为字符串类型。
	 * 如果符合条件，则根据注解配置创建或复用适当的序列化器实例：
	 * <ul>
	 *   <li>对于自定义(CUSTOM)类型，根据是否提供正则表达式和格式，使用正则表达式脱敏或长度脱敏</li>
	 *   <li>对于内置类型，使用预先创建的对应类型序列化器</li>
	 * </ul>
	 * 如果不符合条件，则使用上下文中的默认序列化器。
	 * </p>
	 *
	 * @param prov     序列化器提供者
	 * @param property 当前处理的Bean属性
	 * @return 上下文相关的序列化器实例
	 * @throws JsonMappingException 如果创建序列化器时发生错误
	 * @since 1.0.0
	 */
	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		if (Objects.isNull(property)) {
			return NullSerializer.instance;
		}

		if (String.class.isAssignableFrom(property.getType().getRawClass())) {
			DesensitizeFormat desensitizeFormat = property.getAnnotation(DesensitizeFormat.class);
			if (Objects.isNull(desensitizeFormat)) {
				desensitizeFormat = property.getContextAnnotation(DesensitizeFormat.class);
			}
			if (Objects.nonNull(desensitizeFormat)) {
				if (DesensitizedType.CUSTOM == desensitizeFormat.type()) {
					if (StringUtils.isAnyBlank(desensitizeFormat.regex(), desensitizeFormat.format())) {
						return getSerializer(desensitizeFormat.prefix(), desensitizeFormat.suffix());
					} else {
						return getSerializer(desensitizeFormat.regex(), desensitizeFormat.format());
					}
				}
				return TYPE_SERIALIZER_MAP.get(desensitizeFormat.type().name());
			}
		}
		return prov.findValueSerializer(property.getType(), property);
	}
}