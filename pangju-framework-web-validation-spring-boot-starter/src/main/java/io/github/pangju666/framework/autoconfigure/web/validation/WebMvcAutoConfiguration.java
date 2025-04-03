package io.github.pangju666.framework.autoconfigure.web.validation;

import io.github.pangju666.framework.autoconfigure.web.validation.interceptor.RequestRateLimitInterceptor;
import io.github.pangju666.framework.autoconfigure.web.validation.interceptor.RequestSignatureInterceptor;
import io.github.pangju666.framework.autoconfigure.web.validation.limiter.RequestRateLimiter;
import io.github.pangju666.framework.autoconfigure.web.validation.properties.RequestSignatureProperties;
import io.github.pangju666.framework.autoconfigure.web.validation.store.SignatureSecretKeyStore;
import io.github.pangju666.framework.web.interceptor.BaseRequestInterceptor;
import io.github.pangju666.framework.web.provider.ExcludePathPatternsProvider;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.servlet.Servlet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AutoConfiguration(after = org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
public class WebMvcAutoConfiguration implements WebMvcConfigurer {
	private final List<BaseRequestInterceptor> interceptors;
	private final RequestSignatureProperties signatureProperties;
	private final RequestRateLimiter requestRateLimiter;
	private final SignatureSecretKeyStore secretKeyStore;

	private List<String> excludePathPatterns = Collections.emptyList();

	public WebMvcAutoConfiguration(List<BaseRequestInterceptor> interceptors,
								   RequestSignatureProperties signatureProperties,
								   SignatureSecretKeyStore secretKeyStore,
								   RequestRateLimiter requestRateLimiter) {
		this.interceptors = interceptors;
		this.signatureProperties = signatureProperties;
		this.secretKeyStore = secretKeyStore;
		this.requestRateLimiter = requestRateLimiter;
	}

	@Autowired(required = false)
	public void setExcludePathPatterns(Map<String, ExcludePathPatternsProvider> excludePathPatternProviderMap) {
		this.excludePathPatterns = excludePathPatternProviderMap.values()
			.stream()
			.map(ExcludePathPatternsProvider::getExcludePaths)
			.flatMap(Set::stream)
			.toList();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new RequestSignatureInterceptor(signatureProperties, secretKeyStore))
			.addPathPatterns("/**")
			.excludePathPatterns(excludePathPatterns);
		registry.addInterceptor(new RequestRateLimitInterceptor(requestRateLimiter))
			.addPathPatterns("/**")
			.excludePathPatterns(excludePathPatterns);

		for (BaseRequestInterceptor interceptor : this.interceptors) {
			registry.addInterceptor(interceptor)
				.addPathPatterns(interceptor.getPatterns())
				.excludePathPatterns(ListUtils.union(interceptor.getExcludePathPatterns(), excludePathPatterns))
				.order(interceptor.getOrder());
		}
	}
}