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
import io.github.pangju666.commons.lang.utils.DesensitizationUtils;
import io.github.pangju666.framework.boot.jackson.annotation.DesensitizeFormat;
import io.github.pangju666.framework.boot.jackson.enums.DesensitizedType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.util.Objects;

/**
 * JSON 字段脱敏序列化器。
 * <p>
 * 处理标记了 {@link DesensitizeFormat} 的字符串字段，在序列化过程中按策略进行脱敏。
 * 支持两类策略：
 * <ul>
 *   <li>内置类型：使用 {@link DesensitizedType} 的内置转换器。</li>
 *   <li>CUSTOM 类型：依据注解的 {@code prefix}/{@code suffix} 参数执行前后缀保留与中间隐藏。</li>
 * </ul>
 * 实现 {@link ContextualSerializer} 接口，可根据上下文与注解参数选择具体序列化器。
 * </p>
 *
 * @author pangju666
 * @see DesensitizeFormat
 * @see DesensitizedType
 * @see ContextualSerializer
 * @since 1.0.0
 */
public final class DesensitizedJsonSerializer extends JsonSerializer<CharSequence> implements ContextualSerializer {
	/**
	 * 字符串转换器，用于执行具体的脱敏操作
	 *
	 * @since 1.0.0
	 */
	private final Converter<String, String> converter;

	/**
	 * 默认构造方法，创建一个没有指定转换器的序列化器
	 * <p>
	 * 该构造方法主要用于Jackson初始化，实际使用时会通过{@link #createContextual}方法创建具体配置的序列化器
	 * </p>
	 *
	 * @since 1.0.0
	 */
	public DesensitizedJsonSerializer() {
		this.converter = null;
	}

	/**
	 * 构造方法，创建一个指定转换器的序列化器
	 *
	 * @param converter 字符串转换器，用于执行脱敏操作
	 * @since 1.0.0
	 */
    public DesensitizedJsonSerializer(Converter<String, String> converter) {
        this.converter = converter;
    }

    /**
     * 构造方法，按前后缀保留规则创建序列化器。
     *
     * <p>行为：当 {@code prefix} &le; -1 且 {@code suffix} &le; -1 时，脱敏全部字符；
     * 仅前缀 &le; -1 时保留后缀并隐藏左侧；仅后缀 &le; -1 时保留前缀并隐藏右侧；
     * 两者均 &gt; -1 时按前后缀保留并隐藏中间。</p>
     *
     * @param prefix 前缀保留长度，-1 表示不保留前缀
     * @param suffix 后缀保留长度，-1 表示不保留后缀
     * @since 1.0.0
     */
    public DesensitizedJsonSerializer(int prefix, int suffix) {
        this.converter = value -> {
            if (prefix <= -1) {
                if (suffix <= -1) {
                    return DesensitizationUtils.hidePassword(value);
                }
                return DesensitizationUtils.hideRight(value, suffix);
            }
            if (suffix <= -1) {
                return DesensitizationUtils.hideLeft(value, prefix);
            }
            return DesensitizationUtils.hideRound(value, prefix, suffix);
        };
    }

    /**
     * 将字符串进行脱敏处理后序列化到 JSON。
     * <p>当转换器存在且输入不为空白时输出脱敏结果，否则原样输出。</p>
     *
     * @param value       要序列化的字符串
     * @param gen         JSON 输出生成器
     * @param serializers 序列化器提供者
     * @throws IOException 写入 JSON 内容时发生 I/O 错误
     * @since 1.0.0
     */
	@Override
	public void serialize(CharSequence value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		if (Objects.isNull(value)) {
			gen.writeNull();
			return;
		}
		if (Objects.isNull(converter) || StringUtils.isBlank(value)) {
			gen.writeString(value.toString());
			return;
		}
		gen.writeString(converter.convert(value.toString()));
	}

    /**
     * 创建上下文相关的序列化器。
     *
     * <p>行为：当属性类型为字符串且存在 {@link DesensitizeFormat} 注解时，依据注解与类型选择序列化器：</p>
     * <ul>
     *   <li>非 {@link DesensitizedType#CUSTOM} 类型：复用预创建的内置类型序列化器。</li>
     *   <li>{@link DesensitizedType#CUSTOM} 类型：标准化 {@code prefix}/{@code suffix}（最小为 -1）并按
     *   {@code "prefix&suffix"} 作为键从缓存 {@code CUSTOM_SERIALIZER_MAP} 获取或创建长度保留序列化器。</li>
     * </ul>
     * <p>当属性为空或不满足条件时，返回当前实例或上下文默认序列化器。</p>
     *
     * @param prov     序列化器提供者
     * @param property 当前处理的 Bean 属性
     * @return 上下文相关的序列化器实例
     * @throws JsonMappingException 创建或查找序列化器失败时抛出
     * @since 1.0.0
     */
	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		if (Objects.isNull(property)) {
			return this;
		}

		if (CharSequence.class.isAssignableFrom(property.getType().getRawClass())) {
			DesensitizeFormat annotation = property.getAnnotation(DesensitizeFormat.class);
			if (Objects.nonNull(annotation)) {
				if (annotation.type() != DesensitizedType.CUSTOM) {
					return new DesensitizedJsonSerializer(annotation.type().getConverter());
				}
				int prefix = Math.max(annotation.prefix(), -1);
				int suffix = Math.max(annotation.suffix(), -1);
				return new DesensitizedJsonSerializer(prefix, suffix);
			}
		}
		return prov.findValueSerializer(property.getType(), property);
	}
}