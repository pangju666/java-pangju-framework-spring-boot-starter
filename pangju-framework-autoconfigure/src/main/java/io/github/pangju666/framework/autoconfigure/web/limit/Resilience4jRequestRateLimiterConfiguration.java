package io.github.pangju666.framework.autoconfigure.web.limit;

import io.github.pangju666.framework.autoconfigure.web.limit.limiter.RequestRateLimiter;
import io.github.pangju666.framework.autoconfigure.web.limit.limiter.impl.Resilience4JRequestRateLimiterImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

//@Configuration(proxyBeanMethods = false)
//@ConditionalOnProperty(prefix = "pangju.web.request.rate-limit", value = "type", havingValue = "RESILIENCE4J", matchIfMissing = true)
public class Resilience4jRequestRateLimiterConfiguration {
	@ConditionalOnMissingBean(RequestRateLimiter.class)
	@Bean
	public Resilience4JRequestRateLimiterImpl resilience4jRateLimiter() {
		return new Resilience4JRequestRateLimiterImpl();
	}
}
