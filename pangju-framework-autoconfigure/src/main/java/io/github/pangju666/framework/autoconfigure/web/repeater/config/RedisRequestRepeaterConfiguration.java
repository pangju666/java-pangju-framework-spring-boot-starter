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

package io.github.pangju666.framework.autoconfigure.web.repeater.config;

import io.github.pangju666.framework.autoconfigure.web.repeater.RequestRepeatProperties;
import io.github.pangju666.framework.autoconfigure.web.repeater.handler.RequestRepeater;
import io.github.pangju666.framework.autoconfigure.web.repeater.handler.impl.RedisRequestRepeater;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisOperations.class)
@ConditionalOnProperty(prefix = "pangju.web.repeat", value = "type", havingValue = "REDIS")
public class RedisRequestRepeaterConfiguration {
	@ConditionalOnMissingBean(RequestRepeater.class)
	@Bean
	public RedisRequestRepeater redisRequestRepeater(RequestRepeatProperties properties, BeanFactory beanFactory) {
		return new RedisRequestRepeater(properties, beanFactory);
	}
}
