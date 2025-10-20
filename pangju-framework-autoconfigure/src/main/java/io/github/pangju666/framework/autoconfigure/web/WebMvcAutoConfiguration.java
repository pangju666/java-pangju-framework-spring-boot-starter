package io.github.pangju666.framework.autoconfigure.web;

import io.github.pangju666.framework.autoconfigure.web.interceptor.RequestRateLimitInterceptor;
import io.github.pangju666.framework.autoconfigure.web.interceptor.RequestSignatureInterceptor;
import io.github.pangju666.framework.autoconfigure.web.limiter.RequestRateLimiter;
import io.github.pangju666.framework.autoconfigure.web.properties.RequestSignatureProperties;
import io.github.pangju666.framework.autoconfigure.web.resolver.EncryptRequestParamArgumentResolver;
import io.github.pangju666.framework.autoconfigure.web.resolver.EnumRequestParamArgumentResolver;
import io.github.pangju666.framework.autoconfigure.web.store.SignatureSecretKeyStore;
import io.github.pangju666.framework.web.interceptor.BaseHttpHandlerInterceptor;
import jakarta.servlet.Servlet;
import org.apache.commons.collections4.ListUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;

@AutoConfiguration(after = org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
public class WebMvcAutoConfiguration implements WebMvcConfigurer {
	private final List<BaseHttpHandlerInterceptor> interceptors;
	private final RequestSignatureProperties signatureProperties;
	private final RequestRateLimiter requestRateLimiter;
	private final SignatureSecretKeyStore secretKeyStore;

	private List<String> excludePathPatterns = Collections.emptyList();

	public WebMvcAutoConfiguration(List<BaseHttpHandlerInterceptor> interceptors,
								   RequestSignatureProperties signatureProperties,
								   RequestRateLimiter requestRateLimiter,
								   SignatureSecretKeyStore secretKeyStore) {
		this.interceptors = interceptors;
		this.signatureProperties = signatureProperties;
		this.requestRateLimiter = requestRateLimiter;
		this.secretKeyStore = secretKeyStore;
	}

	/*@Autowired(required = false)
	public void setExcludePathPatterns(Map<String, ExcludePathPatternsProvider> excludePathPatternProviderMap) {
		this.excludePathPatterns = excludePathPatternProviderMap.values()
			.stream()
			.map(ExcludePathPatternsProvider::getExcludePaths)
			.flatMap(Set::stream)
			.toList();
	}*/

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new EnumRequestParamArgumentResolver());
		resolvers.add(new EncryptRequestParamArgumentResolver());
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new RequestSignatureInterceptor(signatureProperties, secretKeyStore))
			.addPathPatterns("/**")
			.excludePathPatterns(excludePathPatterns);
		registry.addInterceptor(new RequestRateLimitInterceptor(requestRateLimiter))
			.addPathPatterns("/**")
			.excludePathPatterns(excludePathPatterns);

		for (BaseHttpHandlerInterceptor interceptor : this.interceptors) {
			registry.addInterceptor(interceptor)
				.addPathPatterns(interceptor.getPatterns())
				.excludePathPatterns(ListUtils.union(interceptor.getExcludePathPatterns(), excludePathPatterns))
				.order(interceptor.getOrder());
		}
	}
}