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

package io.github.pangju666.framework.boot.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import io.github.pangju666.framework.boot.enums.Algorithm;
import io.github.pangju666.framework.boot.enums.Encoding;
import io.github.pangju666.framework.boot.jackson.annotation.DecryptFormat;
import io.github.pangju666.framework.boot.spring.StaticSpringContext;
import io.github.pangju666.framework.boot.utils.CryptoUtils;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.exception.base.ServiceException;
import org.apache.commons.codec.DecoderException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON字段解密反序列化器
 * <p>
 * 该反序列化器用于处理使用{@link DecryptFormat}注解标记的JSON字段，将加密的内容解密后再反序列化。
 * 支持处理字符串、集合和Map类型的数据，对不同类型的数据采用不同的解密策略。
 * 实现了{@link ContextualDeserializer}接口，可根据上下文自动确定处理方式。
 * </p>
 *
 * @author pangju666
 * @see DecryptFormat
 * @see ContextualDeserializer
 * @see CryptoUtils
 * @since 1.0.0
 */
public class DecryptJsonDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {
	protected static final Logger LOGGER = LoggerFactory.getLogger(DecryptJsonDeserializer.class);

	private static final Map<String, DecryptJsonDeserializer> ALGORITHM_DESERIALIZER_MAP = new ConcurrentHashMap<>();
	private static final Map<String, DecryptJsonDeserializer> CUSTOM_DESERIALIZER_MAP = new ConcurrentHashMap<>();

	private final String key;
	private final Encoding encoding;
	private final CryptoFactory cryptoFactory;
	private final Class<?> targetType;
	private final Class<?> elementType;

	/**
	 * 默认构造方法，创建一个没有指定解密注解的反序列化器
	 * <p>
	 * 该构造方法主要用于Jackson初始化，实际使用时会通过{@link #createContextual}方法创建具体配置的反序列化器
	 * </p>
	 *
	 * @since 1.0.0
	 */
	public DecryptJsonDeserializer() {
		this.cryptoFactory = null;
		this.encoding = null;
		this.key = null;
		this.targetType = null;
		this.elementType = null;
	}

	/**
	 * 构造方法，创建一个指定解密注解的反序列化器
	 *
	 * @param annotation 解密格式注解
	 * @since 1.0.0
	 */
	public DecryptJsonDeserializer(String key, CryptoFactory factory, Encoding encoding, Class<?> targetType, Class<?> elementType) {
		this.key = key;
		this.cryptoFactory = factory;
		this.encoding = encoding;
		this.targetType = targetType;
		this.elementType = elementType;
	}

	/**
	 * 将加密的JSON内容解密并反序列化
	 * <p>
	 * 根据JSON内容的类型，采用不同的解密策略：
	 * <ul>
	 *     <li>对于数组类型，解析为Collection后对每个字符串元素进行解密</li>
	 *     <li>对于对象类型，解析为Map后对每个字符串值进行解密</li>
	 *     <li>对于字符串类型，直接解密字符串值</li>
	 *     <li>对于其他类型，直接返回当前值</li>
	 * </ul>
	 * </p>
	 *
	 * @param p    用于读取JSON内容的解析器
	 * @param ctxt 反序列化上下文
	 * @return 解密后的对象
	 * @throws IOException      如果读取JSON内容时发生I/O错误
	 * @throws ServerException  如果解密过程中发生错误
	 * @throws ServiceException 如果十六进制解码失败
	 */
	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		try {
			if (p.currentToken() == JsonToken.VALUE_NULL) {
				return null;
			} else if (byte[].class.equals(targetType)) {
				return readBytes(p.getBinaryValue());
			} else if (String.class.equals(targetType)) {
				return readString(p.getText());
			} else if (List.class.equals(targetType)) {
				return readList(p.readValueAs(List.class));
			} else if (Set.class.equals(targetType)) {
				return readSet(p.readValueAs(Set.class));
			} else if (Collection.class.equals(targetType)) {
				return readList(p.readValueAs(Collection.class));
			} else if (Map.class.equals(targetType)) {
				return readMap(p.readValueAs(Map.class));
			} else if (BigDecimal.class.equals(targetType)) {
				if (p.currentToken() == JsonToken.VALUE_STRING) {
					return readBigDecimal(new BigDecimal(p.getText()));
				}
				return readBigDecimal(p.getDecimalValue());
			} else if (BigInteger.class.equals(targetType)) {
				if (p.currentToken() == JsonToken.VALUE_STRING) {
					return readBigInteger(new BigInteger(p.getText()));
				}
				return readBigInteger(p.getBigIntegerValue());
			} else {
				return p.currentValue();
			}
		} catch (EncryptionOperationNotPossibleException e) {
			LOGGER.error("数据解密失败", e);
			return null;
		} catch (InvalidKeySpecException e) {
			LOGGER.error("无效的密钥", e);
			return null;
		} catch (DecoderException e) {
			LOGGER.error("十六进制解码失败", e);
			return null;
		}
	}

	/**
	 * 创建上下文相关的反序列化器
	 * <p>
	 * 检查当前处理的属性是否标记了{@link DecryptFormat}注解，以及属性类型是否为支持的类型（字符串、集合或Map）。
	 * 如果符合条件，则创建或复用适当的反序列化器实例；否则使用上下文中的默认反序列化器。
	 * </p>
	 *
	 * @param ctxt     反序列化上下文
	 * @param property 当前处理的Bean属性
	 * @return 上下文相关的反序列化器实例
	 * @throws JsonMappingException 如果创建反序列化器时发生错误
	 */
	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
		if (Objects.isNull(property)) {
			return NullifyingDeserializer.instance;
		}

		DecryptFormat annotation = property.getAnnotation(DecryptFormat.class);
		if (Objects.isNull(annotation)) {
			return ctxt.findContextualValueDeserializer(property.getType(), property);
		}
		JavaType javaType = property.getType();
		Class<?> targetType = javaType.getRawClass();
		if (List.class.equals(targetType) || Set.class.equals(targetType) || Collection.class.equals(targetType)) {
			CollectionType collectionType = (CollectionType) javaType;
			Class<?> elementType = collectionType.getContentType().getRawClass();
			if (isSupportType(elementType)) {
				return getDeserializer(annotation, targetType, elementType, ctxt);
			}
			return ctxt.findContextualValueDeserializer(property.getType(), property);
		} else if (Map.class.equals(targetType)) {
			MapType mapType = (MapType) javaType;
			Class<?> keyType = mapType.getKeyType().getRawClass();
			Class<?> valueType = mapType.getContentType().getRawClass();
			if (String.class.equals(keyType) && isSupportType(valueType)) {
				return getDeserializer(annotation, targetType, valueType, ctxt);
			}
			return ctxt.findContextualValueDeserializer(property.getType(), property);
		} else if (isSupportType(property.getType().getRawClass())) {
			return getDeserializer(annotation, targetType, null, ctxt);
		} else {
			return ctxt.findContextualValueDeserializer(property.getType(), property);
		}
	}

	protected byte[] readBytes(byte[] value) throws InvalidKeySpecException, DecoderException {
		return CryptoUtils.decrypt(cryptoFactory, value, key);
	}

	protected String readString(String value) throws InvalidKeySpecException, DecoderException {
		return CryptoUtils.decryptString(cryptoFactory, value, key, encoding);
	}

	protected BigInteger readBigInteger(BigInteger value) throws InvalidKeySpecException {
		return CryptoUtils.decryptBigInteger(cryptoFactory, value, key);
	}

	protected BigDecimal readBigDecimal(BigDecimal value) throws InvalidKeySpecException {
		return CryptoUtils.decryptBigDecimal(cryptoFactory, value, key);
	}

	protected Object readValue(Object value) throws DecoderException, InvalidKeySpecException {
		if (Objects.isNull(value)) {
			return null;
		} else if (byte[].class.equals(elementType)) {
			return readBytes((byte[]) value);
		} else if (String.class.equals(elementType)) {
			return readString((String) value);
		} else if (BigDecimal.class.equals(elementType)) {
			if (value instanceof String string) {
				return readBigDecimal(new BigDecimal(string));
			}
			return readBigDecimal((BigDecimal) value);
		} else if (BigInteger.class.equals(elementType)) {
			if (value instanceof String string) {
				return readBigInteger(new BigInteger(string));
			}
			return readBigInteger((BigInteger) value);
		} else {
			return value;
		}
	}

	protected List readList(Collection values) throws InvalidKeySpecException, DecoderException {
		List result = new ArrayList<>(values.size());
		for (Object value : values) {
			result.add(readValue(value));
		}
		return result;
	}

	protected Set readSet(Collection values) throws InvalidKeySpecException, DecoderException {
		Set result = new HashSet(values.size());
		for (Object value : values) {
			result.add(readValue(value));
		}
		return result;
	}

	protected Map readMap(Map value) throws InvalidKeySpecException, DecoderException {
		Map result = new HashMap<>(value.size());
		for (Object o : value.entrySet()) {
			Map.Entry entry = (Map.Entry) o;
			if (Objects.isNull(entry.getKey())) {
				continue;
			}
			result.put(entry.getKey(), readValue(entry.getValue()));
		}
		return result;
	}

	protected DecryptJsonDeserializer getDeserializer(DecryptFormat annotation, Class<?> targetType, Class<?> elementType,
													  DeserializationContext ctxt) throws JsonMappingException {
		String key = annotation.key() + "-" + annotation.encoding().name() + "-" + targetType.getName();
		if (Objects.nonNull(elementType)) {
			key += elementType.getName();
		}
		DecryptJsonDeserializer deserializer;
		if (annotation.algorithm() == Algorithm.CUSTOM) {
			key += "-" + annotation.factory().getName();
			deserializer = CUSTOM_DESERIALIZER_MAP.get(key);
			if (Objects.isNull(deserializer)) {
				String cryptoKey = CryptoUtils.getKey(annotation.key());
				if (Objects.isNull(cryptoKey)) {
					throw JsonMappingException.from(ctxt, "解密Jackson反序列化器初始化失败");
				}
				CryptoFactory factory = StaticSpringContext.getBeanFactory().getBean(annotation.factory());
				deserializer = new DecryptJsonDeserializer(cryptoKey, factory, annotation.encoding(), targetType, elementType);
				CUSTOM_DESERIALIZER_MAP.put(key, deserializer);
			}
		} else {
			key += "-" + annotation.algorithm().name();
			deserializer = ALGORITHM_DESERIALIZER_MAP.get(key);
			if (Objects.isNull(deserializer)) {
				String cryptoKey = CryptoUtils.getKey(annotation.key());
				if (Objects.isNull(cryptoKey)) {
					throw JsonMappingException.from(ctxt, "解密Jackson反序列化器初始化失败");
				}
				CryptoFactory factory = StaticSpringContext.getBeanFactory().getBean(annotation.algorithm().getFactoryClass());
				deserializer = new DecryptJsonDeserializer(cryptoKey, factory, annotation.encoding(), targetType, elementType);
				ALGORITHM_DESERIALIZER_MAP.put(key, deserializer);
			}
		}
		return deserializer;
	}

	protected boolean isSupportType(Class<?> valueType) {
		return String.class.equals(valueType) || byte[].class.equals(valueType) ||
			BigInteger.class.equals(valueType) || BigDecimal.class.equals(valueType);
	}
}
