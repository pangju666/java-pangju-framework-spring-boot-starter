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

package io.github.pangju666.framework.autoconfigure.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import io.github.pangju666.framework.web.exception.base.ServerException;
import org.apache.commons.lang3.EnumUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class EnumJsonDeserializer extends JsonDeserializer<Enum> implements ContextualDeserializer {
	private static final Map<Class<? extends Enum>, EnumJsonDeserializer> DESERIALIZER_MAP = new ConcurrentHashMap<>(10);

	private final Class<? extends Enum> enumClass;

	public EnumJsonDeserializer() {
		this.enumClass = null;
	}

	public EnumJsonDeserializer(Class<? extends Enum> enumClass) {
		this.enumClass = enumClass;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		try {
			return EnumUtils.getEnumIgnoreCase(this.enumClass, p.getText());
		} catch (JsonParseException e) {
			throw new ServerException("数据解析失败", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public JsonDeserializer<Enum> createContextual(DeserializationContext ctxt, BeanProperty property) {
		Class<? extends Enum> clz = Objects.nonNull(ctxt.getContextualType()) ?
			(Class<? extends Enum>) ctxt.getContextualType().getRawClass() :
			(Class<? extends Enum>) property.getMember().getType().getRawClass();
		JsonDeserializer<Enum> deserializer = DESERIALIZER_MAP.putIfAbsent(clz, new EnumJsonDeserializer(clz));
		if (Objects.isNull(deserializer)) {
			return DESERIALIZER_MAP.get(clz);
		}
		return deserializer;
	}
}