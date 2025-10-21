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
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import io.github.pangju666.framework.web.exception.base.ServerException;
import org.apache.commons.lang3.EnumUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 枚举类型的JSON反序列化器
 * <p>
 * 该反序列化器用于将JSON中的字符串值转换为对应的枚举实例。支持不区分大小写的枚举名匹配，
 * 通过{@link EnumUtils#getEnumIgnoreCase(Class, String)}方法实现。同时实现了
 * {@link ContextualDeserializer}接口，可根据上下文自动确定目标枚举类型。
 * </p>
 * <p>
 * 为了提高性能，该反序列化器使用内部缓存来存储已创建的针对特定枚举类型的反序列化器实例。
 * </p>
 *
 * @author pangju666
 * @see ContextualDeserializer
 * @see EnumUtils
 * @since 1.0.0
 */
public class EnumJsonDeserializer extends JsonDeserializer<Enum> implements ContextualDeserializer {
	/**
	 * 枚举类型反序列化器的缓存，用于存储已创建的反序列化器实例
	 * <p>
	 * 键为枚举类型，值为对应的反序列化器实例
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private static final Map<Class<? extends Enum>, EnumJsonDeserializer> DESERIALIZER_MAP = new ConcurrentHashMap<>(10);

	/**
	 * 当前反序列化器处理的枚举类型
	 *
	 * @since 1.0.0
	 */
	private final Class<? extends Enum> enumClass;

	/**
	 * 默认构造方法，创建一个没有指定枚举类型的反序列化器
	 * <p>
	 * 该构造方法主要用于Jackson初始化，实际使用时会通过{@link #createContextual}方法创建具体类型的反序列化器
	 * </p>
	 *
	 * @since 1.0.0
	 */
	public EnumJsonDeserializer() {
		this.enumClass = null;
	}

	/**
	 * 构造方法，创建一个指定枚举类型的反序列化器
	 *
	 * @param enumClass 要处理的枚举类型
	 * @since 1.0.0
	 */
	public EnumJsonDeserializer(Class<? extends Enum> enumClass) {
		this.enumClass = enumClass;
	}

	/**
	 * 将JSON中的字符串值反序列化为对应的枚举实例
	 * <p>
	 * 使用{@link EnumUtils#getEnumIgnoreCase}方法进行不区分大小写的枚举查找。
	 * 如果解析过程中发生错误，则抛出ServerException异常。
	 * </p>
	 *
	 * @param p    用于读取JSON内容的解析器
	 * @param ctxt 反序列化上下文
	 * @return 对应的枚举实例
	 * @throws IOException 如果读取JSON内容时发生I/O错误
	 * @throws ServerException 如果JSON解析过程中发生错误
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Enum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		try {
			return EnumUtils.getEnumIgnoreCase(this.enumClass, p.getText());
		} catch (JsonParseException e) {
			throw new ServerException("数据解析失败", e);
		}
	}

	/**
	 * 创建上下文相关的反序列化器
	 * <p>
	 * 根据反序列化上下文确定目标枚举类型，并返回对应的反序列化器实例。
	 * 如果缓存中已存在对应类型的反序列化器，则直接返回；否则创建新实例并缓存。
	 * </p>
	 *
	 * @param ctxt     反序列化上下文
	 * @param property 当前处理的Bean属性
	 * @return 上下文相关的反序列化器实例
	 */
	@SuppressWarnings("unchecked")
	@Override
	public JsonDeserializer<Enum> createContextual(DeserializationContext ctxt, BeanProperty property) {
		Class<? extends Enum> clz = Objects.nonNull(ctxt.getContextualType()) ?
			(Class<? extends Enum>) ctxt.getContextualType().getRawClass() :
			(Class<? extends Enum>) property.getMember().getType().getRawClass();
		JsonDeserializer<Enum> deserializer = DESERIALIZER_MAP.putIfAbsent(clz, new EnumJsonDeserializer(clz));
		if (Objects.isNull(deserializer)) {
			return DESERIALIZER_MAP.get(clz);
		}
		return deserializer;
	}
}