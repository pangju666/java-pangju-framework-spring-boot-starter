package io.github.pangju666.framework.autoconfigure.web.security.configuration;

import io.github.pangju666.framework.autoconfigure.web.security.properties.SecurityProperties;
import io.github.pangju666.framework.autoconfigure.web.security.store.AccessTokenStore;
import io.github.pangju666.framework.autoconfigure.web.security.store.impl.RedisAccessTokenStoreImpl;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisOperations.class)
@ConditionalOnProperty(prefix = "pangju.web.security.token", value = "type", havingValue = "REDIS")
public class RedisAccessTokenStoreConfiguration {
	@ConditionalOnMissingBean(AccessTokenStore.class)
	@Bean
	AccessTokenStore redisAccessTokenStore(BeanFactory beanFactory, SecurityProperties properties) {
		return new RedisAccessTokenStoreImpl(beanFactory, properties);
	}
}