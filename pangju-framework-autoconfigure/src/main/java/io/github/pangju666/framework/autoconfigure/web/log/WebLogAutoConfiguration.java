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

import io.github.pangju666.framework.autoconfigure.web.log.filter.WebLogFilter;
import io.github.pangju666.framework.autoconfigure.web.log.handler.WebLogHandler;
import io.github.pangju666.framework.autoconfigure.web.log.revceiver.WebLogReceiver;
import io.github.pangju666.framework.autoconfigure.web.log.sender.WebLogSender;
import io.github.pangju666.framework.autoconfigure.web.log.sender.impl.disruptor.DisruptorWebLogEventHandler;
import io.github.pangju666.framework.autoconfigure.web.log.sender.impl.disruptor.DisruptorWebLogSender;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collections;
import java.util.List;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.log", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(WebLogProperties.class)
public class WebLogAutoConfiguration {
	@ConditionalOnProperty(prefix = "pangju.web.log", name = "sender-type", havingValue = "DISRUPTOR", matchIfMissing = true)
	@ConditionalOnMissingBean(DisruptorWebLogEventHandler.class)
	@ConditionalOnBean(WebLogReceiver.class)
	@Bean
	public DisruptorWebLogEventHandler disruptorWebLogEventHandler(WebLogReceiver webLogReceiver) {
		return new DisruptorWebLogEventHandler(webLogReceiver);
	}

	@ConditionalOnProperty(prefix = "pangju.web.log", name = "sender-type", havingValue = "DISRUPTOR", matchIfMissing = true)
	@ConditionalOnBean(DisruptorWebLogEventHandler.class)
	@ConditionalOnMissingBean(WebLogSender.class)
	@Bean
	public DisruptorWebLogSender disruptorWebLogSender(WebLogProperties properties, DisruptorWebLogEventHandler eventHandler) {
		return new DisruptorWebLogSender(properties, eventHandler);
	}

	@ConditionalOnBean({WebLogSender.class})
	@ConditionalOnMissingFilterBean
	@Bean
	public FilterRegistrationBean<WebLogFilter> webLogFilterRegistrationBean(WebLogProperties properties,
																			 WebLogSender webLogSender,
																			 List<WebLogHandler> webLogHandlers,
																			 RequestMappingHandlerMapping requestMappingHandlerMapping) {
		WebLogFilter webLogFilter = new WebLogFilter(properties, webLogSender, Collections.emptySet(),
			webLogHandlers, requestMappingHandlerMapping);
		FilterRegistrationBean<WebLogFilter> filterRegistrationBean = new FilterRegistrationBean<>(webLogFilter);
		filterRegistrationBean.addUrlPatterns("/*");
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
		return filterRegistrationBean;
	}
}
