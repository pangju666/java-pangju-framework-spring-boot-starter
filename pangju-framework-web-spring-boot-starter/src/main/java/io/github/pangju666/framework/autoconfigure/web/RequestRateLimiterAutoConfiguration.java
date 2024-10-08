package io.github.pangju666.framework.autoconfigure.web;

import io.github.pangju666.framework.autoconfigure.web.aspect.RequestRateLimitAspect;
import io.github.pangju666.framework.autoconfigure.web.configuration.limiter.RedissonRequestRateLimiterConfiguration;
import io.github.pangju666.framework.autoconfigure.web.configuration.limiter.Resilience4jRequestRateLimiterConfiguration;
import io.github.pangju666.framework.autoconfigure.web.limiter.RequestRateLimiter;
import io.github.pangju666.framework.autoconfigure.web.properties.RequestRateLimitProperties;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.DispatcherServlet;

@AutoConfiguration(before = WebMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@EnableConfigurationProperties(RequestRateLimitProperties.class)
@Import({Resilience4jRequestRateLimiterConfiguration.class, RedissonRequestRateLimiterConfiguration.class})
public class RequestRateLimiterAutoConfiguration {
	@AutoConfiguration(after = AopAutoConfiguration.class)
	public static class RequestRateLimitAopAutoConfiguration {
		@Bean
		public RequestRateLimitAspect requestRateLimitAspect(RequestRateLimiter requestRateLimiter) {
			return new RequestRateLimitAspect(requestRateLimiter);
		}
	}
}
