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

package io.github.pangju666.framework.autoconfigure.jackson.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import io.github.pangju666.commons.lang.utils.DesensitizationUtils;
import io.github.pangju666.framework.autoconfigure.jackson.annotation.DesensitizeFormat;
import io.github.pangju666.framework.autoconfigure.jackson.enums.DesensitizedType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DesensitizedJsonSerializer extends JsonSerializer<String> implements ContextualSerializer {
	private static final Map<String, DesensitizedJsonSerializer> TYPE_SERIALIZER_MAP;
	private static final Map<String, DesensitizedJsonSerializer> REGEX_SERIALIZER_MAP = new ConcurrentHashMap<>(10);
	private static final Map<String, DesensitizedJsonSerializer> LENGTH_SERIALIZER_MAP = new ConcurrentHashMap<>(10);

	static {
		DesensitizedType[] desensitizedType = DesensitizedType.values();
		TYPE_SERIALIZER_MAP = new HashMap<>(desensitizedType.length);
		for (DesensitizedType type : desensitizedType) {
			TYPE_SERIALIZER_MAP.put(type.name(), new DesensitizedJsonSerializer(type.getConverter()));
		}
	}

	private final Converter<String, String> converter;

	public DesensitizedJsonSerializer() {
		this.converter = null;
	}

	public DesensitizedJsonSerializer(Converter<String, String> converter) {
		this.converter = converter;
	}

	public static JsonSerializer<String> getSerializer(String regex, String format) {
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

	public static JsonSerializer<String> getSerializer(int left, int right) {
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

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		if (Objects.isNull(property)) {
			return NullSerializer.instance;
		}
		if (Objects.equals(property.getType().getRawClass(), String.class)) {
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