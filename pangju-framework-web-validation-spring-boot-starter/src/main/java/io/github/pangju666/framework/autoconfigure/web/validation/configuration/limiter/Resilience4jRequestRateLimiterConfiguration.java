package io.github.pangju666.framework.autoconfigure.web.validation.configuration.limiter;

import io.github.pangju666.framework.autoconfigure.web.validation.limiter.RequestRateLimiter;
import io.github.pangju666.framework.autoconfigure.web.validation.limiter.impl.Resilience4JRequestRateLimiterImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "pangju.web.request.rate-limit", value = "type", havingValue = "RESILIENCE4J", matchIfMissing = true)
public class Resilience4jRequestRateLimiterConfiguration {
	@ConditionalOnMissingBean(RequestRateLimiter.class)
	@Bean
	public Resilience4JRequestRateLimiterImpl resilience4jRateLimiter() {
		return new Resilience4JRequestRateLimiterImpl();
	}
}
