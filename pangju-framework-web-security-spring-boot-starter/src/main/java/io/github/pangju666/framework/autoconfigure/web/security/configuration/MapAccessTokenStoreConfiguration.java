package io.github.pangju666.framework.autoconfigure.web.security.configuration;

import io.github.pangju666.framework.autoconfigure.web.security.properties.SecurityProperties;
import io.github.pangju666.framework.autoconfigure.web.security.store.AccessTokenStore;
import io.github.pangju666.framework.autoconfigure.web.security.store.impl.MapAccessTokenStoreImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "pangju.web.security.token", value = "type", havingValue = "MAP", matchIfMissing = true)
public class MapAccessTokenStoreConfiguration {
	@ConditionalOnMissingBean(AccessTokenStore.class)
	@Bean
	AccessTokenStore mapAccessTokenStore(SecurityProperties properties) {
		return new MapAccessTokenStoreImpl(properties);
	}
}