package io.github.pangju666.framework.autoconfigure.web;

import io.github.pangju666.framework.autoconfigure.web.interceptor.RequestLimitInterceptor;
import io.github.pangju666.framework.autoconfigure.web.interceptor.RequestRepeatInterceptor;
import io.github.pangju666.framework.autoconfigure.web.properties.RequestLimitProperties;
import io.github.pangju666.framework.autoconfigure.web.properties.RequestRepeatProperties;
import io.github.pangju666.framework.autoconfigure.web.provider.ExcludePathPatternProvider;
import io.github.pangju666.framework.autoconfigure.web.resolver.EncryptRequestParamArgumentResolver;
import io.github.pangju666.framework.web.interceptor.BaseRequestInterceptor;
import io.github.pangju666.framework.web.resolver.EnumRequestParamArgumentResolver;
import jakarta.servlet.Servlet;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@AutoConfiguration(after = {
	org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.class
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@EnableConfigurationProperties({RequestLimitProperties.class, RequestRepeatProperties.class})
public class WebMvcAutoConfiguration implements WebMvcConfigurer, BeanFactoryAware {
	private final RequestLimitProperties requestLimitProperties;
	private final RequestRepeatProperties requestRepeatProperties;
	private final List<BaseRequestInterceptor> interceptors;
	private List<String> excludePathPatterns = Collections.emptyList();
	private BeanFactory beanFactory;

	public WebMvcAutoConfiguration(RequestLimitProperties requestLimitProperties,
								   RequestRepeatProperties requestRepeatProperties,
								   List<BaseRequestInterceptor> interceptors) {
		this.requestRepeatProperties = requestRepeatProperties;
		this.requestLimitProperties = requestLimitProperties;
		this.interceptors = interceptors;
	}

	@Autowired(required = false)
	public void setExcludePathPatterns(Map<String, ExcludePathPatternProvider> excludePathPatternProviderMap) {
		this.excludePathPatterns = excludePathPatternProviderMap.values()
			.stream()
			.map(ExcludePathPatternProvider::getExcludePaths)
			.flatMap(List::stream)
			.toList();
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new EnumRequestParamArgumentResolver());
		resolvers.add(new EncryptRequestParamArgumentResolver());
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new RequestRepeatInterceptor(requestRepeatProperties, beanFactory))
			.addPathPatterns("/**")
			.excludePathPatterns(excludePathPatterns);
		registry.addInterceptor(new RequestLimitInterceptor(requestLimitProperties, beanFactory))
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