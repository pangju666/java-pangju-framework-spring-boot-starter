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
import java.time.Instant;

/**
 * Instant 类型的 JSON 反序列化器。
 * <p>
 * 将 JSON 中的毫秒时间戳转换为 Java 8 的 {@link Instant} 对象。
 * 类型处理：当当前 JSON token 为整型数值（毫秒时间戳）时解析；否则返回 {@code null}。
 * </p>
 *
 * @author pangju666
 * @see Instant
 * @since 1.0.0
 */
public final class InstantJsonDeserializer extends JsonDeserializer<Instant> {
    /**
     * 将 JSON 中的毫秒时间戳反序列化为 {@link Instant}。
     * <p>
     * 行为：当当前 token 为整型数值时，读取其长整型值（毫秒）并转换为 {@link Instant}；
     * 当 token 非整型或值缺失时返回 {@code null}。
     * </p>
     *
     * @param p    JSON 解析器
     * @param ctxt 反序列化上下文
     * @return 解析得到的 {@link Instant} 实例；不匹配时返回 {@code null}
     * @throws IOException 读取 JSON 内容时发生 I/O 错误
     * @since 1.0.0
     */
    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		if (p.currentToken() != JsonToken.VALUE_NUMBER_INT) {
			return null;
		}
		return Instant.ofEpochMilli(p.getLongValue());
    }
}
