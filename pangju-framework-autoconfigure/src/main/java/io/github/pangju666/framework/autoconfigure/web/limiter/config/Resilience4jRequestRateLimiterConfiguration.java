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

package io.github.pangju666.framework.autoconfigure.web.limiter.config;

import io.github.pangju666.framework.autoconfigure.web.limiter.limiter.RequestRateLimiter;
import io.github.pangju666.framework.autoconfigure.web.limiter.limiter.impl.Resilience4JRequestRateLimiterImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "pangju.web.request.rate-limit", value = "type", havingValue = "RESILIENCE4J", matchIfMissing = true)
public class Resilience4jRequestRateLimiterConfiguration {
	@ConditionalOnMissingBean(RequestRateLimiter.class)
	@Bean
	public Resilience4JRequestRateLimiterImpl resilience4jRateLimiter() {
		return new Resilience4JRequestRateLimiterImpl();
	}
}
