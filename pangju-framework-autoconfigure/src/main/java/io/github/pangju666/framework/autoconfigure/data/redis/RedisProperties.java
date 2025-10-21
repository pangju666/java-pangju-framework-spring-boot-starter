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

package io.github.pangju666.framework.autoconfigure.data.redis;

import io.github.pangju666.framework.data.redis.enums.RedisSerializerType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis序列化器配置属性类
 * <p>
 * 用于配置Redis中value和hashValue的序列化方式。
 * 配置前缀为{@code spring.data.redis.serializer}。
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * spring:
 *   data:
 *     redis:
 *       serializer:
 *         value: JSON
 *         hash-value: JSON
 * </pre>
 * </p>
 *
 * @author pangju666
 * @see RedisSerializerType
 * @see RedisAutoConfiguration
 * @since 1.0.0
 */
@ConfigurationProperties("spring.data.redis.serializer")
public class RedisProperties {
	/**
	 * Redis值的序列化类型
	 * <p>
	 * 用于配置Redis中普通key的value序列化方式。
	 * 默认值为{@link RedisSerializerType#JSON}
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private RedisSerializerType value = RedisSerializerType.JSON;
	/**
	 * Redis哈希表中值的序列化类型
	 * <p>
	 * 用于配置Redis中哈希表结构中的value序列化方式。
	 * 默认值为{@link RedisSerializerType#JSON}
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private RedisSerializerType hashValue = RedisSerializerType.JSON;

	public RedisSerializerType getValue() {
		return value;
	}

	public void setValue(RedisSerializerType value) {
		this.value = value;
	}

	public RedisSerializerType getHashValue() {
		return hashValue;
	}

	public void setHashValue(RedisSerializerType hashValue) {
		this.hashValue = hashValue;
	}
}
