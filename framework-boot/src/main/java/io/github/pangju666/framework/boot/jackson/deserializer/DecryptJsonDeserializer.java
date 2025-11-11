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
import io.github.pangju666.framework.boot.crypto.utils.CryptoUtils;
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
 * 数据解密的JSON反序列化器
 * <p>
 * 基于属性上的 {@link io.github.pangju666.framework.boot.jackson.annotation.DecryptFormat} 注解，
 * 按算法、编码和密钥对输入内容进行解密并转换为目标类型。实现了 {@link com.fasterxml.jackson.databind.deser.ContextualDeserializer}
 * 接口，可根据反序列化上下文动态创建或复用反序列化器实例。
 * </p>
 * <p>
 * 为提升性能，内部使用静态缓存（按算法或自定义工厂维度）存储已创建的反序列化器实例；同时在解密失败、密钥非法或解码异常时，
 * 反序列化器会记录日志并返回 null，避免抛出异常影响整体反序列化流程。
 * </p>
 *
 * @author pangju666
 * @see io.github.pangju666.framework.boot.jackson.annotation.DecryptFormat
 * @see io.github.pangju666.framework.boot.crypto.factory.CryptoFactory
 * @see io.github.pangju666.framework.boot.enums.Algorithm
 * @see io.github.pangju666.framework.boot.enums.Encoding
 * @see com.fasterxml.jackson.databind.deser.ContextualDeserializer
 * @see com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer
 * @see CryptoUtils
 * @since 1.0.0
 */
public class DecryptJsonDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {
    /**
     * 日志记录器
     *
     * @since 1.0.0
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(DecryptJsonDeserializer.class);

    /**
     * 按算法与编码缓存的反序列化器映射
     * <p>
     * 键格式：<code>key-encoding-targetType-algorithm</code>（如果存在元素类型则追加其类名），值为对应的
     * {@link DecryptJsonDeserializer} 实例。
     * </p>
     *
     * @since 1.0.0
     */
    private static final Map<String, DecryptJsonDeserializer> ALGORITHM_DESERIALIZER_MAP = new ConcurrentHashMap<>();
    /**
     * 按自定义工厂维度缓存的反序列化器映射
     * <p>
     * 键格式：<code>key-encoding-targetType-factoryClass</code>（如果存在元素类型则追加其类名），值为对应的
     * {@link DecryptJsonDeserializer} 实例。
     * </p>
     *
     * @since 1.0.0
     */
    private static final Map<String, DecryptJsonDeserializer> CUSTOM_DESERIALIZER_MAP = new ConcurrentHashMap<>();

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
     * 目标类型（当前属性的原始类型），用于在反序列化时进行类型分派
     *
     * @since 1.0.0
     */
    private final Class<?> targetType;
    /**
     * 元素类型（集合或映射的元素/值类型），用于在容器类型解密时进行元素级分派
     *
     * @since 1.0.0
     */
    private final Class<?> elementType;

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
        this.targetType = null;
        this.elementType = null;
    }

    /**
     * 指定密钥、工厂、编码及目标/元素类型的构造方法
     *
     * @param key         解密密钥（使用前已通过工具解析为实际密钥值）
     * @param factory     加密工厂实例
     * @param encoding    字符串解密使用的编码方式
     * @param targetType  当前属性的目标类型
     * @param elementType 集合或映射的元素/值类型（非容器类型时为 null）
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
     * 将输入内容按目标类型解密并反序列化
     * <p>
     * 根据 {@link #targetType} 进行类型分派；当当前 token 为 JSON null 时直接返回 null；
     * 对于 {@link java.math.BigDecimal} 和 {@link java.math.BigInteger}，兼容字符串输入与数值输入。
     * 解密失败、密钥非法或十六进制解码失败时，记录错误日志并返回 null。
     * </p>
     *
     * @param p    JSON 输入解析器
     * @param ctxt 反序列化上下文
     * @return 反序列化后的目标类型实例；失败或不匹配时返回 null
     * @throws IOException 读取 JSON 内容时发生 I/O 错误
     * @since 1.0.0
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
     * 创建与属性上下文相关的反序列化器实例
     * <p>
     * 当属性缺失（<code>property == null</code>）时，返回 {@link com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer#instance}，
     * 使反序列化结果为 null；当属性标注了 {@link io.github.pangju666.framework.boot.jackson.annotation.DecryptFormat} 注解时，
     * 根据注解配置与类型信息（包括集合/映射的元素类型）获取或创建对应的解密反序列化器；否则退回 Jackson 默认的值反序列化器。
     * </p>
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

    /**
     * 解密字节数组
     *
     * @param value 字节数组值
     * @return 解密后的字节数组
     * @throws InvalidKeySpecException 当密钥规格无效时抛出
     * @throws DecoderException        当十六进制解码失败时抛出
     * @since 1.0.0
     */
    protected byte[] readBytes(byte[] value) throws InvalidKeySpecException, DecoderException {
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
    protected String readString(String value) throws InvalidKeySpecException, DecoderException {
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
    protected BigInteger readBigInteger(BigInteger value) throws InvalidKeySpecException {
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
    protected BigDecimal readBigDecimal(BigDecimal value) throws InvalidKeySpecException {
        return CryptoUtils.decryptBigDecimal(cryptoFactory, value, key);
    }

    /**
     * 根据元素类型分派解密
     * <p>
     * 当值为 null 时直接返回 null；支持的元素类型包括：<code>byte[]</code>、{@link String}、{@link BigDecimal}、{@link BigInteger}。
     * 对于数值类型，如果输入为字符串则先转换后解密。
     * </p>
     *
     * @param value 元素值
     * @return 解密后的元素值
     * @throws DecoderException        当十六进制解码失败时抛出
     * @throws InvalidKeySpecException 当密钥规格无效时抛出
     * @since 1.0.0
     */
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

    /**
     * 解密集合并返回列表
     *
     * @param values 集合内容
     * @return 解密后的列表
     * @throws InvalidKeySpecException 当密钥规格无效时抛出
     * @throws DecoderException        当十六进制解码失败时抛出
     * @since 1.0.0
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected List readList(Collection values) throws InvalidKeySpecException, DecoderException {
        List result = new ArrayList<>(values.size());
        for (Object value : values) {
            result.add(readValue(value));
        }
        return result;
    }

    /**
     * 解密集合并返回集合（Set）
     *
     * @param values 集合内容
     * @return 解密后的集合
     * @throws InvalidKeySpecException 当密钥规格无效时抛出
     * @throws DecoderException        当十六进制解码失败时抛出
     * @since 1.0.0
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Set readSet(Collection values) throws InvalidKeySpecException, DecoderException {
        Set result = new HashSet(values.size());
        for (Object value : values) {
            result.add(readValue(value));
        }
        return result;
    }

    /**
     * 解密映射并返回新的映射
     * <p>
     * 若键为 null 则跳过该条目；根据值的类型进行分派解密后写入新映射。
     * </p>
     *
     * @param value 原映射内容
     * @return 解密后的映射
     * @throws InvalidKeySpecException 当密钥规格无效时抛出
     * @throws DecoderException        当十六进制解码失败时抛出
     * @since 1.0.0
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
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

    /**
     * 基于注解配置和上下文类型信息，获取或创建解密反序列化器实例
     * <p>
     * 构建缓存键以复用已存在的反序列化器；当密钥解析失败或容器中无法获取加密工厂时，抛出 {@link JsonMappingException}。
     * </p>
     *
     * @param annotation  解密格式注解
     * @param targetType  当前属性的目标类型
     * @param elementType 集合或映射的元素/值类型（非容器类型时为 null）
     * @param ctxt        反序列化上下文
     * @return 对应的解密反序列化器实例
     * @throws JsonMappingException 初始化失败时抛出
     * @since 1.0.0
     */
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

    /**
     * 判断类型是否受支持（用于元素或值类型判定）
     *
     * @param valueType 类型
     * @return 当类型为 {@link String}、<code>byte[]</code>、{@link BigInteger} 或 {@link BigDecimal} 时返回 true；否则返回 false
     * @since 1.0.0
     */
    protected boolean isSupportType(Class<?> valueType) {
        return String.class.equals(valueType) || byte[].class.equals(valueType) ||
            BigInteger.class.equals(valueType) || BigDecimal.class.equals(valueType);
    }
}
