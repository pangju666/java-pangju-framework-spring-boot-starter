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
import io.github.pangju666.framework.autoconfigure.enums.Algorithm;
import io.github.pangju666.framework.autoconfigure.enums.Encoding;
import io.github.pangju666.framework.autoconfigure.jackson.annotation.EncryptFormat;
import io.github.pangju666.framework.autoconfigure.spring.StaticSpringContext;
import io.github.pangju666.framework.autoconfigure.web.crypto.utils.CryptoUtils;
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
	/**
	 * 序列化器缓存，用于存储已创建的序列化器实例
	 * <p>
	 * 键为注解配置的唯一标识，值为对应的序列化器实例
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private static final Map<String, EncryptJsonSerializer> SERIALIZER_MAP = new ConcurrentHashMap<>(10);

	/**
	 * 当前序列化器使用的加密注解
	 *
	 * @since 1.0.0
	 */
	private final EncryptFormat annotation;

	/**
	 * 默认构造方法，创建一个没有指定加密注解的序列化器
	 * <p>
	 * 该构造方法主要用于Jackson初始化，实际使用时会通过{@link #createContextual}方法创建具体配置的序列化器
	 * </p>
	 *
	 * @since 1.0.0
	 */
	public EncryptJsonSerializer() {
		this.annotation = null;
	}

	/**
	 * 构造方法，创建一个指定加密注解的序列化器
	 *
	 * @param annotation 加密格式注解
	 * @since 1.0.0
	 */
	public EncryptJsonSerializer(EncryptFormat annotation) {
		this.annotation = annotation;
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
	 * 加密过程使用{@link CryptoUtils#encryptToString}或{@link CryptoUtils#encrypt(byte[], String, Algorithm, Encoding)}方法，根据注解配置的算法和编码方式进行加密。
	 * </p>
	 *
	 * @param value       要序列化的对象
	 * @param gen         用于生成JSON内容的生成器
	 * @param serializers 序列化器提供者
	 * @throws IOException 如果写入JSON内容时发生I/O错误
	 * @throws ServerException 如果加密过程中发生错误
	 */
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
