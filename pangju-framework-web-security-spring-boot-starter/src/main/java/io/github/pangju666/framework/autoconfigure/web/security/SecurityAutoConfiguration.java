package io.github.pangju666.framework.autoconfigure.web.security;

import io.github.pangju666.framework.autoconfigure.web.security.authentication.Authenticator;
import io.github.pangju666.framework.autoconfigure.web.security.authentication.impl.UsernamePasswordAuthenticator;
import io.github.pangju666.framework.autoconfigure.web.security.configuration.MapAccessTokenStoreConfiguration;
import io.github.pangju666.framework.autoconfigure.web.security.configuration.RedisAccessTokenStoreConfiguration;
import io.github.pangju666.framework.autoconfigure.web.security.filter.AuthenticateLoginFilter;
import io.github.pangju666.framework.autoconfigure.web.security.interceptor.AuthenticatedInterceptor;
import io.github.pangju666.framework.autoconfigure.web.security.properties.SecurityProperties;
import io.github.pangju666.framework.autoconfigure.web.security.store.AccessTokenStore;
import io.github.pangju666.framework.autoconfigure.web.security.store.AuthenticatedUserStore;
import io.github.pangju666.framework.autoconfigure.web.security.store.impl.PropertiesAuthenticatedUserStoreImpl;
import io.github.pangju666.framework.web.provider.ExcludePathPatternProvider;
import jakarta.servlet.Servlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@Import({MapAccessTokenStoreConfiguration.class, RedisAccessTokenStoreConfiguration.class})
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {
	private Set<String> excludePathPatterns = Collections.emptySet();

	@Autowired(required = false)
	public void setExcludePathPatterns(Map<String, ExcludePathPatternProvider> excludePathPatternProviderMap) {
		this.excludePathPatterns = excludePathPatternProviderMap.values()
			.stream()
			.map(ExcludePathPatternProvider::getExcludePaths)
			.flatMap(List::stream)
			.collect(Collectors.toSet());
	}

	@Bean
	public FilterRegistrationBean<AuthenticateLoginFilter> authenticateLoginFilterRegistrationBean(
		AccessTokenStore accessTokenStore, List<Authenticator> authenticators) {
		FilterRegistrationBean<AuthenticateLoginFilter> filterRegistrationBean = new FilterRegistrationBean<>(
			new AuthenticateLoginFilter(accessTokenStore, excludePathPatterns, authenticators));
		for (Authenticator handler : authenticators) {
			filterRegistrationBean.addUrlPatterns(handler.getRequestUrl());
		}
		return filterRegistrationBean;
	}

	@ConditionalOnMissingBean(PasswordEncoder.class)
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	Authenticator usernamePasswordAuthenticator(PasswordEncoder encoder,
												SecurityProperties properties,
												AuthenticatedUserStore authenticatedUserStore) {
		return new UsernamePasswordAuthenticator(properties, encoder, authenticatedUserStore);
	}

	@ConditionalOnMissingBean(AuthenticatedUserStore.class)
	@Bean
	AuthenticatedUserStore propertiesAuthenticatedUserStore(SecurityProperties properties) {
		return new PropertiesAuthenticatedUserStoreImpl(properties);
	}

	@Bean
	public AuthenticatedInterceptor authenticatedInterceptor(AccessTokenStore accessTokenStore, SecurityProperties properties) {
		return new AuthenticatedInterceptor(accessTokenStore, properties);
	}
}