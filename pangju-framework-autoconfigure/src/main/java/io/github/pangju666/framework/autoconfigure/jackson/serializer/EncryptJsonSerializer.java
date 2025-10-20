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

package io.github.pangju666.framework.autoconfigure.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import io.github.pangju666.framework.autoconfigure.jackson.annotation.EncryptFormat;
import io.github.pangju666.framework.autoconfigure.spring.StaticSpringContext;
import io.github.pangju666.framework.autoconfigure.web.utils.CryptoUtils;
import io.github.pangju666.framework.web.exception.base.ServerException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class EncryptJsonSerializer extends JsonSerializer<Object> implements ContextualSerializer {
	private static final Map<String, EncryptJsonSerializer> SERIALIZER_MAP = new ConcurrentHashMap<>(10);

	private final EncryptFormat annotation;

	public EncryptJsonSerializer() {
		this.annotation = null;
	}

	public EncryptJsonSerializer(EncryptFormat annotation) {
		this.annotation = annotation;
	}

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		String key;
		if (annotation.algorithm().needKey()) {
			if (StringUtils.isBlank(annotation.key())) {
				throw new ServerException("无效的密钥属性值");
			}
			key = StaticSpringContext.getProperty(annotation.key());
			if (StringUtils.isBlank(key)) {
				throw new ServerException("未找到密钥，属性：" + key);
			}
		} else {
			key = null;
		}

		try {
			if (value instanceof byte[] bytes) {
				if (ArrayUtils.isEmpty(bytes)) {
					gen.writeBinary(bytes);
				} else {
					byte[] result = CryptoUtils.encrypt(bytes, key, annotation.algorithm(), annotation.encoding());
					gen.writeBinary(result);
				}
			} else if (value instanceof String string) {
				if (StringUtils.isBlank(string)) {
					gen.writeString(string);
				} else {
					String result = CryptoUtils.encryptToString(string.getBytes(), key, annotation.algorithm(), annotation.encoding());
					gen.writeString(result);
				}
			} else if (value instanceof Collection<?> collection) {
				gen.writeStartArray();
				for (Object o : collection) {
					if (!String.class.isAssignableFrom(o.getClass())) {
						gen.writePOJO(o);
					} else {
						String string = (String) o;
						if (StringUtils.isBlank(string)) {
							gen.writeString(string);
						} else {
							gen.writeString(CryptoUtils.encryptToString(string.getBytes(), key,
								annotation.algorithm(), annotation.encoding()));
						}
					}
				}
				gen.writeEndArray();
			} else if (value instanceof Map<?, ?> map) {
				gen.writeStartObject();
				for (var entry : map.entrySet()) {
					if (!String.class.isAssignableFrom(entry.getValue().getClass())) {
						gen.writeObjectField(entry.getKey().toString(), entry.getValue());
					} else {
						gen.writeStringField(entry.getKey().toString(), CryptoUtils.encryptToString(
							((String) entry.getValue()).getBytes(), key, annotation.algorithm(), annotation.encoding()));
					}
				}
				gen.writeEndObject();
			}
		} catch (EncryptionOperationNotPossibleException e) {
			throw new ServerException("数据加密失败", e);
		} catch (InvalidKeySpecException e) {
			throw new ServerException("无效的密钥", e);
		}
	}

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		Class<?> clz = property.getType().getRawClass();
		if (String.class.isAssignableFrom(clz) || byte[].class.equals(clz) ||
			Collection.class.isAssignableFrom(clz) || Map.class.isAssignableFrom(clz)) {
			EncryptFormat annotation = property.getAnnotation(EncryptFormat.class);
			if (Objects.nonNull(annotation)) {
				String key = annotation.key() + "-" + annotation.algorithm().name() + "-" + annotation.encoding().name();
				JsonSerializer<?> serializer = SERIALIZER_MAP.putIfAbsent(key, new EncryptJsonSerializer(annotation));
				if (Objects.isNull(serializer)) {
					return SERIALIZER_MAP.get(key);
				}
				return serializer;
			}
		}
		return prov.findValueSerializer(property.getType(), property);
	}
}
