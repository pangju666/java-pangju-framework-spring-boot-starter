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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.github.pangju666.framework.web.exception.base.ServerException;

import java.io.IOException;

/**
 * Class类型的JSON反序列化器
 * <p>
 * 该反序列化器用于将JSON中的类名字符串转换为对应的Class对象。
 * 支持从字符串形式的全限定类名转换为实际的Class对象。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class ClassJsonDeserializer extends JsonDeserializer<Class> {
	/**
	 * 将JSON中的类名字符串反序列化为Class对象
	 * <p>
	 * 通过{@link Class#forName(String)}方法尝试加载类。如果类不存在，
	 * 则返回null；如果解析过程中发生错误，则抛出ServerException异常。
	 * </p>
	 *
	 * @param p    用于读取JSON内容的解析器
	 * @param ctxt 反序列化上下文
	 * @return 对应的Class对象，如果类不存在则返回null
	 * @throws IOException     如果读取JSON内容时发生I/O错误
	 * @throws ServerException 如果JSON解析过程中发生错误
	 */
	@Override
	public Class deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		try {
			return Class.forName(p.getText());
		} catch (ClassNotFoundException e) {
			return null;
		} catch (JsonParseException e) {
			throw new ServerException("数据解析失败", e);
		}
	}
}
