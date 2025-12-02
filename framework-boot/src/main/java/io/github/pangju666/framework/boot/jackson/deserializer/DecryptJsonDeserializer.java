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
import io.github.pangju666.framework.boot.crypto.enums.Encoding;
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import io.github.pangju666.framework.boot.crypto.utils.CryptoUtils;
import io.github.pangju666.framework.boot.jackson.annotation.DecryptFormat;
import io.github.pangju666.framework.boot.jackson.utils.CryptoFactoryRegistry;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

/**
 * 数据解密的 JSON 反序列化器。
 * <p>
 * 基于属性上的 {@link io.github.pangju666.framework.boot.jackson.annotation.DecryptFormat} 注解，
 * 按密钥、编码与工厂对输入内容进行解密并转换为目标类型。实现 {@link com.fasterxml.jackson.databind.deser.ContextualDeserializer}
 * 接口，可根据反序列化上下文动态创建或复用反序列化器实例。</p>
 *
 * <p>缓存：按“密钥摘要-编码-属性类型-工厂类名”维度缓存已创建的反序列化器实例，属性类型为 {@link JavaType#toString()} 的完整类型表示（集合/映射包含元素/值类型），密钥摘要为 SHA-256。</p>
 * <p>工厂优先级：当注解提供工厂类型时，优先使用该工厂；否则使用算法枚举关联的工厂；工厂获取通过 {@link CryptoFactoryRegistry} 进行 Spring Bean 优先与构造回退。</p>
 * <p>失败处理：上下文化阶段密钥解析或工厂获取失败时返回 {@link NullifyingDeserializer}；解密过程中发生错误时记录日志并返回 {@code null}。</p>
 *
 * @author pangju666
 * @see io.github.pangju666.framework.boot.jackson.annotation.DecryptFormat
 * @see io.github.pangju666.framework.boot.crypto.factory.CryptoFactory
 * @see io.github.pangju666.framework.boot.crypto.enums.Encoding
 * @see io.github.pangju666.framework.boot.crypto.utils.CryptoUtils
 * @see CryptoFactoryRegistry
 * @since 1.0.0
 */
public final class DecryptJsonDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {
	/**
	 * 日志记录器
	 *
	 * @since 1.0.0
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DecryptJsonDeserializer.class);

	/**
	 * 解密密钥（已解析后的实际密钥值）
	 *
	 * @since 1.0.0
	 */
	private final String key;
	/**
	 * 字符串解密输入使用的编码方式
	 *
	 * @since 1.0.0
	 */
	private final Encoding encoding;
	/**
	 * 加密工厂，用于执行具体的解密实现
	 *
	 * @since 1.0.0
	 */
    private final CryptoFactory cryptoFactory;
    /**
     * 当前属性的 Java 类型（包含集合/映射的元素或值类型）。
     *
     * @since 1.0.0
     */
    private final JavaType propertyJavaType;

	/**
	 * 默认构造方法
	 * <p>
	 * 供 Jackson 初始化使用，不直接携带上下文字段；实际反序列化时会通过 {@link #createContextual} 生成带上下文的实例。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	public DecryptJsonDeserializer() {
		this.cryptoFactory = null;
		this.encoding = null;
		this.key = null;
		this.propertyJavaType = null;
	}

	/**
	 * 指定密钥、工厂、编码及目标/元素类型的构造方法
	 *
	 * @param key         解密密钥（使用前已通过工具解析为实际密钥值）
	 * @param encoding    字符串解密使用的编码方式
	 * @param propertyJavaType  当前属性的目标Java类型
	 * @param factory     加密工厂实例
	 * @since 1.0.0
	 */
	public DecryptJsonDeserializer(String key, Encoding encoding, JavaType propertyJavaType, CryptoFactory factory) {
		this.key = key;
		this.cryptoFactory = factory;
		this.encoding = encoding;
		this.propertyJavaType = propertyJavaType;
	}

    /**
     * 将输入内容按目标类型解密并反序列化。
     * <p>
     * 根据 {@link #propertyJavaType} 进行类型分派；当前 token 为 JSON null 时直接返回 {@code null}；
     * 对于 {@link java.math.BigDecimal} 与 {@link java.math.BigInteger}，兼容字符串输入与数值输入。
     * 解密失败、密钥非法或十六进制解码失败时，统一记录日志并返回 {@code null}，不向外抛出相关异常。
     * </p>
     *
     * @param p    JSON 输入解析器
     * @param ctxt 反序列化上下文
     * @return 反序列化后的目标类型实例；失败或不匹配时返回 {@code null}
     * @throws IOException 读取 JSON 内容时发生 I/O 错误
     * @since 1.0.0
     */
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		if (Objects.isNull(propertyJavaType)) {
			return p.currentValue();
		}
		
		Class<?> targetType = propertyJavaType.getRawClass();
		try {
			if (p.currentToken() == JsonToken.VALUE_NULL) {
				return null;
			} else if (targetType == byte[].class) {
				return readBytes(p.getBinaryValue());
			} else if (targetType == String.class) {
				return readString(p.getText());
			} else if (targetType == BigDecimal.class) {
				if (p.currentToken() == JsonToken.VALUE_STRING) {
					try {
						return readBigDecimal(new BigDecimal(p.getText()));
					} catch (NumberFormatException ignored) {
						return null;
					}
				}
				return readBigDecimal(p.getDecimalValue());
			} else if (targetType == BigInteger.class) {
				if (p.currentToken() == JsonToken.VALUE_STRING) {
					try {
						return readBigInteger(new BigInteger(p.getText()));
					} catch (NumberFormatException ignored) {
						return null;
					}
				}
				return readBigInteger(p.getBigIntegerValue());
			} else if (propertyJavaType instanceof CollectionType collectionType) {
				if (targetType == List.class) {
					return readList(p.readValueAs(List.class), collectionType);
				} else if (targetType == Set.class) {
					return readSet(p.readValueAs(Set.class), collectionType);
				} else if (targetType == Collection.class) {
					return readList(p.readValueAs(Collection.class), collectionType);
				}
			} else if (targetType == Map.class && propertyJavaType instanceof MapType mapType) {
				return readMap(p.readValueAs(Map.class), mapType);
			}
			return p.currentValue();
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
     * 创建与属性上下文相关的反序列化器实例。
     *
     * <p>概述：当属性为空返回当前实例；标注了 {@link io.github.pangju666.framework.boot.jackson.annotation.DecryptFormat}
     * 注解则按注解配置与类型信息（包含集合/映射的元素/值类型的 {@link JavaType} 信息）创建或获取解密反序列化器；否则使用 Jackson 默认的值反序列化器。</p>
     *
     * <p>参数与类型规则：</p>
     * <p>属性为空返回当前实例；未标注注解使用默认值反序列化器；集合/映射元素类型不受支持使用默认值反序列化器；映射键类型非 {@code String}
     * 使用默认值反序列化器。</p>
     *
     * <p>类型处理：支持 {@code List}/{@code Set}/{@code Collection} 的元素类型与 {@code Map<String, V>} 的值类型。</p>
     *
     * @param ctxt     反序列化上下文
     * @param property 当前处理的 Bean 属性
     * @return 上下文相关的反序列化器实例
     * @throws JsonMappingException 当初始化或查找反序列化器失败时抛出
     * @since 1.0.0
     */
	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
		if (Objects.isNull(property)) {
			return this;
		}

		DecryptFormat annotation = property.getAnnotation(DecryptFormat.class);
		if (Objects.isNull(annotation)) {
			return ctxt.findContextualValueDeserializer(property.getType(), property);
		}
		JavaType javaType = property.getType();
		Class<?> targetType = javaType.getRawClass();
		if (targetType == List.class || targetType == Set.class || targetType == Collection.class) {
			CollectionType collectionType = (CollectionType) javaType;
			Class<?> contentType = collectionType.getContentType().getRawClass();
			if (isSupportedContentType(contentType)) {
				return getDeserializer(annotation, javaType);
			}
		} else if (targetType == Map.class) {
			MapType mapType = (MapType) javaType;
			Class<?> keyType = mapType.getKeyType().getRawClass();
			Class<?> contentType = mapType.getContentType().getRawClass();
			if (keyType == String.class && isSupportedContentType(contentType)) {
				return getDeserializer(annotation, javaType);
			}
		} else if (isSupportedBaseType(targetType)) {
			return getDeserializer(annotation, javaType);
		}
		return ctxt.findContextualValueDeserializer(property.getType(), property);
	}

	/**
	 * 解密字节数组
	 *
	 * @param value 字节数组值
	 * @return 解密后的字节数组
	 * @throws InvalidKeySpecException 当密钥规格无效时抛出
	 * @throws DecoderException        当十六进制解码失败时抛出
	 * @since 1.0.0
	 */
	private byte[] readBytes(byte[] value) throws InvalidKeySpecException, DecoderException {
		return CryptoUtils.decrypt(cryptoFactory, value, key);
	}

	/**
	 * 解密字符串
	 *
	 * @param value 字符串值
	 * @return 解密后的字符串
	 * @throws InvalidKeySpecException 当密钥规格无效时抛出
	 * @throws DecoderException        当十六进制解码失败时抛出
	 * @since 1.0.0
	 */
	private String readString(String value) throws InvalidKeySpecException, DecoderException {
		return CryptoUtils.decryptString(cryptoFactory, value, key, encoding);
	}

	/**
	 * 解密 {@link java.math.BigInteger}
	 *
	 * @param value 加密的 BigInteger 值
	 * @return 解密后的 BigInteger 值
	 * @throws InvalidKeySpecException 当密钥规格无效时抛出
	 * @since 1.0.0
	 */
	private BigInteger readBigInteger(BigInteger value) throws InvalidKeySpecException {
		return CryptoUtils.decryptBigInteger(cryptoFactory, value, key);
	}

	/**
	 * 解密 {@link java.math.BigDecimal}
	 *
	 * @param value 加密的 BigDecimal 值
	 * @return 解密后的 BigDecimal 值
	 * @throws InvalidKeySpecException 当密钥规格无效时抛出
	 * @since 1.0.0
	 */
	private BigDecimal readBigDecimal(BigDecimal value) throws InvalidKeySpecException {
		return CryptoUtils.decryptBigDecimal(cryptoFactory, value, key);
	}

    /**
     * 分派解密并返回对应类型的值。
     * <p>
     * 基础类型支持：<code>byte[]</code>、{@link String}、{@link BigDecimal}、{@link BigInteger}。
     * 当数值输入为字符串时，先进行类型转换再解密。
     * </p>
     * <p>
     * 容器类型支持：当 {@code javaType} 为 {@link CollectionType} 或 {@link MapType} 时，
     * 按元素/值类型分别调用 {@link #readList(Collection, CollectionType)}、{@link #readSet(Collection, CollectionType)}
     * 与 {@link #readMap(Map, MapType)} 进行递归解密；映射键类型需为 {@code String}。
     * </p>
     * <p>
     * 失败策略：解密失败、密钥非法或十六进制解码失败时记录日志并返回 {@code null}。
     * </p>
     *
     * @param value    元素或容器的原始值
     * @param javaType 该值对应的 {@link JavaType}，用于判定目标/元素/值类型
     * @return 解密后的值；在不支持或失败时返回原值或 {@code null}
     * @since 1.0.0
     */
    @SuppressWarnings("rawtypes")
    private Object readValue(Object value, JavaType javaType) {
		Class<?> targetType = javaType.getRawClass();
		try {
			if (Objects.isNull(value)) {
				return null;
			} else if (targetType == byte[].class) {
				if (value instanceof String string) {
					return readBytes(Base64.decodeBase64(string));
				}
				return readBytes((byte[]) value);
			} else if (targetType == String.class) {
				return readString((String) value);
			} else if (targetType == BigDecimal.class) {
				if (value instanceof String string) {
					try {
						return readBigDecimal(new BigDecimal(string));
					} catch (NumberFormatException ignored) {
						return null;
					}
				} else if (value instanceof BigInteger bigInteger) {
					return readBigDecimal(new BigDecimal(bigInteger.toString()));
				}
				return readBigDecimal((BigDecimal) value);
			} else if (targetType == BigInteger.class) {
				if (value instanceof String string) {
					try {
						return readBigInteger(new BigInteger(string));
					} catch (NumberFormatException ignored) {
						return null;
					}
				}
				return readBigInteger((BigInteger) value);
			} else if (javaType instanceof CollectionType collectionType) {
				if (targetType == List.class || targetType == Collection.class) {
					return readList((List) value, collectionType);
				} else if (targetType == Set.class) {
					return readSet((Set) value, collectionType);
				}
			} else if (targetType == Map.class && javaType instanceof MapType mapType) {
				Class<?> keyClass = mapType.getKeyType().getRawClass();
				if (keyClass == String.class) {
					return readMap((Map) value, mapType);
				}
			}
			return value;
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
	 * 解密集合并返回列表。
	 *
	 * <p>行为：按 {@link CollectionType#getContentType()} 的元素类型递归调用 {@link #readValue(Object, JavaType)} 进行解密；
	 * 元素为 {@code null} 时保留 {@code null}；失败时记录日志并写入 {@code null}。</p>
	 *
	 * @param values 集合内容
	 * @return 解密后的列表（与输入大小一致）
	 * @since 1.0.0
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private List readList(Collection values, CollectionType javaType) {
		List result = new ArrayList<>(values.size());
		for (Object value : values) {
			result.add(readValue(value, javaType.getContentType()));
		}
		return result;
	}

	/**
	 * 解密集合并返回集合（Set）。
	 *
	 * <p>行为：按 {@link CollectionType#getContentType()} 的元素类型递归调用 {@link #readValue(Object, JavaType)} 进行解密；
	 * 元素为 {@code null} 时保留 {@code null}；失败时记录日志并写入 {@code null}。</p>
	 *
	 * @param values 集合内容
	 * @return 解密后的集合（与输入大小一致）
	 * @since 1.0.0
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private Set readSet(Collection values, CollectionType javaType) {
		Set result = new HashSet(values.size());
		for (Object value : values) {
			result.add(readValue(value, javaType.getContentType()));
		}
		return result;
	}

	/**
	 * 解密映射并返回新的映射。
	 *
	 * <p>行为：不修改键；按 {@link MapType#getContentType()} 的值类型递归调用 {@link #readValue(Object, JavaType)} 进行解密并写入新映射。
	 * 当值为 {@code null} 或解密失败时写入 {@code null}。</p>
	 *
	 * @param value 原映射内容
	 * @return 解密后的映射（键保持原样）
	 * @since 1.0.0
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private Map readMap(Map value, MapType javaType) {
		Map result = new HashMap<>(value.size());
		for (Object o : value.entrySet()) {
			Map.Entry entry = (Map.Entry) o;
			result.put(entry.getKey(), readValue(entry.getValue(), javaType.getContentType()));
		}
		return result;
	}

    /**
     * 基于注解配置和上下文类型信息，获取或创建解密反序列化器实例。
     *
     * <p>缓存键：{@code sha256Hex(key)-encoding-javaType-factoryClassName}；按键复用实例。</p>
     * <p>工厂优先级：当注解提供工厂类型时，优先使用该工厂；否则使用算法枚举关联的工厂。</p>
     * <p>工厂获取：通过 {@link CryptoFactoryRegistry#getOrCreate(Class)} 优先查找 Spring Bean，不存在则构造实例。</p>
     * <p>失败处理：密钥无法解析或工厂 Bean 获取失败时返回 {@link com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer#instance}。</p>
     *
     * @param annotation  解密格式注解
     * @param targetType  当前属性的 {@link JavaType}
     * @return 对应的解密反序列化器实例
     * @since 1.0.0
     */
	private JsonDeserializer<?> getDeserializer(DecryptFormat annotation, JavaType targetType) {
		String cryptoKey;
		try {
			cryptoKey = CryptoUtils.getKey(annotation.key());
		} catch (IllegalArgumentException e) {
			LOGGER.error("无效的密钥，注解属性值：{}", annotation.key());
			return NullifyingDeserializer.instance;
		}

		Class<? extends CryptoFactory> factoryClass;
		if (ArrayUtils.isNotEmpty(annotation.factory())) {
			factoryClass = annotation.factory()[0];
		} else {
			factoryClass = annotation.algorithm().getFactoryClass();
		}

		try {
			return new DecryptJsonDeserializer(cryptoKey, annotation.encoding(), targetType,
					CryptoFactoryRegistry.getOrCreate(factoryClass));
		} catch (IllegalStateException e) {
			LOGGER.error("无法获取或创建 CryptoFactory, class: {}", factoryClass.getName(), e);
			return NullifyingDeserializer.instance;
		}
	}

	/**
	 * 判断是否为支持的基础类型。
	 *
	 * <p>支持类型：{@link String}、<code>byte[]</code>、{@link BigInteger}、{@link BigDecimal}。</p>
	 *
	 * @param valueType 待判定类型
	 * @return 当类型为支持的基础类型时返回 {@code true}
	 * @since 1.0.0
	 */
	private boolean isSupportedBaseType(Class<?> valueType) {
		return valueType == String.class || valueType == byte[].class || valueType == BigInteger.class ||
			valueType == BigDecimal.class;
	}

	/**
	 * 判断是否为支持的集合/映射元素类型。
	 *
	 * <p>支持类型：（{@link String}、<code>byte[]</code>、{@link BigInteger}、{@link BigDecimal}）、（{@link List}、
	 * {@link Set}、{@link Collection}、{@link Map}）。</p>
	 *
	 * @param valueType 待判定类型
	 * @return 当类型为支持的基础或容器类型时返回 {@code true}
	 * @since 1.0.0
	 */
	private boolean isSupportedContentType(Class<?> valueType) {
		return valueType == String.class || valueType == byte[].class || valueType == BigInteger.class ||
			valueType == BigDecimal.class || valueType == List.class || valueType == Set.class ||
			valueType == Collection.class || valueType == Map.class;
	}
}
