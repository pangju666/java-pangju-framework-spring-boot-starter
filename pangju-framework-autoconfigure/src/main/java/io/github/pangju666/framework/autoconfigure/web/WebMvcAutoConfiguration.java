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

import io.github.pangju666.framework.autoconfigure.web.crypto.resolver.EncryptRequestParamArgumentResolver;
import io.github.pangju666.framework.autoconfigure.web.limiter.interceptor.RequestRateLimitInterceptor;
import io.github.pangju666.framework.autoconfigure.web.limiter.limiter.RequestRateLimiter;
import io.github.pangju666.framework.autoconfigure.web.resolver.EnumRequestParamArgumentResolver;
import io.github.pangju666.framework.web.interceptor.BaseHttpHandlerInterceptor;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@AutoConfiguration(after = org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
public class WebMvcAutoConfiguration implements WebMvcConfigurer {
	private final List<BaseHttpHandlerInterceptor> interceptors;
	//private final RequestSignatureProperties signatureProperties;
	private final RequestRateLimiter requestRateLimiter;
	//private final SignatureSecretKeyStore secretKeyStore;

	public WebMvcAutoConfiguration(List<BaseHttpHandlerInterceptor> interceptors,
								   RequestRateLimiter requestRateLimiter
								   /*RequestSignatureProperties signatureProperties,

								   SignatureSecretKeyStore secretKeyStore*/) {
		this.interceptors = interceptors;
		this.requestRateLimiter = requestRateLimiter;
		/*this.signatureProperties = signatureProperties;

		this.secretKeyStore = secretKeyStore;*/
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new EnumRequestParamArgumentResolver());
		resolvers.add(new EncryptRequestParamArgumentResolver());
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		/*registry.addInterceptor(new RequestSignatureInterceptor(signatureProperties, secretKeyStore))
			.addPathPatterns("/**")
			.excludePathPatterns(excludePathPatterns);*/
		RequestRateLimitInterceptor requestRateLimitInterceptor = new RequestRateLimitInterceptor(requestRateLimiter);
		registry.addInterceptor(requestRateLimitInterceptor)
			.addPathPatterns(requestRateLimitInterceptor.getPatterns())
			.excludePathPatterns(requestRateLimitInterceptor.getExcludePathPatterns());

		for (BaseHttpHandlerInterceptor interceptor : this.interceptors) {
			registry.addInterceptor(interceptor)
				.addPathPatterns(interceptor.getPatterns())
				.excludePathPatterns(interceptor.getExcludePathPatterns())
				.order(interceptor.getOrder());
		}
	}
}