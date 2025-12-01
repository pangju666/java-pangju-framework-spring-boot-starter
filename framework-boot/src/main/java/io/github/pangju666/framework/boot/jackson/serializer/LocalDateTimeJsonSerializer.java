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
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.pangju666.commons.lang.utils.DateUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * LocalDateTime类型的JSON序列化器
 * <p>
 * 该序列化器用于将Java 8的LocalDateTime对象转换为JSON中的时间戳（毫秒）。
 * 通过{@link DateUtils#toDate(LocalDateTime)}方法先将LocalDateTime转换为Date对象，
 * 然后获取其时间戳进行输出。
 * </p>
 *
 * @author pangju666
 * @see DateUtils
 * @see LocalDateTime
 * @since 1.0.0
 */
public final class LocalDateTimeJsonSerializer extends JsonSerializer<LocalDateTime> {
	/**
	 * 将LocalDateTime对象序列化为JSON中的毫秒时间戳
	 * <p>
	 * 将传入的LocalDateTime对象转换为Date，然后获取其时间戳并写入JSON。
	 * 序列化结果为数值类型的毫秒时间戳。
	 * </p>
	 * <p>
	 * 空值处理：当传入的值为null时，写入JSON null，并且不抛出异常。
	 * </p>
	 *
	 * @param value       要序列化的LocalDateTime对象
     * @param gen         用于生成JSON内容的生成器
     * @param serializers 序列化器提供者
     * @throws IOException 如果写入JSON内容时发生I/O错误
     * @since 1.0.0
     */
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (Objects.isNull(value)) {
            gen.writeNull();
            return;
        }
        gen.writeNumber(DateUtils.toDate(value).getTime());
    }
}
