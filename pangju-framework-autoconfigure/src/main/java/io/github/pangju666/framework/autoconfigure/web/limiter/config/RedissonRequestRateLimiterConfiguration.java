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

import io.github.pangju666.framework.autoconfigure.web.limiter.RequestRateLimitProperties;
import io.github.pangju666.framework.autoconfigure.web.limiter.handler.RequestRateLimiter;
import io.github.pangju666.framework.autoconfigure.web.limiter.handler.impl.RedissonRequestRateLimiterImpl;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({RedissonClient.class})
@ConditionalOnProperty(prefix = "pangju.web.rate-limit", value = "type", havingValue = "REDISSON")
public class RedissonRequestRateLimiterConfiguration {
	@ConditionalOnMissingBean(RequestRateLimiter.class)
	@Bean
	public RedissonRequestRateLimiterImpl redissonRequestRateLimiter(RequestRateLimitProperties properties,
																	 BeanFactory beanFactory) {
		return new RedissonRequestRateLimiterImpl(properties, beanFactory);
	}
}
