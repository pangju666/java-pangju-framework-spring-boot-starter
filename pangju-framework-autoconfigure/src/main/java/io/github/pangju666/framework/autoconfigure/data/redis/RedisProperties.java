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

@ConfigurationProperties("spring.data.redis.serializer")
public class RedisProperties {
	private RedisSerializerType key = RedisSerializerType.STRING;
	private RedisSerializerType value = RedisSerializerType.JSON;
	private RedisSerializerType hashKey = RedisSerializerType.STRING;
	private RedisSerializerType hashValue = RedisSerializerType.JSON;

	public RedisSerializerType getKey() {
		return key;
	}

	public void setKey(RedisSerializerType key) {
		this.key = key;
	}

	public RedisSerializerType getValue() {
		return value;
	}

	public void setValue(RedisSerializerType value) {
		this.value = value;
	}

	public RedisSerializerType getHashKey() {
		return hashKey;
	}

	public void setHashKey(RedisSerializerType hashKey) {
		this.hashKey = hashKey;
	}

	public RedisSerializerType getHashValue() {
		return hashValue;
	}

	public void setHashValue(RedisSerializerType hashValue) {
		this.hashValue = hashValue;
	}
}
