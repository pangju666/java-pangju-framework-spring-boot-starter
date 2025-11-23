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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.github.pangju666.commons.lang.utils.DateUtils;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * LocalDateTime类型的JSON反序列化器
 * <p>
 * 该反序列化器用于将JSON中的时间戳（毫秒）转换为Java 8的LocalDateTime对象。
 * 通过{@link DateUtils#toLocalDateTime(Long)}方法进行转换，将毫秒时间戳转为对应的日期时间。
 * </p>
 *
 * @author pangju666
 * @see DateUtils
 * @see LocalDateTime
 * @since 1.0.0
 */
public final class LocalDateTimeJsonDeserializer extends JsonDeserializer<LocalDateTime> {
	/**
	 * 将JSON中的毫秒时间戳反序列化为LocalDateTime对象
	 * <p>
	 * 从JSON解析器中读取长整型数值（毫秒时间戳），然后转换为LocalDateTime对象。
	 * 空值/类型处理：当JSON token不是整型数值或值为空/缺失时，返回null，不抛出异常。
	 * </p>
	 *
	 * @param p    用于读取JSON内容的解析器
     * @param ctxt 反序列化上下文
     * @return 对应的LocalDateTime对象
     * @throws IOException     如果读取JSON内容时发生I/O错误
     * @since 1.0.0
     */
    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		if (p.currentToken() != JsonToken.VALUE_NUMBER_INT) {
			return null;
		}
		return DateUtils.toLocalDateTime(p.getLongValue());
    }
}