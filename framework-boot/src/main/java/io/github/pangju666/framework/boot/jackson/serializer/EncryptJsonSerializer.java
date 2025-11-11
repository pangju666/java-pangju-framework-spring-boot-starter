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
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import io.github.pangju666.framework.boot.enums.Algorithm;
import io.github.pangju666.framework.boot.enums.Encoding;
import io.github.pangju666.framework.boot.jackson.annotation.EncryptFormat;
import io.github.pangju666.framework.boot.spring.StaticSpringContext;
import io.github.pangju666.framework.boot.utils.CryptoUtils;
import io.github.pangju666.framework.web.exception.base.ServerException;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON字段加密序列化器
 * <p>
 * 该序列化器用于处理使用{@link EncryptFormat}注解标记的JSON字段，将内容加密后再序列化。
 * 支持处理字节数组、字符串、集合和Map类型的数据，对不同类型的数据采用不同的加密策略。
 * 实现了{@link ContextualSerializer}接口，可根据上下文自动确定处理方式。
 * </p>
 *
 * @author pangju666
 * @see EncryptFormat
 * @see ContextualSerializer
 * @see CryptoUtils
 * @since 1.0.0
 */
public class EncryptJsonSerializer extends JsonSerializer<Object> implements ContextualSerializer {
	protected static final Logger LOGGER = LoggerFactory.getLogger(EncryptJsonSerializer.class);

	private static final Map<String, EncryptJsonSerializer> ALGORITHM_SERIALIZER_MAP = new ConcurrentHashMap<>();
	private static final Map<String, EncryptJsonSerializer> CUSTOM_SERIALIZER_MAP = new ConcurrentHashMap<>();

	private final String key;
	private final Encoding encoding;
	private final CryptoFactory cryptoFactory;

	/**
	 * 默认构造方法，创建一个没有指定加密注解的序列化器
	 * <p>
	 * 该构造方法主要用于Jackson初始化，实际使用时会通过{@link #createContextual}方法创建具体配置的序列化器
	 * </p>
	 *
	 * @since 1.0.0
	 */
	public EncryptJsonSerializer() {
		this.cryptoFactory = null;
		this.encoding = null;
		this.key = null;
	}

	/**
	 * 构造方法，创建一个指定加密注解的序列化器
	 *
	 * @param annotation 加密格式注解
	 * @since 1.0.0
	 */
	public EncryptJsonSerializer(String key, Encoding encoding, CryptoFactory cryptoFactory) {
		this.key = key;
		this.cryptoFactory = cryptoFactory;
		this.encoding = encoding;
	}

	/**
	 * 将对象加密后序列化为JSON内容
	 * <p>
	 * 根据对象的类型，采用不同的加密策略：
	 * <ul>
	 *     <li>对于字节数组，直接加密后输出为二进制</li>
	 *     <li>对于字符串，将其转换为字节数组加密后输出</li>
	 *     <li>对于集合类型，遍历集合中的字符串元素进行加密</li>
	 *     <li>对于Map类型，遍历Map中的字符串值进行加密</li>
	 * </ul>
	 * </p>
	 *
	 * @param value       要序列化的对象
	 * @param gen         用于生成JSON内容的生成器
	 * @param serializers 序列化器提供者
	 * @throws IOException     如果写入JSON内容时发生I/O错误
	 * @throws ServerException 如果加密过程中发生错误
	 */
	@Override
	public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		try {
			writeValue(value, gen);
		} catch (EncryptionOperationNotPossibleException e) {
			LOGGER.error("数据加密失败", e);
			gen.writeNull();
		} catch (InvalidKeySpecException e) {
			LOGGER.error("无效的密钥", e);
			gen.writeNull();
		}
	}

	/**
	 * 创建上下文相关的序列化器
	 * <p>
	 * 检查当前处理的属性是否标记了{@link EncryptFormat}注解，以及属性类型是否为支持的类型（字节数组、字符串、集合或Map）。
	 * 如果符合条件，则创建或复用适当的序列化器实例；否则使用上下文中的默认序列化器。
	 * </p>
	 *
	 * @param prov     序列化器提供者
	 * @param property 当前处理的Bean属性
	 * @return 上下文相关的序列化器实例
	 * @throws JsonMappingException 如果创建序列化器时发生错误
	 */
	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		if (Objects.isNull(property)) {
			return NullSerializer.instance;
		}

		EncryptFormat annotation = property.getAnnotation(EncryptFormat.class);
		if (Objects.nonNull(annotation)) {
			return getSerializer(annotation, prov);
		}

		return prov.findValueSerializer(property.getType(), property);
	}

	protected void writeBytes(byte[] value, JsonGenerator gen) throws InvalidKeySpecException, IOException {
		gen.writeBinary(CryptoUtils.encrypt(cryptoFactory, value, key));
	}

	protected void writeString(CharSequence value, JsonGenerator gen) throws InvalidKeySpecException, IOException {
		if (StringUtils.isBlank(value)) {
			gen.writeString(value.toString());
		} else {
			gen.writeString(CryptoUtils.encryptString(cryptoFactory, value.toString(), key, encoding));
		}
	}

	protected void writeBigInteger(BigInteger value, JsonGenerator gen) throws InvalidKeySpecException, IOException {
		if (Objects.isNull(value)) {
			gen.writeNull();
		} else {
			gen.writeNumber(CryptoUtils.encryptBigInteger(cryptoFactory, value, key));
		}
	}

	protected void writeBigDecimal(BigDecimal value, JsonGenerator gen) throws InvalidKeySpecException, IOException {
		if (Objects.isNull(value)) {
			gen.writeNull();
		} else {
			gen.writeNumber(CryptoUtils.encryptBigDecimal(cryptoFactory, value, key));
		}
	}

	protected void writeValue(Object value, JsonGenerator gen) throws InvalidKeySpecException, IOException {
		if (value instanceof byte[] bytes) {
			writeBytes(bytes, gen);
		} else if (value instanceof CharSequence charSequence) {
			writeString(charSequence, gen);
		} else if (value instanceof Iterable<?> iterable) {
			writeIterable(iterable, gen);
		} else if (value instanceof Map<?, ?> map) {
			writeMap(map, gen);
		} else if (value instanceof BigDecimal bigDecimal) {
			writeBigDecimal(bigDecimal, gen);
		} else if (value instanceof BigInteger bigInteger) {
			writeBigInteger(bigInteger, gen);
		} else {
			gen.writePOJO(value);
		}
	}

	protected void writeIterable(Iterable<?> values, JsonGenerator gen) throws InvalidKeySpecException, IOException {
		gen.writeStartArray();
		for (Object value : values) {
			writeValue(value, gen);
		}
		gen.writeEndArray();
	}

	protected void writeMap(Map<?, ?> value, JsonGenerator gen) throws InvalidKeySpecException, IOException {
		gen.writeStartObject();
		for (var entry : value.entrySet()) {
			if (Objects.isNull(entry.getKey())) {
				continue;
			}
			if (entry.getValue() instanceof byte[] bytes) {
				gen.writeFieldName(entry.getKey().toString());
				writeBytes(bytes, gen);
			} else if (entry.getValue() instanceof CharSequence charSequence) {
				gen.writeFieldName(entry.getKey().toString());
				writeString(charSequence, gen);
			} else if (entry.getValue() instanceof Iterable<?> iterable) {
				gen.writeFieldName(entry.getKey().toString());
				writeIterable(iterable, gen);
			} else if (entry.getValue() instanceof Map<?, ?> map) {
				gen.writeFieldName(entry.getKey().toString());
				writeMap(map, gen);
			} else if (value instanceof BigDecimal bigDecimal) {
				gen.writeFieldName(entry.getKey().toString());
				writeBigDecimal(bigDecimal, gen);
			} else if (value instanceof BigInteger bigInteger) {
				gen.writeFieldName(entry.getKey().toString());
				writeBigInteger(bigInteger, gen);
			} else {
				gen.writePOJOField(entry.getKey().toString(), entry.getValue());
			}
		}
		gen.writeEndObject();
	}

	protected EncryptJsonSerializer getSerializer(EncryptFormat annotation, SerializerProvider prov) throws JsonMappingException {
		String key = annotation.key() + "-" + annotation.encoding().name();
		EncryptJsonSerializer serializer;
		if (annotation.algorithm() == Algorithm.CUSTOM) {
			key += "-" + annotation.factory().getName();
			serializer = CUSTOM_SERIALIZER_MAP.get(key);
			if (Objects.isNull(serializer)) {
				String cryptoKey = CryptoUtils.getKey(annotation.key());
				if (Objects.isNull(cryptoKey)) {
					throw JsonMappingException.from(prov, "加密Jackson序列化器初始化失败");
				}
				CryptoFactory factory = StaticSpringContext.getBeanFactory().getBean(annotation.factory());
				serializer = new EncryptJsonSerializer(cryptoKey, annotation.encoding(), factory);
				CUSTOM_SERIALIZER_MAP.put(key, serializer);
			}
		} else {
			key += "-" + annotation.algorithm().name();
			serializer = ALGORITHM_SERIALIZER_MAP.get(key);
			if (Objects.isNull(serializer)) {
				String cryptoKey = CryptoUtils.getKey(annotation.key());
				if (Objects.isNull(cryptoKey)) {
					throw JsonMappingException.from(prov, "加密Jackson序列化器初始化失败");
				}
				CryptoFactory factory = StaticSpringContext.getBeanFactory().getBean(annotation.algorithm().getFactoryClass());
				serializer = new EncryptJsonSerializer(cryptoKey, annotation.encoding(), factory);
				ALGORITHM_SERIALIZER_MAP.put(key, serializer);
			}
		}
		return serializer;
	}
}
