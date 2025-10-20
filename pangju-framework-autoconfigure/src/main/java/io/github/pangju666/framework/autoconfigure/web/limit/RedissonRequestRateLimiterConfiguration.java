package io.github.pangju666.framework.autoconfigure.web.limit;

import io.github.pangju666.framework.autoconfigure.web.limit.limiter.RequestRateLimiter;
import io.github.pangju666.framework.autoconfigure.web.limit.limiter.impl.RedissonRequestRateLimiterImpl;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

//@Configuration(proxyBeanMethods = false)
//@ConditionalOnClass({RedissonClient.class})
//@ConditionalOnProperty(prefix = "pangju.web.request.rate-limit", value = "type", havingValue = "REDISSON")
public class RedissonRequestRateLimiterConfiguration {
	@ConditionalOnMissingBean(RequestRateLimiter.class)
	@Bean
	public RedissonRequestRateLimiterImpl redissonRateLimiter(RequestRateLimitProperties properties,
															  BeanFactory beanFactory) {
		return new RedissonRequestRateLimiterImpl(properties, beanFactory);
	}
}
