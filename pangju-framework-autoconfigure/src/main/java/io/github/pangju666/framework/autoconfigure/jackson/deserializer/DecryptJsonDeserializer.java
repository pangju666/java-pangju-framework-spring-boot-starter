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
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import io.github.pangju666.framework.autoconfigure.jackson.annotation.DecryptFormat;
import io.github.pangju666.framework.autoconfigure.spring.StaticSpringContext;
import io.github.pangju666.framework.autoconfigure.web.utils.CryptoUtils;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.exception.base.ServiceException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DecryptJsonDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {
	private static final Map<String, DecryptJsonDeserializer> DESERIALIZER_MAP = new ConcurrentHashMap<>(10);

	private final DecryptFormat annotation;

	public DecryptJsonDeserializer() {
		this.annotation = null;
	}

	public DecryptJsonDeserializer(DecryptFormat annotation) {
		this.annotation = annotation;
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		String key = null;
		if (annotation.algorithm().needKey()) {
			if (StringUtils.isBlank(annotation.key())) {
				throw new ServerException("无效的密钥属性值");
			}
			key = StaticSpringContext.getProperty(annotation.key());
			if (StringUtils.isBlank(key)) {
				throw new ServerException("未找到密钥，属性：" + key);
			}
		}

		try {
			if (p.getCurrentToken() == JsonToken.START_ARRAY) {
				Collection<?> collection = p.readValueAs(Collection.class);
				List<Object> list = new ArrayList<>(collection.size());
				for (Object o : collection) {
					if (o instanceof String string) {
						list.add(StringUtils.isBlank(string) ? string : CryptoUtils.decryptToString(string,
							key, annotation.algorithm(), annotation.encoding()));
					} else {
						list.add(o);
					}
				}
				return list;
			} else if (p.getCurrentToken() == JsonToken.START_OBJECT) {
				Map<Object, Object> map = p.readValueAs(Map.class);
				Map<Object, Object> decryptMap = new HashMap<>(map.size());
				for (Map.Entry<?, ?> entry : map.entrySet()) {
					if (entry.getValue() instanceof String string) {
						decryptMap.put(entry.getKey(), StringUtils.isBlank(string) ? string :
							CryptoUtils.decryptToString(string, key, annotation.algorithm(), annotation.encoding()));
					} else {
						decryptMap.put(entry.getKey(), entry.getValue());
					}
				}
				return decryptMap;
			} else {
				String value = p.getText();
				return StringUtils.isBlank(value) ? value : CryptoUtils.decryptToString(value, key,
					annotation.algorithm(), annotation.encoding());
			}
		} catch (EncryptionOperationNotPossibleException e) {
			throw new ServerException("数据加密失败", e);
		} catch (InvalidKeySpecException e) {
			throw new ServerException("无效的密钥", e);
		} catch (DecoderException e) {
			throw new ServiceException("十六进制解码失败", e);
		} catch (JsonParseException e) {
			throw new ServerException("数据解析失败", e);
		}
	}

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
		if (Objects.isNull(property)) {
			return NullifyingDeserializer.instance;
		}

		Class<?> clz = Objects.nonNull(ctxt.getContextualType()) ? ctxt.getContextualType().getRawClass() :
			property.getMember().getType().getRawClass();
		if (String.class.isAssignableFrom(clz) || Collection.class.isAssignableFrom(clz) ||
			Map.class.isAssignableFrom(clz)) {
			DecryptFormat annotation = property.getAnnotation(DecryptFormat.class);
			if (Objects.nonNull(annotation)) {
				String key = annotation.key() + "-" + annotation.algorithm().name() + "-" + annotation.encoding().name();
				JsonDeserializer<?> deserializer = DESERIALIZER_MAP.putIfAbsent(key, new DecryptJsonDeserializer(annotation));
				if (Objects.isNull(deserializer)) {
					return DESERIALIZER_MAP.get(key);
				}
				return deserializer;
			}
		}
		return ctxt.findContextualValueDeserializer(property.getType(), property);
	}
}
