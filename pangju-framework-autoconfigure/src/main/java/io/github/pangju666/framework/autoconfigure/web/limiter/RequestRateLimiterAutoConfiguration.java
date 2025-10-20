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

import io.github.pangju666.framework.autoconfigure.web.WebMvcAutoConfiguration;
import io.github.pangju666.framework.autoconfigure.web.limiter.source.impl.IpRateLimitSourceExtractor;
import io.github.pangju666.framework.autoconfigure.web.limiter.config.RedissonRequestRateLimiterConfiguration;
import io.github.pangju666.framework.autoconfigure.web.limiter.config.Resilience4jRequestRateLimiterConfiguration;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.DispatcherServlet;

@AutoConfiguration(before = WebMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@EnableConfigurationProperties(RequestRateLimitProperties.class)
@Import({Resilience4jRequestRateLimiterConfiguration.class, RedissonRequestRateLimiterConfiguration.class})
public class RequestRateLimiterAutoConfiguration {
	//@ConditionalOnMissingBean
	//@Bean
	public IpRateLimitSourceExtractor ipRateLimitSourceExtractor() {
		return new IpRateLimitSourceExtractor();
	}
}
