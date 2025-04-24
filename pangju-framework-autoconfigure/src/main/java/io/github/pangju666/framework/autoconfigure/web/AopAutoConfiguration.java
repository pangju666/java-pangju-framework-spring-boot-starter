package io.github.pangju666.framework.autoconfigure.web;

import io.github.pangju666.framework.autoconfigure.web.aspect.RequestRateLimitAspect;
import io.github.pangju666.framework.autoconfigure.web.aspect.RequestRepeatAspect;
import io.github.pangju666.framework.autoconfigure.web.limiter.RequestRateLimiter;
import io.github.pangju666.framework.autoconfigure.web.repeater.RequestRepeater;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = {RequestRateLimiterAutoConfiguration.class, RequestRepeaterAutoConfiguration.class,
	org.springframework.boot.autoconfigure.aop.AopAutoConfiguration.class})
public class AopAutoConfiguration {
	@ConditionalOnBean(RequestRateLimiter.class)
	@Bean
	public RequestRateLimitAspect requestRateLimitAspect(RequestRateLimiter requestRateLimiter) {
		return new RequestRateLimitAspect(requestRateLimiter);
	}

	@ConditionalOnBean(RequestRepeater.class)
	@Bean
	public RequestRepeatAspect requestRepeatAspect(RequestRepeater requestRepeater) {
		return new RequestRepeatAspect(requestRepeater);
	}
}
