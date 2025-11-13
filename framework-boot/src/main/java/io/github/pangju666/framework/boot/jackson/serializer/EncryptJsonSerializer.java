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
import io.github.pangju666.framework.boot.enums.CryptoAlgorithm;
import io.github.pangju666.framework.boot.enums.Encoding;
import io.github.pangju666.framework.boot.jackson.annotation.EncryptFormat;
import io.github.pangju666.framework.boot.spring.StaticSpringContext;
import io.github.pangju666.framework.boot.crypto.utils.CryptoUtils;
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
 * 数据加密的JSON序列化器
 * <p>
 * 基于属性上的 {@link io.github.pangju666.framework.boot.jackson.annotation.EncryptFormat} 注解，
 * 按算法、编码和密钥对输出内容进行加密。实现了 {@link com.fasterxml.jackson.databind.ser.ContextualSerializer}
 * 接口，可根据序列化上下文动态创建或复用序列化器实例。
 * </p>
 * <p>
 * 为提升性能，内部使用静态缓存（按算法或自定义工厂维度）存储已创建的序列化器实例；同时在加密失败或密钥非法时，
 * 序列化器会记录日志并输出 JSON null，避免抛出异常影响整体序列化流程。
 * </p>
 *
 * @author pangju666
 * @see io.github.pangju666.framework.boot.jackson.annotation.EncryptFormat
 * @see io.github.pangju666.framework.boot.crypto.factory.CryptoFactory
 * @see CryptoAlgorithm
 * @see io.github.pangju666.framework.boot.enums.Encoding
 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer
 * @see com.fasterxml.jackson.databind.ser.std.NullSerializer
 * @see CryptoUtils
 * @since 1.0.0
 */
public class EncryptJsonSerializer extends JsonSerializer<Object> implements ContextualSerializer {
    /**
     * 日志记录器
     *
     * @since 1.0.0
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptJsonSerializer.class);

    /**
     * 按算法与编码缓存的序列化器映射
     * <p>
     * 键格式：<code>key-encoding-algorithm</code>，值为对应的 {@link EncryptJsonSerializer} 实例。
     * </p>
     *
     * @since 1.0.0
     */
    private static final Map<String, EncryptJsonSerializer> ALGORITHM_SERIALIZER_MAP = new ConcurrentHashMap<>();
    /**
     * 按自定义工厂维度缓存的序列化器映射
     * <p>
     * 键格式：<code>key-encoding-factoryClass</code>，值为对应的 {@link EncryptJsonSerializer} 实例。
     * </p>
     *
     * @since 1.0.0
     */
    private static final Map<String, EncryptJsonSerializer> CUSTOM_SERIALIZER_MAP = new ConcurrentHashMap<>();

    /**
     * 加密密钥（已解析后的实际密钥值）
     *
     * @since 1.0.0
     */
    private final String key;
    /**
     * 字符串加密输出使用的编码方式
     *
     * @since 1.0.0
     */
    private final Encoding encoding;
    /**
     * 加密工厂，用于执行具体的加密实现
     *
     * @since 1.0.0
     */
    private final CryptoFactory cryptoFactory;

    /**
     * 默认构造方法
     * <p>
     * 供 Jackson 初始化使用，不直接携带上下文字段；实际序列化时会通过 {@link #createContextual} 生成带上下文的实例。
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
     * 指定密钥、编码与加密工厂的构造方法
     *
     * @param key           加密密钥（使用前已通过工具解析为实际密钥值）
     * @param encoding      字符串加密输出的编码方式
     * @param cryptoFactory 加密工厂实例
     * @since 1.0.0
     */
    public EncryptJsonSerializer(String key, Encoding encoding, CryptoFactory cryptoFactory) {
        this.key = key;
        this.cryptoFactory = cryptoFactory;
        this.encoding = encoding;
    }

    /**
     * 将输入对象加密并写入 JSON
     * <p>
     * 根据对象的运行时类型进行分派处理并加密输出。若加密过程中发生异常（如密钥非法或操作不可执行），
     * 记录错误日志并写入 JSON null，以保证序列化流程不中断。
     * </p>
     *
     * @param value       待序列化的对象
     * @param gen         JSON 输出生成器
     * @param serializers 序列化器提供者
     * @throws IOException 写入 JSON 内容时发生 I/O 错误
     * @since 1.0.0
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
     * 创建与属性上下文相关的序列化器实例
     * <p>
     * 当属性缺失（<code>property == null</code>）时，返回 {@link com.fasterxml.jackson.databind.ser.std.NullSerializer#instance}，
     * 使序列化结果为 null；当属性标注了 {@link io.github.pangju666.framework.boot.jackson.annotation.EncryptFormat} 注解时，
     * 根据注解配置获取或创建对应的加密序列化器；否则退回 Jackson 默认的值序列化器。
     * </p>
     *
     * @param prov     序列化器提供者
     * @param property 当前处理的 Bean 属性
     * @return 上下文相关的序列化器实例
     * @throws JsonMappingException 当初始化或查找序列化器失败时抛出
     * @since 1.0.0
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

    /**
     * 加密字节数组并以二进制形式写入 JSON
     *
     * @param value 字节数组值
     * @param gen   JSON 输出生成器
     * @throws InvalidKeySpecException 当密钥规格无效时抛出
     * @throws IOException             写入 JSON 内容时发生 I/O 错误
     * @since 1.0.0
     */
    private void writeBytes(byte[] value, JsonGenerator gen) throws InvalidKeySpecException, IOException {
        gen.writeBinary(CryptoUtils.encrypt(cryptoFactory, value, key));
    }

    /**
     * 加密字符串并写入 JSON
     * <p>
     * 当字符串为空白时（空或仅空白字符），直接原样输出；否则按配置加密并按指定编码写入。
     * </p>
     *
     * @param value 字符串值
     * @param gen   JSON 输出生成器
     * @throws InvalidKeySpecException 当密钥规格无效时抛出
     * @throws IOException             写入 JSON 内容时发生 I/O 错误
     * @since 1.0.0
     */
    private void writeString(CharSequence value, JsonGenerator gen) throws InvalidKeySpecException, IOException {
        if (StringUtils.isBlank(value)) {
            gen.writeString(value.toString());
        } else {
            gen.writeString(CryptoUtils.encryptString(cryptoFactory, value.toString(), key, encoding));
        }
    }

    /**
     * 加密 {@link java.math.BigInteger} 并写入 JSON 数值
     * <p>
     * 当值为 null 时输出 JSON null；非空时加密后写入数值。
     * </p>
     *
     * @param value BigInteger 值
     * @param gen   JSON 输出生成器
     * @throws InvalidKeySpecException 当密钥规格无效时抛出
     * @throws IOException             写入 JSON 内容时发生 I/O 错误
     * @since 1.0.0
     */
    private void writeBigInteger(BigInteger value, JsonGenerator gen) throws InvalidKeySpecException, IOException {
        if (Objects.isNull(value)) {
            gen.writeNull();
        } else {
            gen.writeNumber(CryptoUtils.encryptBigInteger(cryptoFactory, value, key));
        }
    }

    /**
     * 加密 {@link java.math.BigDecimal} 并写入 JSON 数值
     * <p>
     * 当值为 null 时输出 JSON null；非空时加密后写入数值。
     * </p>
     *
     * @param value BigDecimal 值
     * @param gen   JSON 输出生成器
     * @throws InvalidKeySpecException 当密钥规格无效时抛出
     * @throws IOException             写入 JSON 内容时发生 I/O 错误
     * @since 1.0.0
     */
    private void writeBigDecimal(BigDecimal value, JsonGenerator gen) throws InvalidKeySpecException, IOException {
        if (Objects.isNull(value)) {
            gen.writeNull();
        } else {
            gen.writeNumber(CryptoUtils.encryptBigDecimal(cryptoFactory, value, key));
        }
    }

    /**
     * 根据运行时类型分派加密写出
     * <p>
     * 支持的类型包括：<code>byte[]</code>、{@link CharSequence}、{@link Iterable}、{@link Map}、
     * {@link BigDecimal}、{@link BigInteger}；不在列表中的类型通过 {@link JsonGenerator#writePOJO(Object)} 原样输出。
     * </p>
     *
     * @param value 待写出的值
     * @param gen   JSON 输出生成器
     * @throws InvalidKeySpecException 当密钥规格无效时抛出
     * @throws IOException             写入 JSON 内容时发生 I/O 错误
     * @since 1.0.0
     */
    private void writeValue(Object value, JsonGenerator gen) throws InvalidKeySpecException, IOException {
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

    /**
     * 加密并写出集合类型
     * <p>
     * 以 JSON 数组的形式输出集合内容，并对其中的每个元素进行递归分派加密。
     * </p>
     *
     * @param values 集合内容
     * @param gen    JSON 输出生成器
     * @throws InvalidKeySpecException 当密钥规格无效时抛出
     * @throws IOException             写入 JSON 内容时发生 I/O 错误
     * @since 1.0.0
     */
    private void writeIterable(Iterable<?> values, JsonGenerator gen) throws InvalidKeySpecException, IOException {
        gen.writeStartArray();
        for (Object value : values) {
            writeValue(value, gen);
        }
        gen.writeEndArray();
    }

    /**
     * 加密并写出映射类型
     * <p>
     * 以 JSON 对象形式输出映射内容。若键为 null 则跳过该条目；根据值的类型进行分派加密后写出。
     * </p>
     *
     * @param value 映射内容
     * @param gen   JSON 输出生成器
     * @throws InvalidKeySpecException 当密钥规格无效时抛出
     * @throws IOException             写入 JSON 内容时发生 I/O 错误
     * @since 1.0.0
     */
    private void writeMap(Map<?, ?> value, JsonGenerator gen) throws InvalidKeySpecException, IOException {
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

    /**
     * 基于注解配置和上下文，获取或创建加密序列化器实例
     * <p>
     * 构建缓存键以复用已存在的序列化器；当密钥解析失败或容器中无法获取加密工厂时，抛出 {@link JsonMappingException}。
     * </p>
     *
     * @param annotation 加密格式注解
     * @param prov       序列化器提供者
     * @return 对应的加密序列化器实例
     * @throws JsonMappingException 初始化失败时抛出
     * @since 1.0.0
     */
    private EncryptJsonSerializer getSerializer(EncryptFormat annotation, SerializerProvider prov) throws JsonMappingException {
        String key = annotation.key() + "-" + annotation.encoding().name();
        EncryptJsonSerializer serializer;
        if (annotation.algorithm() == CryptoAlgorithm.CUSTOM) {
            key += "-" + annotation.factory().getName();
			serializer = CUSTOM_SERIALIZER_MAP.get(key);
			if (Objects.isNull(serializer)) {
				String cryptoKey = CryptoUtils.getKey(annotation.key(), false);
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
				String cryptoKey = CryptoUtils.getKey(annotation.key(), false);
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
