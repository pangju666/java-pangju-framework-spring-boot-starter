package io.github.pangju666.framework.autoconfigure.web.validation.configuration.repeater;

import io.github.pangju666.framework.autoconfigure.web.validation.properties.RequestRepeatProperties;
import io.github.pangju666.framework.autoconfigure.web.validation.repeater.RequestRepeater;
import io.github.pangju666.framework.autoconfigure.web.validation.repeater.impl.RedisRequestRepeater;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisOperations.class)
@ConditionalOnProperty(prefix = "pangju.web.request.repeat", value = "type", havingValue = "REDIS")
public class RedisRequestRepeaterConfiguration {
	@ConditionalOnMissingBean(RequestRepeater.class)
	@Bean
	public RedisRequestRepeater redisRequestRepeater(RequestRepeatProperties properties, BeanFactory beanFactory) {
		return new RedisRequestRepeater(properties, beanFactory);
	}
}
