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

package io.github.pangju666.framework.autoconfigure.web;

import io.github.pangju666.framework.web.filter.ContentCachingWrapperFilter;
import io.github.pangju666.framework.web.filter.CorsFilter;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.Collections;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
public class FilterAutoConfiguration {
	@ConditionalOnClass(CorsFilter.class)
	@ConditionalOnMissingFilterBean
	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOriginPattern("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		FilterRegistrationBean<CorsFilter> filterRegistrationBean = new FilterRegistrationBean<>(
			new CorsFilter(source, Collections.emptySet()));
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
		return filterRegistrationBean;
	}

	@ConditionalOnClass(ContentCachingWrapperFilter.class)
	@ConditionalOnMissingFilterBean
	@Bean
	public FilterRegistrationBean<ContentCachingWrapperFilter> contentCachingWrapperFilterRegistrationBean() {
		ContentCachingWrapperFilter contentCachingWrapperFilter = new ContentCachingWrapperFilter(Collections.emptySet());
		FilterRegistrationBean<ContentCachingWrapperFilter> filterRegistrationBean = new FilterRegistrationBean<>(contentCachingWrapperFilter);
		filterRegistrationBean.addUrlPatterns("/*");
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
		return filterRegistrationBean;
	}
}
