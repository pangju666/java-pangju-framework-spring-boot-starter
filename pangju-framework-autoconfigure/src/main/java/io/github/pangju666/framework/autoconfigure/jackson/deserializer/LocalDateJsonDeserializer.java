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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.github.pangju666.commons.lang.utils.DateUtils;
import io.github.pangju666.framework.web.exception.base.ServerException;

import java.io.IOException;
import java.time.LocalDate;

/**
 * LocalDate类型的JSON反序列化器
 * <p>
 * 该反序列化器用于将JSON中的时间戳（毫秒）转换为Java 8的LocalDate对象。
 * 通过{@link DateUtils#toLocalDate(Long)}方法进行转换，将毫秒时间戳转为对应的日期。
 * </p>
 *
 * @author pangju666
 * @see DateUtils
 * @see LocalDate
 * @since 1.0.0
 */
public class LocalDateJsonDeserializer extends JsonDeserializer<LocalDate> {
	/**
	 * 将JSON中的毫秒时间戳反序列化为LocalDate对象
	 * <p>
	 * 从JSON解析器中读取长整型数值（毫秒时间戳），然后转换为LocalDate对象。
	 * 如果解析过程中发生错误，则抛出ServerException异常。
	 * </p>
	 *
	 * @param p    用于读取JSON内容的解析器
	 * @param ctxt 反序列化上下文
	 * @return 对应的LocalDate对象
	 * @throws IOException     如果读取JSON内容时发生I/O错误
	 * @throws ServerException 如果JSON解析过程中发生错误
	 */
	@Override
	public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		try {
			return DateUtils.toLocalDate(p.getLongValue());
		} catch (JsonParseException e) {
			throw new ServerException("数据解析失败", e);
		}
	}
}