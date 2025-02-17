package io.github.pangju666.framework.autoconfigure.web.security;

import io.github.pangju666.framework.autoconfigure.web.security.config.customizer.AuthorizeHttpRequestsConfigurerCustomizer;
import io.github.pangju666.framework.autoconfigure.web.security.config.customizer.CsrfConfigurerCustomizer;
import io.github.pangju666.framework.autoconfigure.web.security.config.customizer.FormLoginConfigurerCustomizer;
import io.github.pangju666.framework.autoconfigure.web.security.config.customizer.HttpBasicConfigurerCustomizer;
import io.github.pangju666.framework.autoconfigure.web.security.properties.SecurityProperties;
import io.github.pangju666.framework.web.filter.BaseRequestFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Objects;

@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {
	@ConditionalOnMissingBean(SecurityFilterChain.class)
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, BeanFactory beanFactory,
												   SecurityProperties properties) throws Exception {
		if (properties.getHttpBasic().isDisabled()) {
			http.httpBasic(AbstractHttpConfigurer::disable);
		} else {
			try {
				http.httpBasic(beanFactory.getBean(HttpBasicConfigurerCustomizer.class));
			} catch (NoSuchBeanDefinitionException ignored) {
				http.httpBasic(Customizer.withDefaults());
			}
			if (Objects.nonNull(properties.getHttpBasic().getFilter())) {
				addFilterAt(http, beanFactory, properties.getHttpBasic().getFilter(),
					BasicAuthenticationFilter.class);
			}
		}

		if (properties.getCsrf().isDisabled()) {
			http.csrf(AbstractHttpConfigurer::disable);
		} else {
			try {
				http.csrf(beanFactory.getBean(CsrfConfigurerCustomizer.class));
			} catch (NoSuchBeanDefinitionException ignored) {
				http.csrf(Customizer.withDefaults());
			}
			if (Objects.nonNull(properties.getCsrf().getFilter())) {
				addFilterAt(http, beanFactory, properties.getCsrf().getFilter(), CsrfFilter.class);
			}
		}

		if (properties.getFormLogin().isDisabled()) {
			http.formLogin(AbstractHttpConfigurer::disable);
		} else {
			try {
				http.formLogin(beanFactory.getBean(FormLoginConfigurerCustomizer.class));
			} catch (NoSuchBeanDefinitionException ignored) {
				http.formLogin(Customizer.withDefaults());
			}
			if (Objects.nonNull(properties.getFormLogin().getFilter())) {
				addFilterAt(http, beanFactory, properties.getFormLogin().getFilter(),
					UsernamePasswordAuthenticationFilter.class);
			}
		}

		if (properties.getAuthorizeHttpRequests().isDisabled()) {
			http.authorizeHttpRequests(
				registry -> registry
					.anyRequest()
					.permitAll());
		} else {
			try {
				http.authorizeHttpRequests(beanFactory.getBean(
					AuthorizeHttpRequestsConfigurerCustomizer.class));
			} catch (NoSuchBeanDefinitionException ignored) {
				http.authorizeHttpRequests(Customizer.withDefaults());
			}
			if (Objects.nonNull(properties.getAuthorizeHttpRequests().getFilter())) {
				addFilterAt(http, beanFactory, properties.getAuthorizeHttpRequests().getFilter(),
					AuthorizationFilter.class);
			}
		}

		return http.build();
	}

	@ConditionalOnMissingBean(UserDetailsService.class)
	@Bean
	public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder, SecurityProperties properties) {
		List<UserDetails> users = properties.getUsers()
			.stream()
			.map(user -> User.builder()
				.username(user.getUsername())
				.password(passwordEncoder.encode(user.getPassword()))
				.roles(StringUtils.join(user.getRoles(), ","))
				.disabled(user.isDisabled())
				.accountLocked(user.isAccountLocked())
				.accountExpired(user.isAccountExpired())
				.build())
			.toList();
		return new InMemoryUserDetailsManager(users);
	}

	@ConditionalOnMissingBean(PasswordEncoder.class)
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	private void addFilterAt(HttpSecurity http, BeanFactory beanFactory,
							 Class<? extends BaseRequestFilter> newFilterClass, Class<? extends Filter> oldFilterClass) {
		try {
			http.addFilterAt(beanFactory.getBean(newFilterClass), oldFilterClass);
		} catch (NoSuchBeanDefinitionException ignored) {
		}
	}
}
