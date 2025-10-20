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

package io.github.pangju666.framework.autoconfigure.web.log;

import io.github.pangju666.framework.autoconfigure.web.log.config.KafkaSenderAutoConfiguration;
import io.github.pangju666.framework.autoconfigure.web.log.config.MongoReceiverAutoConfiguration;
import io.github.pangju666.framework.autoconfigure.web.log.filter.WebLogFilter;
import io.github.pangju666.framework.autoconfigure.web.log.handler.WebLogHandler;
import io.github.pangju666.framework.autoconfigure.web.log.sender.WebLogSender;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collections;
import java.util.List;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@ConditionalOnProperty(prefix = "pangju.web.log", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({KafkaSenderAutoConfiguration.class, MongoReceiverAutoConfiguration.class})
@EnableConfigurationProperties(WebLogProperties.class)
public class WebLogAutoConfiguration {
	@ConditionalOnBean({WebLogSender.class})
	@Bean
	public FilterRegistrationBean<WebLogFilter> webLogFilterRegistrationBean(WebLogProperties properties,
																			 WebLogSender webLogSender,
																			 List<WebLogHandler> webLogHandlers,
																			 RequestMappingHandlerMapping requestMappingHandlerMapping) {
		WebLogFilter webLogFilter = new WebLogFilter(properties, webLogSender, Collections.emptySet(),
			webLogHandlers, requestMappingHandlerMapping);
		FilterRegistrationBean<WebLogFilter> filterRegistrationBean = new FilterRegistrationBean<>(webLogFilter);
		filterRegistrationBean.addUrlPatterns("/*");
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 3);
		return filterRegistrationBean;
	}
}
