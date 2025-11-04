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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import io.github.pangju666.framework.boot.jackson.annotation.DecryptFormat;
import io.github.pangju666.framework.boot.spring.StaticSpringContext;
import io.github.pangju666.framework.boot.utils.CryptoUtils;
import io.github.pangju666.framework.web.exception.base.ServerException;
import io.github.pangju666.framework.web.exception.base.ServiceException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import java.io.IOException;
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
	/**
	 * 反序列化器缓存，用于存储已创建的反序列化器实例
	 * <p>
	 * 键为注解配置的唯一标识，值为对应的反序列化器实例
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private static final Map<String, DecryptJsonDeserializer> DESERIALIZER_MAP = new ConcurrentHashMap<>(10);

	/**
	 * 当前反序列化器使用的解密注解
	 *
	 * @since 1.0.0
	 */
	private final DecryptFormat annotation;

	/**
	 * 默认构造方法，创建一个没有指定解密注解的反序列化器
	 * <p>
	 * 该构造方法主要用于Jackson初始化，实际使用时会通过{@link #createContextual}方法创建具体配置的反序列化器
	 * </p>
	 *
	 * @since 1.0.0
	 */
	public DecryptJsonDeserializer() {
		this.annotation = null;
	}

	/**
	 * 构造方法，创建一个指定解密注解的反序列化器
	 *
	 * @param annotation 解密格式注解
	 * @since 1.0.0
	 */
	public DecryptJsonDeserializer(DecryptFormat annotation) {
		this.annotation = annotation;
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
	 * 解密过程使用{@link CryptoUtils#decryptToString}方法，根据注解配置的算法和编码方式进行解密。
	 * </p>
	 *
	 * @param p    用于读取JSON内容的解析器
	 * @param ctxt 反序列化上下文
	 * @return 解密后的对象
	 * @throws IOException 如果读取JSON内容时发生I/O错误
	 * @throws ServerException 如果解密过程中发生错误
	 * @throws ServiceException 如果十六进制解码失败
	 */
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
			} else if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
				String value = p.getText();
				return StringUtils.isBlank(value) ? value : CryptoUtils.decryptToString(value, key,
					annotation.algorithm(), annotation.encoding());
			}
			return p.currentValue();
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
