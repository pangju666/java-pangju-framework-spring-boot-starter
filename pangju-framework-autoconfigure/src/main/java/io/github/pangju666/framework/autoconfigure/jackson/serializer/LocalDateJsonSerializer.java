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
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.pangju666.commons.lang.utils.DateUtils;

import java.io.IOException;
import java.time.LocalDate;

/**
 * LocalDate类型的JSON序列化器
 * <p>
 * 该序列化器用于将Java 8的LocalDate对象转换为JSON中的时间戳（毫秒）。
 * 通过{@link DateUtils#toDate(LocalDate)}方法先将LocalDate转换为Date对象，
 * 然后获取其时间戳进行输出。
 * </p>
 *
 * @author pangju666
 * @see DateUtils
 * @see LocalDate
 * @since 1.0.0
 */
public class LocalDateJsonSerializer extends JsonSerializer<LocalDate> {
	/**
	 * 将LocalDate对象序列化为JSON中的毫秒时间戳
	 * <p>
	 * 将传入的LocalDate对象转换为Date，然后获取其时间戳并写入JSON。
	 * 序列化结果为数值类型的毫秒时间戳。
	 * </p>
	 *
	 * @param value       要序列化的LocalDate对象
	 * @param gen         用于生成JSON内容的生成器
	 * @param serializers 序列化器提供者
	 * @throws IOException 如果写入JSON内容时发生I/O错误
	 */
	@Override
	public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeNumber(DateUtils.toDate(value).getTime());
	}
}
