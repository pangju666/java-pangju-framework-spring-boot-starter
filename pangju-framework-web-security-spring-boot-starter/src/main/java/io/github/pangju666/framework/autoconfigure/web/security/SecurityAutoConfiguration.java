package io.github.pangju666.framework.autoconfigure.web.security;

import io.github.pangju666.framework.autoconfigure.web.security.filter.AuthenticateKeyFilter;
import io.github.pangju666.framework.autoconfigure.web.security.filter.AuthenticateLoginFilter;
import io.github.pangju666.framework.autoconfigure.web.security.interceptor.AuthenticatedInterceptor;
import io.github.pangju666.framework.autoconfigure.web.security.properties.SecurityProperties;
import io.github.pangju666.framework.web.provider.ExcludePathPatternProvider;
import jakarta.servlet.Servlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {
	private Set<String> excludePathPatterns = Collections.emptySet();

	@Autowired(required = false)
	public void setExcludePathPatterns(Map<String, ExcludePathPatternProvider> excludePathPatternProviderMap) {
		this.excludePathPatterns = excludePathPatternProviderMap.values()
			.stream()
			.map(ExcludePathPatternProvider::getExcludePathSet)
			.flatMap(Set::stream)
			.collect(Collectors.toSet());
	}

	@ConditionalOnExpression("#{environment['pangju.web.authenticate.algorithm'] == null || T(io.github.pangju666.framework.autoconfigure.web.security.enums.PasswordAlgorithm).AES256.name().equals(environment['pangju.web.authenticate.algorithm'].toUpperCase()) || T(io.github.pangju666.framework.autoconfigure.web.security.enums.PasswordAlgorithm).RSA.name().equals(environment['pangju.web.authenticate.algorithm'].toUpperCase())}")
	@Bean
	public FilterRegistrationBean<AuthenticateKeyFilter> authenticateKeyFilterFilterRegistrationBean(SecurityProperties properties) {
		FilterRegistrationBean<AuthenticateKeyFilter> filterRegistrationBean = new FilterRegistrationBean<>(new AuthenticateKeyFilter(properties, excludePathPatterns));
		filterRegistrationBean.addUrlPatterns(properties.getRequest().getKeyUrl());
		return filterRegistrationBean;
	}

	@Bean
	public FilterRegistrationBean<AuthenticateLoginFilter> authenticateLoginFilterFilterRegistrationBean(SecurityProperties properties) throws NoSuchAlgorithmException, InvalidKeySpecException {
		FilterRegistrationBean<AuthenticateLoginFilter> filterRegistrationBean = new FilterRegistrationBean<>(new AuthenticateLoginFilter(properties, excludePathPatterns));
		filterRegistrationBean.addUrlPatterns(properties.getRequest().getLoginUrl());
		return filterRegistrationBean;
	}

	@Bean
	public AuthenticatedInterceptor authenticatedInterceptor(SecurityProperties properties) {
		return new AuthenticatedInterceptor(properties);
	}
}
