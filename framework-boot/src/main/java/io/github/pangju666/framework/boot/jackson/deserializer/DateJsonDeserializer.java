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

import java.io.IOException;
import java.util.Date;

/**
 * Date 类型反序列化器。
 * <p>
 * 将时间戳（数值类型）反序列化为 {@link Date} 对象。
 *
 * @author pangju666
 * @since 1.0.0
 */
public final class DateJsonDeserializer extends JsonDeserializer<Date> {
	/**
	 * 反序列化 Date。
	 * <p>
	 * 仅当 JSON Token 为数值类型（VALUE_NUMBER_INT）时执行反序列化，否则返回 null。
	 *
	 * @param p    JSON 解析器
	 * @param ctxt 反序列化上下文
	 * @return Date 对象或 null
	 * @throws IOException IO 异常
	 */
	@Override
	public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		if (p.currentToken() != JsonToken.VALUE_NUMBER_INT) {
			return null;
		}
		return new Date(p.getLongValue());
	}
}
