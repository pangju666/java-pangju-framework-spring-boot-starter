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

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.time.Instant;
import java.util.Objects;

/**
 * Instant 类型的 JSON 序列化器。
 * <p>
 * 将 Java 8 的 {@link Instant} 转换为 JSON 中的毫秒时间戳（数值）。
 * 行为：当值非空时写出其纪元毫秒；为空时写出 JSON null。
 * </p>
 *
 * @author pangju666
 * @see Instant
 * @since 1.0.0
 */
public final class InstantSerializer extends ValueSerializer<Instant> {
    /**
     * 将 {@link Instant} 序列化为 JSON 中的毫秒时间戳。
     * <p>
     * 行为：当值非空时写出 {@link Instant#toEpochMilli()} 的数值；当值为空时写出 JSON null。
     * </p>
     *
     * @param value       要序列化的 {@link Instant}
     * @param generator         JSON 生成器
     * @param context 序列化器提供者
     * @since 1.0.0
     */
    @Override
    public void serialize(Instant value, JsonGenerator generator, SerializationContext context) {
        if (Objects.isNull(value)) {
			generator.writeNull();
            return;
        }
		generator.writeNumber(value.toEpochMilli());
    }
}
