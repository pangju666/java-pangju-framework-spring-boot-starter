package io.github.pangju666.framework.autoconfigure.web.security;

import io.github.pangju666.framework.autoconfigure.web.security.config.AuthorizeHttpRequestsConfigurerCustomizer;
import io.github.pangju666.framework.autoconfigure.web.security.config.CsrfConfigurerCustomizer;
import io.github.pangju666.framework.autoconfigure.web.security.config.FormLoginConfigurerCustomizer;
import io.github.pangju666.framework.autoconfigure.web.security.config.HttpBasicConfigurerCustomizer;
import io.github.pangju666.framework.autoconfigure.web.security.properties.SecurityProperties;
import io.github.pangju666.framework.web.filter.BaseRequestFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Objects;

@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {
	@ConditionalOnMissingBean(SecurityFilterChain.class)
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, BeanFactory beanFactory,
												   SecurityProperties securityProperties) throws Exception {
		if (securityProperties.getHttpBasic().isDisabled()) {
			http.httpBasic(AbstractHttpConfigurer::disable);
		} else {
			try {
				http.httpBasic(beanFactory.getBean(HttpBasicConfigurerCustomizer.class));
			} catch (NoSuchBeanDefinitionException ignored) {
				http.httpBasic(Customizer.withDefaults());
			}
			if (Objects.nonNull(securityProperties.getHttpBasic().getFilter())) {
				addFilterAt(http, beanFactory, securityProperties.getHttpBasic().getFilter(),
					BasicAuthenticationFilter.class);
			}
		}

		if (securityProperties.getCsrf().isDisabled()) {
			http.csrf(AbstractHttpConfigurer::disable);
		} else {
			try {
				http.csrf(beanFactory.getBean(CsrfConfigurerCustomizer.class));
			} catch (NoSuchBeanDefinitionException ignored) {
				http.csrf(Customizer.withDefaults());
			}
			if (Objects.nonNull(securityProperties.getCsrf().getFilter())) {
				addFilterAt(http, beanFactory, securityProperties.getCsrf().getFilter(), CsrfFilter.class);
			}
		}

		if (securityProperties.getFormLogin().isDisabled()) {
			http.formLogin(AbstractHttpConfigurer::disable);
		} else {
			try {
				http.formLogin(beanFactory.getBean(FormLoginConfigurerCustomizer.class));
			} catch (NoSuchBeanDefinitionException ignored) {
				http.formLogin(Customizer.withDefaults());
			}
			if (Objects.nonNull(securityProperties.getFormLogin().getFilter())) {
				addFilterAt(http, beanFactory, securityProperties.getFormLogin().getFilter(),
					UsernamePasswordAuthenticationFilter.class);
			}
		}

		if (securityProperties.getAuthorizeHttpRequests().isDisabled()) {
			http.authorizeHttpRequests(
				registry -> registry
					.anyRequest()
					.permitAll());
		} else {
			try {
				http.authorizeHttpRequests(beanFactory.getBean(AuthorizeHttpRequestsConfigurerCustomizer.class));
			} catch (NoSuchBeanDefinitionException ignored) {
				http.authorizeHttpRequests(Customizer.withDefaults());
			}
			if (Objects.nonNull(securityProperties.getAuthorizeHttpRequests().getFilter())) {
				addFilterAt(http, beanFactory, securityProperties.getAuthorizeHttpRequests().getFilter(),
					AuthorizationFilter.class);
			}
		}

		return http.build();
	}

	private void addFilterAt(HttpSecurity http, BeanFactory beanFactory,
							 Class<? extends BaseRequestFilter> newFilterClass, Class<? extends Filter> oldFilterClass) {
		try {
			http.addFilterAt(beanFactory.getBean(newFilterClass), oldFilterClass);
		} catch (NoSuchBeanDefinitionException ignored) {
		}
	}
}
