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

package io.github.pangju666.framework.autoconfigure.web.idempotent;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pangju.web.idempotent")
public class IdempotentProperties {
	private Type type = Type.EXPIRE_MAP;
	private Redis redis = new Redis();

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Redis getRedis() {
		return redis;
	}

	public void setRedis(Redis redis) {
		this.redis = redis;
	}

	public enum Type {
		REDIS,
		EXPIRE_MAP
	}

	public static class Redis {
		private String redisTemplateBeanName;
		private String keyPrefix = "idempotent";

		public String getRedisTemplateBeanName() {
			return redisTemplateBeanName;
		}

		public void setRedisTemplateBeanName(String redisTemplateBeanName) {
			this.redisTemplateBeanName = redisTemplateBeanName;
		}

		public String getKeyPrefix() {
			return keyPrefix;
		}

		public void setKeyPrefix(String keyPrefix) {
			this.keyPrefix = keyPrefix;
		}
	}
}
