package io.github.pangju666.framework.autoconfigure.data.dynamic.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(RedisClient.class)
@ConditionalOnProperty(prefix = "spring.redis", name = "client-type", havingValue = "lettuce", matchIfMissing = true)
public class ClientResourcesAutoConfiguration {
	@Bean(destroyMethod = "shutdown")
	@ConditionalOnMissingBean(ClientResources.class)
	public ClientResources clientResources() {
		return DefaultClientResources.create();
	}
}
