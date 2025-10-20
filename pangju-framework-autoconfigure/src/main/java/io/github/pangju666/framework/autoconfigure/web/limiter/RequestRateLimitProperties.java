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

package io.github.pangju666.framework.autoconfigure.web.limiter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "pangju.web.rate-limit")
public class RequestRateLimitProperties {
	private Type type = Type.RESILIENCE4J;
	private Redisson redisson = new Redisson();

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Redisson getRedisson() {
		return redisson;
	}

	public void setRedisson(Redisson redisson) {
		this.redisson = redisson;
	}

	public enum Type {
		REDISSON,
		RESILIENCE4J
	}

	public static class Redisson {
		private String beanName;
		private String keyPrefix = "request-limit";
		private Duration expire = Duration.ofSeconds(5);

		public String getBeanName() {
			return beanName;
		}

		public void setBeanName(String beanName) {
			this.beanName = beanName;
		}

		public String getKeyPrefix() {
			return keyPrefix;
		}

		public void setKeyPrefix(String keyPrefix) {
			this.keyPrefix = keyPrefix;
		}

		public Duration getExpire() {
			return expire;
		}

		public void setExpire(Duration expire) {
			this.expire = expire;
		}
	}
}
