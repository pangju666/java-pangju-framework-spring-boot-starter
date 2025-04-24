package io.github.pangju666.framework.autoconfigure.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@AutoConfiguration(after = org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class)
@ConditionalOnClass(RestClient.class)
public class RestClientAutoConfiguration {
	@ConditionalOnBean(RestClient.Builder.class)
	@ConditionalOnMissingBean(RestClient.class)
	@Bean
	public RestClient restClient(RestClient.Builder builder) {
		return builder.build();
	}
}
