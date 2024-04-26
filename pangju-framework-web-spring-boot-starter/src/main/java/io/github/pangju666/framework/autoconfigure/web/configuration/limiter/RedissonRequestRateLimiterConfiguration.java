package io.github.pangju666.framework.autoconfigure.web.configuration.limiter;

import io.github.pangju666.framework.autoconfigure.web.limiter.RequestRateLimiter;
import io.github.pangju666.framework.autoconfigure.web.limiter.impl.RedissonRequestRateLimiterImpl;
import io.github.pangju666.framework.autoconfigure.web.properties.RequestRateLimitProperties;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({RedissonClient.class})
@ConditionalOnProperty(prefix = "pangju.web.request.rate-limit", value = "type", havingValue = "REDISSON")
public class RedissonRequestRateLimiterConfiguration {
	@ConditionalOnMissingBean(RequestRateLimiter.class)
	@Bean
	public RedissonRequestRateLimiterImpl redissonRateLimiter(RequestRateLimitProperties properties,
															  BeanFactory beanFactory) {
		return new RedissonRequestRateLimiterImpl(properties, beanFactory);
	}
}
